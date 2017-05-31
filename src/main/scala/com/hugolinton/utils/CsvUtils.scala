package com.hugolinton.utils

import com.hugolinton.model.Transaction

import scala.io.Source

/**
  * Created by hugol on 31/05/2017.
  */
object CsvUtils {

  def getTransactions(path : String) : List[Transaction]= {
    val transactionsLines = Source.fromFile(path).getLines().drop(1)

    transactionsLines.map { line =>
      val split = line.split(',')
      Transaction(split(0), split(1), split(2).toInt, split(3), split(4).toDouble)
    }.toList
  }

}
