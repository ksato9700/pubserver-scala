package v1.books

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

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

  private val bookList = List(
    BookData(BookId("1"), "title 1", "book content 1"),
    BookData(BookId("2"), "title 2", "book content 2"),
    BookData(BookId("3"), "title 3", "book content 3"),
    BookData(BookId("4"), "title 4", "book content 4"),
    BookData(BookId("5"), "title 5", "book content 5")
  )

  override def list()(
      implicit mc: MarkerContext
  ): Future[Iterable[BookData]] = {
    Future {
      logger.trace(s"list: ")
      bookList
    }
  }

  override def get(
      id: BookId
  )(implicit mc: MarkerContext): Future[Option[BookData]] = {
    Future {
      logger.trace(s"get: id = $id")
      bookList.find(book => book.id == id)
    }
  }

  def create(data: BookData)(implicit mc: MarkerContext): Future[BookId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
