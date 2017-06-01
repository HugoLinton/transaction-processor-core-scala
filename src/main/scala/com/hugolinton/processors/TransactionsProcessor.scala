package com.hugolinton.processors

import com.hugolinton.model.{ReportItem, Transaction}
import com.hugolinton.utils.{CsvUtils, MapUtils}

import scala.io.Source

/**
  * Created by hugol on 31/05/2017.
  */
object TransactionsProcessor {

  def main(args: Array[String]): Unit = {

    //Originally had a main method to load in any CSV File Path but changed to resources to keep things tidy
//    if(args.length != 1){
//      Console.err.println("Usage: ParquetQuery <CSV File Path>!")
//      System.exit(1)
//    }
//
//    val Array(csvFilePath) = args

    //Gets path of file from the resource folder
    val filePath = getClass.getResource("/transactions.txt").getPath

    println("Calculate the Total Transactions per day")
    val transactions = CsvUtils.getTransactions(filePath)
    getTransactionsGroupedByDay(transactions)


    println("Calculate the average value of transactions per account for each type of transaction")
    getAccountTransactionsAverage(transactions)

    println("Create a Daily Report Per Account")
    getDailyReports(transactions).foreach(report => println(ReportItem.getReportString(report)))
  }

  def getTransactionsGroupedByDay( transactions : List[Transaction]) = {
    // groupBy returns the data in a tuple with the value and list of the relevant data
    val results = transactions.groupBy(trans => trans.transactionDay).map(x => (x._1.toString,x._2.map(_.transactionAmount).sum))
    MapUtils.printMap(results)
    results
  }

  def getAccountTransactionsAverage( transactions : List[Transaction]) = {
    //Split the list by two groups, firstly account to collect all data per account
    val groupedByAccount = transactions.groupBy(trans => trans.accountId)
    groupedByAccount.map(account => {
      //Split the data on categories and perform the calculation
      val categories = account._2.groupBy(x => x.category).map(x => (x._1,x._2.map(_.transactionAmount).sum / x._2.size))
      println("--------- " + account._1 + " ---------")
      MapUtils.printMap(categories)
      (account._1, categories)
    })
  }

  def getDailyReports( transactions : List[Transaction]) = {
    //Group by account
    val groupedByAccount = transactions.groupBy(trans => trans.accountId)
    groupedByAccount.flatMap(account => {
      //Group by days to easily find the valid days needed to be reported on
      val days = account._2.sortBy(-_.transactionDay)
      //Create Range so we create a report for all days even if there are no reports on that day
      val maxDay = transactions.reduceLeft(ReportItem.maxTransactionDay).transactionDay + 1
      val dayRange = List.range(1,maxDay)
      dayRange.map(day => createDayReport(days,day))
    }).toSeq.sortBy(_.day)
  }

  def createDayReport(transactions : List[Transaction], reportDay : Int) = {
    //Filter so we only have the days we are interested in
    val results = transactions.filter(day => transactions.head.transactionDay - 5 < day.transactionDay && reportDay != day.transactionDay)
    //Maps results to case class ReportItem which has the same fields shown in the question
    getReport(results,reportDay)
  }

  def removeDuplicates(transactions : List[Transaction], duplicate : Transaction) : List[Transaction] = {
    transactions.filter(day => day.transactionDay != duplicate.transactionDay)
  }

  def getReport(transactions : List[Transaction], day : Int) : ReportItem = {
    val accountId = transactions.head.accountId
    //Finds the obj with the highest value using the function provided
    val maxTransaction = transactions.reduceLeft(ReportItem.maxTransactionAmount).transactionAmount
    val avg = transactions.map(_.transactionAmount).sum / transactions.size
    //Group By Catregory to easily calculate the total
    val categories = transactions.groupBy(x => x.category).filter(category => category._1 == "AA"
      || category._1 == "CC"
      || category._1 == "FF").map(x => (x._1, x._2.map(_.transactionAmount).sum))

    ReportItem(day,accountId,maxTransaction,avg,categories.get("AA"),categories.get("CC"),categories.get("FF"))
  }

}
