package v1.books

import javax.inject.{Inject, Provider}
import java.time.LocalDateTime
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

case class BookResource(
    id: String,
    link: String,
    title: String,
    release_date: LocalDateTime
)

object BookResource {
  implicit val format: Format[BookResource] = Json.format
}

class BookResourceHandler @Inject() (
    routerProvider: Provider[BookRouter],
    booksRepository: BookRepository
)(implicit ec: ExecutionContext) {

  def lookup(
      id: String
  )(implicit mc: MarkerContext): Future[Option[BookResource]] = {
    val booksFuture = booksRepository.get(BookId(id))
    booksFuture.map { maybeBookData =>
      maybeBookData match {
        case Some(book) => Some(createBookResource(book))
        case None       => None
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[BookResource]] = {
    booksRepository.list().map { booksDataList =>
      booksDataList.map(booksData => createBookResource(booksData))
    }
  }

  private def createBookResource(book: BookData): BookResource = {
    BookResource(
      book.book_id.toString,
      routerProvider.get.link(book.book_id),
      book.title,
      book.release_date
    )
  }

}
