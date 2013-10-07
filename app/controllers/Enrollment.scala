package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Enrollment extends Controller {

  def enrollmentForm = Action {
    Logger.info("Enrollment form displayed.")
    Ok(views.html.enrollment())
  }

  type Position = (Long, Long, Long)

  implicit val positionReads : Reads[Position] = (
    (__ \ 'x).read[Long] and
    (__ \ 'y).read[Long] and
    (__ \ 't).read[Long]
    tupled
  )

  implicit val signatureReads = (
    (__ \ 'name).read[String] and
    (__ \ 'signature).read[List[Position]]
  ) tupled

  def enroll = Action(parse.json) { implicit request =>
    Logger.info("Enrollment request")
    request.body.validate[(String, List[Position])].map{ 
      case (name, signature) => Ok(
        "Hello " + name +
        ", number of sampled points : "+signature.size +
        ", starting points : "+signature.toString).as("text/html");
    }.recoverTotal{
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))
    }
  }
}