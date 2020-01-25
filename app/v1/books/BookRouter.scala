package v1.books

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class BookRouter @Inject() (controller: BookController) extends SimpleRouter {
  val prefix = "/v1/books"

  def link(id: BookId): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case GET(p"/$id") =>
      controller.show(id)
  }
}
