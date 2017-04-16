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

import edu.neu.coe.scala.sentiment._

/**
  * Created by Mushtaq on 3/26/2017.
  */

object TwitterClient {

  val ConsumerKey = "yg3NK1BMfLx6dEE7UenZMMGiW"
  val ConsumerSecret = "MFwnagcHiyQMvkETkiZ613ngiWkKAKXN0lHVq6bW4g687G20R9"
  val AccessToken = "708481334482698240-QTn0EaokD6IVWFH0ZUhzlW48rdl42Qt"
  val AccessSecret = "7XfA0v9j0utKeUuf44n2YEB3AtzqVlMM0ue4IrJC0v2cK"

  def getFromSearchApiByKeyword(k: String, count: Int = 90): InputStream = {
    val consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret)
    consumer.setTokenWithSecret(AccessToken, AccessSecret)
    val url = "https://api.twitter.com/1.1/search/tweets.json?q=" + k + "&count=" + count
    val request = new HttpGet(url)
    consumer.sign(request)
    val client = HttpClientBuilder.create().build()
    val response = client.execute(request)
    response.getEntity().getContent()
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
    Usecases.popularLocations()
  }
}
