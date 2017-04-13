package edu.neu.coe.scala.retrieval

import edu.neu.coe.scala.sentiment.SentimentUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j._

/**
  * Usecases
  * should be called from Main()
  * Created by HuangWei on 4/11/17.
  */
object Usecases {

  /**
    *
    * @param ConsumerKey    twitter oauth consumerKey
    * @param ConsumerSecret twitter oauth ConsumerSecret
    * @param AccessToken    twitter oauth AccessToken
    * @param AccessSecret   twitter oauth AccessSecret
    *
    *                       Listens to a stream of Tweets and keeps track of the most popular
    *                       hashtags over a 5 minute window.
    *                       by Wei Huang, Mar. 28
    */
  def popularHashTags(ConsumerKey: String, ConsumerSecret: String, AccessToken: String, AccessSecret: String): Unit = {

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
    //val entweets = tweets.filter(s => s.getLang == "en")
    val entweets = tweets.filter(filterLanguage)
    //val statuses = entweets.map(status => (status.getText(), SentimentUtils.detectSentimentScore(status.getText())))
    val statuses = entweets.map(getTextAndSentiment)

    // blow out each word into a new DStream
    //val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))

    // eliminate anything that's not a hashtag
    //val hashtags = tweetwords.filter(word => word.startsWith("#"))
    val hashtags = statuses.map(getHashTags)

    // map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding up the values
    val hashtagKeyValues = hashtags.map(addCountToHashTags)

    // count them up over a 5 minute window sliding every one second
    val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow(plusForTwo, minusForTwo, Seconds(600), Seconds(1))
    //  You will often see this written in the following shorthand:
    //val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow( _ + _, _ -_, Seconds(300), Seconds(1))

    val hashtagCountsAveSentimentScore = hashtagCounts.map(countsAveSentimentScore)

    // sort the results by the count values
    val sortedResults = hashtagCountsAveSentimentScore.transform(rdd => rdd.sortBy(x => x._2._1, false))

    // print the top 10
    sortedResults.print


    // set a checkpoint directory, and start
    ssc.checkpoint("testdata/checkpoint/")
    ssc.start()
    ssc.awaitTermination()
  }

  val filterLanguage = {
    status:Status => status.getLang == "en"
  }

  val getTextAndSentiment = {
    status:Status => (status.getText(), SentimentUtils.detectSentimentScore(status.getText()))
  }

  val getHashTags = {
    a:(String,Double) => (a._1.split(" ").filter(b => b.startsWith("#")).headOption.getOrElse("NoHashTag"), a._2)
  }

  val addCountToHashTags = {
    a:(String,Double) => (a._1, (1, a._2))
  }

  val plusForTwo = {
    (x:(Int,Double), y:(Int,Double)) => (x._1 + y._1, x._2 + y._2)
  }

  val minusForTwo = {
    (x:(Int,Double), y:(Int,Double)) => (x._1 - y._1, x._2 - y._2)
  }

  val countsAveSentimentScore = {
    a:(String,(Int,Double)) => (a._1, (a._2._1, a._2._2 / a._2._1))
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
}
