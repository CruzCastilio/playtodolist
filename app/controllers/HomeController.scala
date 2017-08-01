package controllers

import java.util.Date
import javax.inject._

import models._
import org.apache.logging.log4j.scala.Logging
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

@Singleton
class HomeController @Inject()(taskDao: TaskDao, val messagesApi: MessagesApi) extends Controller with I18nSupport with Logging {

  def withLog(f: Request[AnyContent] => Result): Action[AnyContent] = {
    val start = System.currentTimeMillis()
    val res = Action { request => f(request) }
    val finish = System.currentTimeMillis() - start
    logger.info(s"Request Processing Time: $finish ms")
    res
  }

  def index = withLog { implicit request =>
    logger.info("Home Page Attempt")
    Redirect(routes.HomeController.taskList())
  }

  def taskList = withLog { implicit request =>
    logger.info("Task List Opening")
    Ok(views.html.tasklist(taskDao.all()))
  }

  def taskPost = withLog { implicit request =>
    logger.info("New Task Creating Attempt")
    taskForm.bindFromRequest.fold(
      formWithErrors => {
        logger.info("Got Form Errors While New Task Creation")
        BadRequest(views.html.taskedit(None, formWithErrors))
      },
      task => {
        logger.info("Successful New Task Creation")
        taskDao.create(task.label, task.task, new Date(), task.expirationDate, task.assigner, task.executor)
        Redirect(routes.HomeController.taskList()).flashing("success" -> messagesApi("flashCreated"))
      }
    )
  }

  def taskEdit(id: Long) = withLog { implicit request =>
    logger.info("Task ID Searching Attempt")
    taskDao.findById(id) match {
      case Some(found) =>
        logger.info("Task ID Found; Task Editing Attempt")
        taskForm.bindFromRequest.fold(
          formWithErrors => {
            logger.info("Got Form Errors While Task Editing")
            BadRequest(views.html.taskedit(Some((found.id, found.creationDate)), formWithErrors))
          },
          task => {
            logger.info("Successful Task Editing")
            taskDao.edit(id, task.label, task.task, task.expirationDate, task.assigner, task.executor)
            Redirect(routes.HomeController.taskList()).flashing("success" -> messagesApi("flashEdited"))
          }
        )
      case None =>
        logger.info("Task ID Not Found")
        BadRequest(views.html.errors(messagesApi("wrongId")))
    }
  }

  def taskDetails(id: Long) = withLog { implicit request =>
    logger.info("Task ID Searching Attempt")
    taskDao.findById(id) match {
      case Some(task) =>
        logger.info("Task Details Opened")
        Ok(views.html.taskedit(Some((task.id, task.creationDate)), taskForm.fill(TaskForm(
          task.label, task.task, task.expirationDate, task.assigner, task.executor))))
      case None =>
        logger.info("Task ID Not Found")
        BadRequest(views.html.errors(messagesApi("wrongId")))
    }
  }

  def taskNew = withLog { implicit request =>
    logger.info("New Task Creation Page Opened")
    Ok(views.html.taskedit(None, taskForm))
  }

  def taskDelete(id: Long) = withLog { implicit request =>
    logger.info("Successful Task Deleting")
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
