package controllers

import javax.inject.Inject

import play.api.i18n._
import play.api.mvc._

class LanguageController @Inject()(val messagesApi: MessagesApi, langs: Langs) extends Controller with I18nSupport {

  def switchToLanguage(language: String) = Action { implicit request =>
    if (langs.availables.exists(_.code == language)) {
      Redirect(routes.HomeController.taskList()).withLang(Lang(language))
    } else {
      Redirect(routes.HomeController.taskList())
    }
  }

}