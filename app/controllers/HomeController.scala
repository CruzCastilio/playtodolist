package controllers

import javax.inject._

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

@Singleton
class HomeController @Inject()(taskDao: TaskDao, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def index = Action {
    Redirect(routes.HomeController.taskList())
  }

  def taskList() = Action {
    Ok(views.html.tasklist(taskDao.all()))
  }

  /*
    def postTask = Action { implicit request =>
      taskForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.taskedit(taskDao.all(), formWithErrors)),
        task => {
          taskDao.create(task.label, task.assigner)
          Redirect(routes.HomeController.taskList())
        }
      )
    }
  */
  def taskEdit(id: Long) = Action { implicit request =>
    taskDao.findById(id) match {
      case obj@Some(task) => Ok(views.html.taskedit(obj, taskForm))
      case None => BadRequest("Not Found")
    }
  }

  def taskNew = ???

  def deleteTask(id: Long) = Action { implicit request =>
    taskDao.delete(id)
    Redirect(routes.HomeController.taskList())
  }

  val taskForm = Form(
    mapping(
      "label" -> nonEmptyText,
      "task" -> nonEmptyText,
      "creationDate" -> date,
      "expirationDate" -> date,
      "assigner" -> nonEmptyText,
      "executor" -> nonEmptyText
    )(TaskForm.apply)(TaskForm.unapply)
  )
}
