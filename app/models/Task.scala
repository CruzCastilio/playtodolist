package models

import java.util.Date
import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import play.api.db._

case class Task(id: Long, label: String, task: String, creationDate: Date, expirationDate: Date,
                assigner: String, executor: String)

case class TaskForm(label: String, task: String, creationDate: Date, expirationDate: Date, assigner: String,
                    executor: String)

@Singleton
class DbEngine @Inject()(dbApi: DBApi) {
  val db: Database = dbApi.database("default")
}

@Singleton
class TaskDao @Inject()(engine: DbEngine) {

  val task: RowParser[Task] = {
    get[Long]("id") ~
      get[String]("label") ~
      get[String]("task") ~
      get[Date]("creationDate") ~
      get[Date]("expirationDate") ~
      get[String]("assigner") ~
      get[String]("executor") map {
      case id ~ label ~ task ~ creationDate ~ expirationDate ~ assigner ~ executor => Task(id, label, task, creationDate,
        expirationDate, assigner, executor)
    }
  }

  def all(): List[Task] = engine.db.withConnection { implicit c =>
    SQL("select * from task").as(task *)
  }

  def create(label: String, task: String, creationDate: String, expirationDate: String, assigner: String,
             executor: String): Int = {
    engine.db.withConnection { implicit c =>
      SQL("insert into task (label, task, creationDate, expirationDate, assigner, executor) values ({label, task, " +
        "creationDate, expirationDate, assigner, executor})").on(
        'label -> label,
        'task -> task,
        'creationDate -> creationDate,
        'expirationDate -> expirationDate,
        'assigner -> assigner,
        'executor -> executor
      ).executeUpdate()
    }
  }

  def delete(id: Long): Int = {
    engine.db.withConnection { implicit c =>
      SQL("delete from task where id = {id}").on('id -> id).executeUpdate()
    }
  }

  def findById(id: Long): Option[Task] = {
    engine.db.withConnection { implicit c =>
      SQL("select * from task where id = {id}").on('id -> id).as(task.singleOpt)
    }
  }
}
