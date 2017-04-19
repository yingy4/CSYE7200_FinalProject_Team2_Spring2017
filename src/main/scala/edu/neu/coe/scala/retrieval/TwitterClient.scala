package edu.neu.coe.scala.retrieval

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{DefaultHttpClient, HttpClientBuilder}
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import twitter4j.Twitter
import java.io.{InputStream, PrintWriter}

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import java.util.Calendar
import edu.neu.coe.scala.sentiment._

/**
  * Created by Mushtaq on 3/26/2017.
  */

object TwitterClient {

  val ConsumerKey = "yg3NK1BMfLx6dEE7UenZMMGiW"
  val ConsumerSecret = "MFwnagcHiyQMvkETkiZ613ngiWkKAKXN0lHVq6bW4g687G20R9"
  val AccessToken = "708481334482698240-QTn0EaokD6IVWFH0ZUhzlW48rdl42Qt"
  val AccessSecret = "7XfA0v9j0utKeUuf44n2YEB3AtzqVlMM0ue4IrJC0v2cK"

  def getFromSearchApiByKeyword(k: String, count: Int = 90): String = {
    val tweet = new StringBuilder()
    val now = Calendar.getInstance()
    val today = now.get(Calendar.DATE)
    //println(today)
    for(i <- today-7 to today-1) {
    val consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret)
    consumer.setTokenWithSecret(AccessToken, AccessSecret)
    val url = "https://api.twitter.com/1.1/search/tweets.json?q=" + k + "&count=" + count + "&until=2017-04-" + i
    //println(url)
    val request = new HttpGet(url)
    consumer.sign(request)
    val client = HttpClientBuilder.create().build()
    val response = client.execute(request)
   // response.getEntity().getContent()
    var tweet_string = IOUtils.toString(response.getEntity().getContent())
    tweet ++= tweet_string + "\n"
    }
    tweet.toString()
  }

  def main(args: Array[String]) {
/*
    val consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret)
    consumer.setTokenWithSecret(AccessToken, AccessSecret)
    val request = new HttpGet("https://api.twitter.com/1.1/search/tweets.json?q=Northeastern%20University")
    consumer.sign(request)
    val client = HttpClientBuilder.create().build()
    val response = client.execute(request)

    println(response.getStatusLine().getStatusCode())

    var tweet_string = IOUtils.toString(response.getEntity().getContent())
    println(tweet_string)

    new PrintWriter("searchapi_sample1.json") { write(tweet_string); close }
*/
//    sparkTestRun
    //Usecases.popularHashTags("Trump")
    //Usecases.popularHashTags()
    //Usecases.popularLocations("London")
    //Usecases.popularLocations()
    /*
    val bosScore = Usecases.calcSentimentFromSearchApi("Boston weather")
    println("Boston vs Seattle:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("Seattle weather")))
    println("Boston vs Houston:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("Houston weather")))
    println("Boston vs San Francisco:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("San Francisco weather")))
    println("Boston vs New York:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("New York weather")))
    println("Boston vs Miami:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("Miami weather")))
    println("Boston vs Chicago:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("Chicago weather")))
    println("Boston vs Denver:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("Denver weather")))
    println("Boston vs Los Angeles:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("Los Angeles weather")))
    println("Boston vs Phoenix:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("Phoenix weather")))
    println("Boston vs Atlanta:"+Usecases.compareSentiment(bosScore,Usecases.calcSentimentFromSearchApi("Atlanta weather")))
    */
    val aaplScore = Usecases.calcSentimentFromSearchApi("Apple Inc stock")
    println("Apple Inc vs GNC:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("GNC stock")))
    println("Apple Inc vs Netflix:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("Netflix stock")))
    println("Apple Inc vs Bank of America:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("Bank of America stock")))
    println("Apple Inc vs Comcast:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("Comcast stock")))
    println("Apple Inc vs Wells Fargo:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("Wells Fargo stock")))
    println("Apple Inc vs Verizon:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("Verizon stock")))
    println("Apple Inc vs Citi:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("Citi stock")))
    println("Apple Inc vs ATT:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("ATT stock")))
    println("Apple Inc vs United Continental:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("United Continental stock")))
    println("Apple Inc vs Delta Air Lines:"+Usecases.compareSentiment(aaplScore,Usecases.calcSentimentFromSearchApi("Delta Air Lines stock")))
  }
}
