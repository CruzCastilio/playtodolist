package controllers

import java.util.Date
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

  def taskList = Action {
    Ok(views.html.tasklist(taskDao.all()))
  }


  def taskPost = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      formWithErrors => BadRequest("WRONG!!!"),
      task => {
        taskDao.create(task.label, task.task, new Date(), task.expirationDate, task.assigner, task.executor)
        Redirect(routes.HomeController.taskList)
      }
    )
  }

  def taskEdit(id: Long) = Action { implicit request =>
    taskDao.findById(id) match {
      case Some(found) =>
        taskForm.bindFromRequest.fold(
          formWithErrors => BadRequest("WRONG!!!"),
          task => {
            taskDao.edit(id, task.label, task.task, task.expirationDate, task.assigner, task.executor)
            Redirect(routes.HomeController.taskList)
          }
        )
      case None => BadRequest("Not Found")
    }
  }

  def taskDetails(id: Long) = Action { implicit request =>
    taskDao.findById(id) match {
      case obj @ Some(task) => Ok(views.html.taskedit(Some((task.id, task.creationDate)),taskForm.fill(TaskForm(
        task.label, task.task, task.expirationDate, task.assigner, task.executor))))
      case None => BadRequest("Not Found")
    }
  }

  def taskNew = Action {
    Ok(views.html.taskedit(None, taskForm))
  }

  def taskDelete(id: Long) = Action { implicit request =>
    taskDao.delete(id)
    Redirect(routes.HomeController.taskList())
  }

  val taskForm = Form(
    mapping(
      "label" -> nonEmptyText,
      "task" -> nonEmptyText,
      "expirationDate" -> date,
      "assigner" -> nonEmptyText,
      "executor" -> nonEmptyText
    )(TaskForm.apply)(TaskForm.unapply)
  )
}
