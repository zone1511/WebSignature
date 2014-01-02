package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Enrollment.addEnrollmentSignature,
        routes.javascript.Enrollment.enroll,
        routes.javascript.Enrollment.probaSignature
      )).as("text/javascript")
  }
}