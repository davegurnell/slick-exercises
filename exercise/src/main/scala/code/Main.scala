package code

import code.Main.store
import slick.dbio.Effect
import slick.jdbc
import slick.jdbc.MySQLProfile
import slick.jdbc.MySQLProfile.api._
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction, SqlAction}

import java.time.LocalDate
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  val store = new EmployeeStore(MySQLProfile)
  val database = Database.forConfig("database.docker_employees")

  def main(args: Array[String]): Unit = {
    val action = store.capitaliseNames

    val future = database.run(action)

    val result = Await.result(future, 60.seconds)

    println(result)

    /*
    select x2.`birth_date`, x2.`first_name`, x2.`last_name`, x2.`gender`, x2.`hire_date`, x2.`emp_no`, x3.`emp_no`, x3.`salary`, x3.`from_date`, x3.`to_date`
    from `employees` x2, `salaries` x3
    where x2.`emp_no` = x3.`emp_no`
    limit 10

     */
  }
}
