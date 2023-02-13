package code

import enumeratum._

sealed abstract class Gender(override val entryName: String) extends EnumEntry

object Gender extends Enum[Gender] {
  case object Male extends Gender("m")
  case object Female extends Gender("f")
  val values: IndexedSeq[Gender] = findValues
}
