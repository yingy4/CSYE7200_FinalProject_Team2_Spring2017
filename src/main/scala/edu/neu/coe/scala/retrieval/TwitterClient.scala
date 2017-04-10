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

  def getFromSearchApiByKeyword(k: String): InputStream = {
    val consumer = new CommonsHttpOAuthConsumer(ConsumerKey, ConsumerSecret)
    consumer.setTokenWithSecret(AccessToken, AccessSecret)
    val request = new HttpGet("https://api.twitter.com/1.1/search/tweets.json?q=Boston%20weather")
    consumer.sign(request)
    val client = HttpClientBuilder.create().build()
    val response = client.execute(request)
    response.getEntity().getContent()
  }

  def main(args: Array[String]) {

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

//    sparkTestRun
    popularHashTags2
  }

  /**
    * Spark test run, counts the number of ratings (1 - 5) in testdata/u.data
    * by Wei Huang, Mar. 28
    */
  def sparkTestRun(): Unit = {

    println("\nSpark test run\n")

    // set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // create a SparkContext using every core of the local machine, named RatingsCounter
    val sc = new SparkContext("local[*]", "RatingsCounter")

    // load up each line of the ratings data into an RDD
    val lines = sc.textFile("testdata/u.data")

    // convert each line to a string, split it out by tabs, and extract the third field.
    // (The file format is userID, movieID, rating, timestamp)
    val ratings = lines.map(x => x.toString().split("\t")(2))

    // count up how many times each value (rating) occurs
    val results = ratings.countByValue()

    // sort the resulting map of (rating, count) tuples
    val sortedResults = results.toSeq.sortBy(_._1)

    // Print each result on its own line.
    sortedResults.foreach(println)
  }

  /**
    * Spark stream test run
    * Listens to a stream of Tweets and keeps track of the most popular
    * hashtags over a 5 minute window.
    * by Wei Huang, Mar. 30
    */
  def popularHashTags(): Unit = {

    // set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // create a SparkContext
    val sc = new SparkContext("local[*]", "PopularHashtags")

    // create up a Spark streaming context named "PopularHashtags" that runs locally using
    // all CPU cores and one-second batches of data
    val ssc = new StreamingContext(sc, Seconds(1))

    System.setProperty("twitter4j.oauth.consumerKey", ConsumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", ConsumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", AccessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", AccessSecret)

    // create a DStream from Twitter using our streaming context
    val tweets = TwitterUtils.createStream(ssc, None)
    println("========="+tweets.print())

    // extract the text of each status update into DStreams using map()
    val statuses = tweets.map(status => status.getText())


    // blow out each word into a new DStream
    val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))

    // eliminate anything that's not a hashtag
    val hashtags = tweetwords.filter(word => word.startsWith("#"))

    // map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding up the values
    val hashtagKeyValues = hashtags.map(hashtag => (hashtag, 1))

    // count them up over a 5 minute window sliding every one second
    val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow((x, y) => x + y, (x, y) => x - y, Seconds(300), Seconds(1))
    //  You will often see this written in the following shorthand:
    //val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow( _ + _, _ -_, Seconds(300), Seconds(1))

    // sort the results by the count values
    val sortedResults = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2, false))

    // print the top 10
    sortedResults.print

    // set a checkpoint directory, and start
    ssc.checkpoint("testdata/checkpoint/")
    ssc.start()
    ssc.awaitTermination()
  }



  def popularHashTags2(): Unit = {

    // set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // create a SparkContext
    val sc = new SparkContext("local[*]", "PopularHashtags")

    // create up a Spark streaming context named "PopularHashtags" that runs locally using
    // all CPU cores and one-second batches of data
    val ssc = new StreamingContext(sc, Seconds(1))

    System.setProperty("twitter4j.oauth.consumerKey", ConsumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", ConsumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", AccessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", AccessSecret)

    // create a DStream from Twitter using our streaming context
    val tweets = TwitterUtils.createStream(ssc, None)

    // extract the text of each status update into DStreams using map()
    //val statuses = tweets.map(status => status.getText())
    val entweets = tweets.filter(s => s.getLang == "en")
    val statuses = entweets.map(status => (status.getText(),SentimentUtils.detectSentimentScore(status.getText())))

    // blow out each word into a new DStream
    //val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))

    // eliminate anything that's not a hashtag
    //val hashtags = tweetwords.filter(word => word.startsWith("#"))
    val hashtags = statuses.map(a => (a._1.split(" ").filter(b => b.startsWith("#")).headOption.getOrElse("NoHashTag"),a._2))

    // map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding up the values
    val hashtagKeyValues = hashtags.map(hashtag => (hashtag._1, (1, hashtag._2)))

    // count them up over a 5 minute window sliding every one second
    val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow((x, y) => (x._1 + y._1, x._2 + y._2), (x, y) => (x._1 - y._1, x._2 - y._2), Seconds(300), Seconds(1))
    //  You will often see this written in the following shorthand:
    //val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow( _ + _, _ -_, Seconds(300), Seconds(1))

    // sort the results by the count values
    val sortedResults = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2._1, false))

    // print the top 10
    sortedResults.print


    // set a checkpoint directory, and start
    ssc.checkpoint("testdata/checkpoint/")
    ssc.start()
    ssc.awaitTermination()
  }
}
