package controllers

import java.util.Date
import javax.inject._
import helpers.Hashing._
import helpers.SecurityApi
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

@Singleton
class HomeController @Inject()(userDao: UserDao, taskDao: TaskDao, val messagesApi: MessagesApi) extends Controller with SecurityApi with I18nSupport {

  def userLogin = withLog { implicit request =>
    logger.info("User Login Attempt")
    userForm.bindFromRequest.fold(
      formWithErrors => {
        logger.info("Got Form Errors While Login")
        BadRequest(views.html.errors(messagesApi("wrongUsernameOrPassword")))
      },
      userData => {
        userDao.findUserByName(userData.username) match {
          case Some(user) if md5Hash(userData.password + user.salt) == user.password =>
            logger.info("Successful User Login")
            Redirect(routes.HomeController.taskList()).withCookies(generateCookie(user))
          case _ =>
            logger.info("There Is No Such User Or Password In DB")
            BadRequest(views.html.errors(messagesApi("wrongUsernameOrPassword")))
        }
      }
    )
  }

  def userLogout = withAuthentication { user => implicit request =>
    request.cookies.find(_.name == cookieName) match {
      case Some(found) => sessions.get(found.value) match {
        case Some(x) =>
          sessions.remove(found.value)
          println(sessions)
          Redirect(routes.HomeController.index()).discardingCookies(DiscardingCookie(found.name))
        case None => BadRequest
  }}}

  def userCreate = withoutCookie { implicit request =>
    logger.info("New User Creation Attempt")
    userForm.bindFromRequest.fold(
      formWithErrors => {
        logger.info("Got Form Errors While New User Creation")
        BadRequest(views.html.usercreate(formWithErrors))
      },
      userData => {
        userDao.findUserByName(userData.username) match {
          case Some(user) =>
            logger.info(s"Such Username ${user.username} Already Exists In DB")
            BadRequest(views.html.errors(messagesApi("usernameExists")))
          case None =>
            logger.info("Successful User Creation And Logging In")
            Redirect(routes.HomeController.taskList()).withCookies(generateCookie(userDao.createUser(userData.username,
              userData.password))).flashing("success" -> messagesApi("userCreated"))
        }
      }
    )
  }

  def userNew = withoutCookie { implicit request =>
    logger.info("New User Creation Page Opened")
    Ok(views.html.usercreate(userForm))
  }

  val userForm = Form(
    mapping(
      "username" -> text.verifying(messagesApi("verificationUsername"), s => s.length >= 3 && s.length <= 30 && s.matches("[\\S]+")),
      "password" -> text.verifying(messagesApi("verificationPassword"), s => s.length >= 3 && s.length <= 30 && s.matches("[\\S]+"))
    )(UserForm.apply)(UserForm.unapply)
  )

  def index = withoutCookie { implicit request =>
    logger.info("Home Page")
    println(sessions)
    Ok(views.html.index(userForm))
  }

  def taskList = withAuthentication { user: User => implicit request =>
    logger.info("Task List Opening")
    println(sessions)
    Ok(views.html.tasklist(taskDao.all()))
  }

  def taskPost = withAuthentication { user: User => implicit request =>
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

  def taskEdit(id: Long) = withAuthentication { user: User => implicit request =>
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

  def taskDetails(id: Long) = withAuthentication { user: User => implicit request =>
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

  def taskNew = withAuthentication { user: User => implicit request =>
    logger.info("New Task Creation Page Opened")
    Ok(views.html.taskedit(None, taskForm))
  }

  def taskDelete(id: Long) = withAuthentication { user: User => implicit request =>
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
