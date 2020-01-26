package v1.books
import scala.util.Properties
import javax.inject.{Inject, Singleton}

import java.time.{LocalDateTime, Instant, ZoneId}
import java.time.format.DateTimeFormatter.ISO_INSTANT

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

import org.mongodb.scala._
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Filters._
import org.bson.conversions.Bson

import scala.util.{Success, Failure}
import org.bson.conversions.Bson

final case class BookData(
    book_id: BookId,
    title: String,
    release_date: LocalDateTime
)

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

    val client: MongoClient = MongoClient()
    client.getDatabase("aozora")
  }

  private def bookList(filter: Option[Bson] = None) = {
    val collection = db.getCollection("books")
    val filtered = filter match {
      case Some(filter) => collection.find(filter)
      case None         => collection.find()
    }
    filtered.map((d: Document) => {
      val book_id = d.get("book_id").get.asInt32.getValue.toString
      val title = d.get("title").get.asString.getValue
      val release_date = d.get("release_date").get.asDateTime.getValue
      BookData(
        BookId(book_id),
        title,
        // LocalDate.parse(release_date, ISO_INSTANT)
        LocalDateTime
          .ofInstant(Instant.ofEpochMilli(release_date), ZoneId.systemDefault())
      )
    })
  }

  override def list()(
      implicit mc: MarkerContext
  ): Future[Iterable[BookData]] = {
    bookList().toFuture
  }

  override def get(
      id: BookId
  )(implicit mc: MarkerContext): Future[Option[BookData]] = {

    bookList(Some(equal("book_id", id.underlying))).head
      .map(book => Some(book))
      .fallbackTo(Future { None })
  }

  def create(data: BookData)(implicit mc: MarkerContext): Future[BookId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.book_id
    }
  }
}
