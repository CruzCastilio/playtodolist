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

  def taskList = Action { implicit request =>
    Ok(views.html.tasklist(taskDao.all()))
  }

  def taskPost = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.taskedit(None, formWithErrors)),
      task => {
        taskDao.create(task.label, task.task, new Date(), task.expirationDate, task.assigner, task.executor)
        Redirect(routes.HomeController.taskList()).flashing("success" -> messagesApi("flashCreated"))
      }
    )
  }

  def taskEdit(id: Long) = Action { implicit request =>
    taskDao.findById(id) match {
      case Some(found) =>
        taskForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.taskedit(Some((found.id, found.creationDate)), formWithErrors)),
          task => {
            taskDao.edit(id, task.label, task.task, task.expirationDate, task.assigner, task.executor)
            Redirect(routes.HomeController.taskList()).flashing("success" -> messagesApi("flashEdited"))
          }
        )
      case None => BadRequest
    }
  }

  def taskDetails(id: Long) = Action { implicit request =>
    taskDao.findById(id) match {
      case Some(task) => Ok(views.html.taskedit(Some((task.id, task.creationDate)), taskForm.fill(TaskForm(
        task.label, task.task, task.expirationDate, task.assigner, task.executor))))
      case None => BadRequest(views.html.errors(messagesApi("wrongId")))
    }
  }

  def taskNew = Action { implicit request =>
    Ok(views.html.taskedit(None, taskForm))
  }

  def taskDelete(id: Long) = Action { implicit request =>
    taskDao.delete(id)
    Redirect(routes.HomeController.taskList()).flashing("success" -> messagesApi("flashDeleted"))
  }

  val taskForm = Form(
    mapping(
      "label" -> text.verifying(messagesApi("verificationLabel"), s => s.length >= 3 && s.length <= 30),
      "task" -> text.verifying(messagesApi("verificationTask"), _.trim.nonEmpty),
      "expirationDate" -> date.verifying(messagesApi("verificationExpirationDate"), _.after(new Date())),
      "assigner" -> text.verifying(messagesApi("verificationAssigner"), s => s.length >= 2 && s.length <= 15),
      "executor" -> text.verifying(messagesApi("verificationExecutor"), s => s.length >= 2 && s.length <= 15)
    )(TaskForm.apply)(TaskForm.unapply)
  )

}
