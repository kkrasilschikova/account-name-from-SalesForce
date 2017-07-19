package AccountNameFromSalesForce.model

case class VeeamCase(number: String)
object validateCaseFormat{
  def apply(number: String): Option[VeeamCase]=
    if (number matches "[0-9]{8}") Some(VeeamCase(number)) else None
}