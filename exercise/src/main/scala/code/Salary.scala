package code

import java.time.LocalDate

case class Salary(
  employeeNumber: EmployeeNumber,
  amount: Int,
  fromDate: LocalDate,
  toDate: LocalDate,
)
