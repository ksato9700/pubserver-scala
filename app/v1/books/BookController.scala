package v1.books

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class BookController @Inject() (cc: BookControllerComponents)(
    implicit ec: ExecutionContext
) extends BookBaseController(cc) {

  private val logger = Logger(getClass)

  def index: Action[AnyContent] = BookAction.async { implicit request =>
    logger.trace("index: ")
    bookResourceHandler.find.map { books =>
      Ok(Json.toJson(books))
    }
  }

  def show(id: String): Action[AnyContent] = BookAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      bookResourceHandler
        .lookup(id)
        .map { book =>
          Ok(Json.toJson(book))
        }
        .fallbackTo(Future { Ok("{}") })
  }

}
