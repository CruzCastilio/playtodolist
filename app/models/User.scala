package models

import javax.inject.Inject
import helpers.Hashing._
import anorm.SqlParser.get
import anorm.{RowParser, SQL, ~}

case class UserForm(username: String, password: String)

case class User(id: Long, username: String, password: String, isAdmin: Boolean, salt: String)

class UserDao @Inject()(engine: DbEngine) {
  val user: RowParser[User] = {
    get[Long]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[Boolean]("isAdmin") ~
    get[String]("salt") map {
      case id ~ username ~ password ~ isAdmin ~ salt => User(id, username, password, isAdmin, salt)
    }
  }

  def findUser(username: String, password: String): Option[User] = {
    engine.db.withConnection { implicit c =>
      SQL("select * from users where username = {username}, password = {password}").on('username -> username, 'password -> password).as(user.singleOpt)
    }
  }

  def findUserByName(username: String): Option[User] = {
    engine.db.withConnection { implicit c =>
      SQL("select * from users where username = {username}").on('username -> username).as(user.singleOpt)
    }
  }

  def createUser(username: String, password: String): User = {
    val salt = System.currentTimeMillis().toString
    val saltPassword = md5Hash(password + salt)
    engine.db.withConnection { implicit c =>
      SQL("insert into users (username, password, isAdmin, salt) values ({username}, {password}, {isAdmin}, {salt})").on(
        'username -> username,
        'password -> saltPassword,
        'isAdmin -> false,
        'salt -> salt
      ).executeUpdate()
    }
    engine.db.withConnection { implicit c =>
      SQL("select * from users where username = {username}").on('username -> username).as(user.single)
    }
  }
}