package edu.neu.coe.scala.retrieval

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{HttpClientBuilder}
import com.github.nscala_time.time.Imports._
import com.github.nscala_time.time._

/**
  * Created by Team2 on 3/26/2017.
  * reference:
  * https://dev.twitter.com/rest/public
  * https://github.com/nscala-time/nscala-time
  */

object TwitterClient {

  val ConsumerKey = "yg3NK1BMfLx6dEE7UenZMMGiW"
  val ConsumerSecret = "MFwnagcHiyQMvkETkiZ613ngiWkKAKXN0lHVq6bW4g687G20R9"
  val AccessToken = "708481334482698240-QTn0EaokD6IVWFH0ZUhzlW48rdl42Qt"
  val AccessSecret = "7XfA0v9j0utKeUuf44n2YEB3AtzqVlMM0ue4IrJC0v2cK"

  def getFromSearchApiByKeyword(k: String, count: Int = 90): String = {
    val today= DateTime.now
    //println(today.toString(StaticDateTimeFormat.forPattern("yyyy-MM-dd")))
    val ss = for (i <- 1 to 7) yield getFromSearchApiByKeywordForOneDay(today-i.days,k,count)
    ss.mkString
  }

  def getFromSearchApiByKeywordForOneDay(i: DateTime,k: String, count: Int): String = {
    val consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret)
    consumer.setTokenWithSecret(AccessToken, AccessSecret)
    val url = "https://api.twitter.com/1.1/search/tweets.json?q=" + k + "&count=" + count + "&until=" + i.toString(StaticDateTimeFormat.forPattern("yyyy-MM-dd"))
    //println(url)
    val request = new HttpGet(url)
    consumer.sign(request)
    val client = HttpClientBuilder.create().build()
    val response = client.execute(request)
    IOUtils.toString(response.getEntity().getContent()) + "\n"
  }

  def main(args: Array[String]) {

    if (args.size == 0) println("Please refer to README.md in our repo for input parameter.") else {
      val keyword = if (args.size > 1) args.tail.mkString(" ") else ""

      args(0) match {
        case "hashtags" => Usecases.popularHashTags(keyword)
        case "map" => Usecases.popularLocations(keyword)
        case "weather" => runWeather(keyword)
        case "stock" => runStock(keyword)
        case _ => println("Invalid input. Please refer to README.md in our repo for input parameter.")
      }
    }
  }

  def runWeather(k: String) = {
    k match {
      case "" =>
        val bosScore = Usecases.calcSentimentFromSearchApi("Boston weather")
        println("Boston vs Seattle:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("Seattle weather")))
        println("Boston vs Houston:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("Houston weather")))
        println("Boston vs San Francisco:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("San Francisco weather")))
        println("Boston vs New York:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("New York weather")))
        println("Boston vs Miami:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("Miami weather")))
        println("Boston vs Chicago:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("Chicago weather")))
        println("Boston vs Denver:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("Denver weather")))
        println("Boston vs Los Angeles:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("Los Angeles weather")))
        println("Boston vs Phoenix:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("Phoenix weather")))
        println("Boston vs Atlanta:" + Usecases.compareSentiment(bosScore, Usecases.calcSentimentFromSearchApi("Atlanta weather")))
      case _ =>
        println(k+" weather sentiment score:"+Usecases.calcSentimentFromSearchApi(k+" weather"))
    }
  }

  def runStock(k: String) = {
    k match {
      case "" =>
        val aaplScore = Usecases.calcSentimentFromSearchApi("Apple Inc stock")
        println("Apple Inc vs GNC:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("GNC stock")))
        println("Apple Inc vs Netflix:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("Netflix stock")))
        println("Apple Inc vs Bank of America:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("Bank of America stock")))
        println("Apple Inc vs Comcast:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("Comcast stock")))
        println("Apple Inc vs Wells Fargo:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("Wells Fargo stock")))
        println("Apple Inc vs Verizon:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("Verizon stock")))
        println("Apple Inc vs Citi:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("Citi stock")))
        println("Apple Inc vs ATT:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("ATT stock")))
        println("Apple Inc vs United Continental:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("United Continental stock")))
        println("Apple Inc vs Delta Air Lines:" + Usecases.compareSentiment(aaplScore, Usecases.calcSentimentFromSearchApi("Delta Air Lines stock")))
      case _ =>
        println(k+" stock sentiment score:"+Usecases.calcSentimentFromSearchApi(k + " stock"))
    }
  }
}
