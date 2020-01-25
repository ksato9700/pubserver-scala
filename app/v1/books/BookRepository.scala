package v1.books
import scala.util.Properties
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

import org.mongodb.scala._
import org.mongodb.scala.bson._
import scala.util.{Success, Failure}

final case class BookData(id: BookId, title: String, body: String)

class BookId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object BookId {
  def apply(raw: String): BookId = {
    require(raw != null)
    new BookId(Integer.parseInt(raw))
  }
}

class BookExecutionContext @Inject() (actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

trait BookRepository {
  def create(data: BookData)(implicit mc: MarkerContext): Future[BookId]

  def list()(implicit mc: MarkerContext): Future[Iterable[BookData]]

  def get(id: BookId)(implicit mc: MarkerContext): Future[Option[BookData]]
}

@Singleton
class BookRepositoryImpl @Inject() ()(implicit ec: BookExecutionContext)
    extends BookRepository {

  private val logger = Logger(this.getClass)

  private val db: MongoDatabase = {
    val mongodb_credential =
      Properties.envOrElse("AOZORA_MONGODB_CREDENTIAL", "")
    val mongodb_host = Properties.envOrElse("AOZORA_MONGODB_HOST", "localhost")
    val mongodb_port = Properties.envOrElse("AOZORA_MONGODB_PORT", "27017")
    logger.info(s"host: ${mongodb_host}")

    //   val uri: String =
    //     s"mongodb://${mongodb_credential}@${mongodb_host}:${mongodb_port}/aozora?retryWrites=true&w=majority"
    //   System.setProperty("org.mongodb.async.type", "netty")
    // val client: MongoClient = MongoClient(uri)
    val client: MongoClient = MongoClient()
    val db: MongoDatabase = client.getDatabase("aozora")
    // println(db.getCollection("books").find().first())
    db

  }

  private def bookList = {
    db.getCollection("books")
      .find
      .map((d: Document) => {
        // d.toJson
        BookData(
          BookId(d.getOrElse("book_id", "000").asInt32.getValue.toString),
          d.getOrElse("title", "(no-title)").asString.getValue.toString,
          d.getOrElse("text_url", "(no-content)").asString.getValue.toString
        )
      })
  }

  override def list()(
      implicit mc: MarkerContext
  ): Future[Iterable[BookData]] = {
    bookList.toFuture
  }

  override def get(
      id: BookId
  )(implicit mc: MarkerContext): Future[Option[BookData]] = {
    logger.info(s"id: ${id}")

    bookList
      .filter(book => book.id == id)
      .head
      .map(book => Some(book))
      .fallbackTo(Future { None })
  }

  def create(data: BookData)(implicit mc: MarkerContext): Future[BookId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
