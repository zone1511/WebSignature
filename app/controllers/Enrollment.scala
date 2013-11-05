package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.SignatureModel
import be.ac.ulg.montefiore.run.jahmm._
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object Enrollment extends Controller {

  def enrollmentForm = Action {
    Logger.info("Enrollment form displayed.")
    Ok(views.html.enrollment())
  }
  
  var signatures = ListBuffer[ListBuffer[Array[Double]]]()

  var model : SignatureModel = null

  type Position = (Double, Double, Double)

  implicit val positionReads : Reads[Position] = (
    (__ \ 'x).read[Double] and
    (__ \ 'y).read[Double] and
    (__ \ 't).read[Double]
  ) tupled

  implicit val signatureReads = (
    (__ \ 'name).read[String] and
    (__ \ 'signature).read[ListBuffer[Position]]
  ) tupled

  def addEnrollmentSignature = Action(parse.json) { implicit request =>
    Logger.info("Add enrollment signature")
    request.body.validate[(String, ListBuffer[Position])].map{ 
      case (name, signature) => {
        val signArray : ListBuffer[Array[Double]] = signature.map(x => Array(x._1, x._2))
        //signatures ::= signArray
        //signatures = ListBuffer[ListBuffer[Array[Double]]]()
        signatures += signArray

        Ok(
        "Hello " + name +
        ", number of sampled points : "+signArray.size +
        ", number of signatures : "+signature).as("text/html");
      }
    }.recoverTotal{
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))
    }
  }

  def probaSignature = Action(parse.json) { implicit request =>
    Logger.info("Test probab")
    request.body.validate[(String, ListBuffer[Position])].map{ 
      case (name, signature) => {
        val signArray : ListBuffer[Array[Double]] = signature.map(x => Array(x._1, x._2))
        //signatures ::= signArray
        val aSign : java.util.List[Array[Double]] = new java.util.LinkedList(signArray.map(y => y.map(z => z)))
        val probab = model.probability(aSign)

        Ok(
        "Hello " + name +
        ", number of sampled points : "+probab).as("text/html");
      }
    }.recoverTotal{
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))
    }
  }

  def enroll = Action {
    Logger.info("Enrolling ...")
    //val aSign : java.util.List[java.util.List[Array[Double]]] = new java.util.LinkedList(signatures.map(x => new java.util.LinkedList(x.map(y => new Array[java.lang.Double](y)))))

    val aSign : java.util.List[java.util.List[Array[Double]]] = new java.util.LinkedList(signatures.map(x => new java.util.LinkedList(x.map(y => y.map(z => z)))))
    model = new SignatureModel(aSign)
    Ok("Hi !")
  }

}