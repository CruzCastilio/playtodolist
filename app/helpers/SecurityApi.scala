package helpers

import controllers.routes
import helpers.Hashing._
import models.User
import org.apache.logging.log4j.scala.Logging
import play.api.mvc._

import scala.collection.mutable

trait SecurityApi extends Logging { this: Controller =>
  val sessions: mutable.HashMap[String, User] = mutable.HashMap.empty
  val cookieName = "TODO-TOKEN"

  def withAuthentication(f: User => Request[AnyContent] => Result): Action[AnyContent] = withLog { implicit request =>
    request.cookies.find(_.name == cookieName) match {
      case Some(found) => sessions.get(found.value) match {
        case Some(user) => f(user)(request)
        case None => Redirect(routes.HomeController.index()).discardingCookies(DiscardingCookie(found.name))
      }
      case None => Redirect(routes.HomeController.index())
    }
  }

  def withLog(f: Request[AnyContent] => Result): Action[AnyContent] = Action { request =>
    val start = System.currentTimeMillis()
    val result = f(request)
    val finish = System.currentTimeMillis() - start
    logger.info(s"${request.uri} Processing Time: $finish ms")
    result
  }

  def withoutCookie(f: Request[AnyContent] => Result) = withLog { implicit request =>
    request.cookies.find(_.name == cookieName) match {
      case Some(_) => Redirect(routes.HomeController.taskList())
      case None => f(request)
    }
  }

  def generateCookie(user: User): Cookie = {
    val cookie = md5Hash(user.username + System.currentTimeMillis().toString)
    sessions.put(cookie, user)
    Cookie(cookieName, cookie)
  }
}

object Hashing {
  def md5Hash(text: String): String =
    java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map { "%02x".format(_) }.mkString
}
