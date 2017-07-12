package AccountNameFromSalesForce

import com.sforce.async._
import java.io.ByteArrayInputStream
import java.util.Calendar

import scala.util.{Failure, Success, Try}

object SFQuery {
  def prepareJob(establishedConnection: BulkConnection): JobInfo = {
    val job = new JobInfo
    job.setObject("Case")
    job.setOperation(OperationEnum.query)
    job.setConcurrencyMode(ConcurrencyMode.Parallel)
    job.setContentType(ContentType.CSV)
    job
  }

  def tryJobCreation(establishedConnection: BulkConnection, preparedJob: JobInfo): Try[JobInfo]={
    Try(establishedConnection.createJob(preparedJob)) match {
      case Success(cJ) => Success(cJ)
      case Failure(ex) => println("Please modify the job settings.")
        Failure(ex)
    }
  }

  def createJob(establishedConnection: BulkConnection, triedJob: Try[JobInfo]): JobInfo = {
    val createdJob = triedJob.get
    assert(createdJob.getId != null, "Job was not created")
    establishedConnection.getJobStatus(createdJob.getId)
  }

  case class customException(ex: String) extends Exception(ex)

  def queryString(listOfCases: List[String]): String = {
    if ((listOfCases.size > 9999)|| (listOfCases.size < 1))
      throw customException("SalesForce batch inconsistent, please change batch size and check source input data.")

    s"""
    SELECT CaseNumber, Account.Name
    FROM Case
    WHERE CaseNumber IN
    (${
      listOfCases
        .map(number => s"'$number'")
        .mkString(",")
    })"""
  }

  def tryBatchCreation(establishedConnection: BulkConnection, createdJob: JobInfo, preparedQuery: String): Try[BatchInfo]={
    val bout = new ByteArrayInputStream(preparedQuery.getBytes)
    Try (establishedConnection.createBatchFromStream(createdJob, bout))
    match{
      case Success(info)=>Success(info)

      case Failure(ex)=>Failure(ex)
    }
  }

  def createBatch(establishedConnection: BulkConnection, createdJob: JobInfo, triedBatch: Try[BatchInfo]): Option[BatchInfo] = {
    val createdBatch = establishedConnection.getBatchInfo(createdJob.getId, triedBatch.get.getId)
    val startTime=Calendar.getInstance.getTime

    def checkBatchState(createdBatch: BatchInfo, diff: Long): Option[BatchInfo]= {
      createdBatch.getState match {
        case BatchStateEnum.Completed =>
          Some(createdBatch)
        case BatchStateEnum.Failed =>
          println(s"${createdBatch.getStateMessage}")
          None
        case _ =>
          println(s"Waiting for batch...\n")
          Thread.sleep(20000)
          if (diff<70000) checkBatchState(createdBatch, Calendar.getInstance.getTime.getTime-startTime.getTime)
          else println("Waited for batch too long.")
          None
      }
    }

    checkBatchState(createdBatch, Calendar.getInstance.getTime.getTime-startTime.getTime)
  }


  def getQueryResults(establishedConnection: BulkConnection, createdJob: JobInfo, createdBatch: Option[BatchInfo]): Option[List[String]] = {
    createdBatch match {
      case Some(cB) =>
        val queryResults = establishedConnection
          .getQueryResultList(createdJob.getId, cB.getId).getResult
        queryResults match {
          case null => None
          case _ =>
            Some(queryResults
              .map(resultId =>
                scala.io.Source.fromInputStream(establishedConnection.getQueryResultStream(createdJob.getId, cB.getId, resultId)))
              .flatMap(x => x.getLines())
              .toList)
        }
      case None => None
    }
  }

  def closeJob(createdJob: JobInfo, establishedConnection: BulkConnection): Try[JobInfo] = {
    createdJob.setState(JobStateEnum.Closed)
    Try(establishedConnection.closeJob(createdJob.getId)) match {
      case Success(v) => Success(v)
      case Failure(msg) => Failure(msg)
    }
  }
}
