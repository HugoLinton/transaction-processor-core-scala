package com.hugolinton.utils

/**
  * Created by hugol on 31/05/2017.
  */
object MapUtils {


  def printMap(values : Map [String, Double]) = {
    val sortedValues = values.toSeq.sortBy(_._1)
    sortedValues.foreach(value => println(value.toString()))
  }

}
