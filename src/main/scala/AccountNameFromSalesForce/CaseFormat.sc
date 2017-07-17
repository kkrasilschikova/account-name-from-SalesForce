case class VeeamCase(number: String)
val veeamCaseFormat="[0-9]{8}"

def getListOfCasesFromFile(filepath: String, column: Int): List[VeeamCase] = {
  val sourcefile = io.Source.fromFile(filepath)
  val listOfCasesInColumn = sourcefile.getLines()
    .map(line => line.split(",")(column).replace("\"", ""))
    .toList

  def checkCaseNumberFormat(in: List[String], acc: List[VeeamCase]): List[VeeamCase] = {
    in match {
      case Nil => acc
      case head :: tail =>if (head matches veeamCaseFormat)
        checkCaseNumberFormat(tail, VeeamCase(head) :: acc)
      else checkCaseNumberFormat(tail, acc)
    }
  }

  sourcefile.close
  checkCaseNumberFormat(listOfCasesInColumn, List(): List[VeeamCase])

}

//res2: List[VeeamCase] = List(VeeamCase(02166239), VeeamCase(02164979), VeeamCase(02160547), VeeamCase(02157391), VeeamCase(02164770))