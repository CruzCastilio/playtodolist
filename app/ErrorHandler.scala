import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, NOT_FOUND}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router

import scala.concurrent._

@Singleton
class ErrorHandler @Inject() (val messagesApi: MessagesApi)(
  env: Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: Provider[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with I18nSupport {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) =
    statusCode match {
      case BAD_REQUEST => Future.successful(BadRequest(views.html.errors(messagesApi("badRequestUpperCase"))))
      case FORBIDDEN => Future.successful(Forbidden(views.html.errors(messagesApi("forbidden"))))
      case NOT_FOUND => Future.successful(NotFound(views.html.errors(messagesApi("notFoundUpperCase"))))
      case clientError if statusCode >= 400 && statusCode < 500 =>
        Future.successful(Results.Status(clientError)(views.html.defaultpages.badRequest(request.method, request.uri, message)))
      case nonClientError =>
        throw new IllegalArgumentException("onClientError " + messagesApi("nonClientError") + s" $statusCode: $message")
    }


  override def onServerError(request: RequestHeader, exception: Throwable) =
    Future.successful(
      InternalServerError(messagesApi("onServerError") + exception.getMessage)
    )
}