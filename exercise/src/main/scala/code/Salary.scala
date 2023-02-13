package code

import java.time.LocalDate

case class Salary(
  employeeNumber: Int,
  amount: Int,
  fromDate: LocalDate,
  toDate: LocalDate,
)
