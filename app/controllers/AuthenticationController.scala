//package controllers
//
//import models.{User, UserDao}
//import play.api.mvc.{Action, Controller, RequestHeader, Result}
//
//trait AuthenticationController {
//  self: Controller =>
//  var accessConditions: List[Conditions.Condition] = List.empty
//  def authenticateMe(f: User => Result) = Action { implicit request =>
//    val user = AuthUtils.parseUserFromRequest
//    if (user.isEmpty)
//      Forbidden("Invalid username or password")
//    else {
//      accessConditions.map(condition => condition(user.get)).collectFirst[String]{case Left(error) => error}
//      match {
//        case Some(error) => Forbidden(s"Condition not met: $error")
//        case _ => f(user.get)
//      }
//    }
//  }
//}
//
//object Conditions {
//  type Condition = (User => Either[String, Unit])
//  def isAdmin: Condition = {
//    user => if (user.isAdmin)
//      Right()
//    else
//      Left("User must be admin")
//  }
//}
//
//trait AdminOnly {
//  self: AuthenticationController =>
//  accessConditions = accessConditions :+ Conditions.isAdmin
//}
//
//object AuthUtils {
//  def parseUserFromCookie(implicit request: RequestHeader) = request.session.get("username").flatMap(username => UserDao.findUser(username))
//
//  def parseUserFromQueryString(implicit request:RequestHeader) = {
//    val query = request.queryString.map { case (k, v) => k -> v.mkString }
//    val username = query get ("username")
//    val password = query get ("password")
//    (username, password) match {
//      case (Some(u), Some(p)) => UserDao.findUser(u).filter(user => user.checkPassword(p))
//      case _ => None
//    }
//  }
//
//  def parseUserFromRequest(implicit request:RequestHeader):Option[UserDao] = {
//    parseUserFromQueryString orElse  parseUserFromCookie
//  }
//}