package edu.neu.coe.scala.retrieval

import edu.neu.coe.scala.ingest.{Ingest, Response}
import edu.neu.coe.scala.sentiment.SentimentUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j._

import scala.io.{Codec, Source}
import scala.util.{Failure, Success}

/**
  * Usecases
  * should be called from Main()
  * Created by HuangWei on 4/11/17.
  */
object Usecases {


  System.setProperty("twitter4j.oauth.consumerKey", TwitterClient.ConsumerKey)
  System.setProperty("twitter4j.oauth.consumerSecret", TwitterClient.ConsumerSecret)
  System.setProperty("twitter4j.oauth.accessToken", TwitterClient.AccessToken)
  System.setProperty("twitter4j.oauth.accessTokenSecret", TwitterClient.AccessSecret)

  /**
    *
    *                       Listens to a stream of Tweets and keeps track of the most popular
    *                       hashtags over a 5 minute window.
    *                       by Wei Huang, Mar. 28
    */
  def popularHashTags(k: String = ""): Unit = {

    // set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // create a SparkContext
    val sc = new SparkContext("local[*]", "PopularHashtags")

    // create up a Spark streaming context named "PopularHashtags" that runs locally using
    // all CPU cores and one-second batches of data
    val ssc = new StreamingContext(sc, Seconds(1))



    // create a DStream from Twitter using our streaming context
    val tweets = k match {
      case "" => TwitterUtils.createStream(ssc, None)
      case _  => TwitterUtils.createStream(ssc, None,Seq(k))
    }

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

  val getLocationAndSentiment = {
    status:Status => (status.getGeoLocation match {
      case g:GeoLocation => matchLocation(g)
      case null => "null"
    }, SentimentUtils.detectSentimentScore(status.getText()))
  }

  val filterContainGeoLocation = {
    status:Status => Option(status.getGeoLocation) match {
      case Some(_) => true
      case None => false
    }
  }


  def matchLocation(g:GeoLocation):String = g match {
    case g if nearLocation(g,40.730610, -73.935242)  => "New York City, NY, USA,40.730610, -73.935242"
    case g if nearLocation(g,34.052235, -118.243683)  => "Los Angeles, CA, USA,34.052235, -118.243683"
    case g if nearLocation(g,47.608013, -122.335167)  => "Seattle, WA, USA,47.608013, -122.335167"
    case g if nearLocation(g,29.761993, -95.366302)  => "Houston, TX, USA,29.761993, -95.366302"
    case g if nearLocation(g,25.761681, -80.191788)  =>  "Miami, FL, USA,25.761681, -80.191788"
    case g if nearLocation(g,51.515419, -0.141099)  => "London, UK,51.515419, -0.141099"
    case g if nearLocation(g,43.653908, -79.384293)  => "Toronto, ON, Canada,43.653908, -79.384293"
    case g if nearLocation(g,-33.865143, 151.209900)  => "Sydney, NSW, Australia,-33.865143, 151.209900"
    case g if nearLocation(g,19.073212, 72.854195)  => "Mumbai, India,19.073212, 72.854195"
    case _ => "Other Location"
  }

  def nearLocation(g:GeoLocation,latitude: Double,longitude: Double) = {
    //println(g)
    if ((math.abs(g.getLatitude - latitude) < 2) &&
      (math.abs(g.getLongitude - longitude) < 2)
    ) true else false
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
    *
    *                       Listens to a stream of Tweets and keeps track of the most popular
    *                       locations over a 5 minute window.
    *                       by Yuan Ying, Apr. 12
    */
  //def popularLocations(ConsumerKey: String, ConsumerSecret: String, AccessToken: String, AccessSecret: String): Unit = {

  def popularLocations(k: String = ""): Unit = {
    // set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // create a SparkContext
    val sc = new SparkContext("local[*]", "PopularHashtags")

    // create up a Spark streaming context named "PopularHashtags" that runs locally using
    // all CPU cores and one-second batches of data
    val ssc = new StreamingContext(sc, Seconds(1))


    // create a DStream from Twitter using our streaming context
    val tweets = k match {
      case "" => TwitterUtils.createStream(ssc, None)
      case _  => TwitterUtils.createStream(ssc, None,Seq(k))
    }

    // extract the text of each status update into DStreams using map()
    //val statuses = tweets.map(status => status.getText())
    //val entweets = tweets.filter(s => s.getLang == "en")
    val entweets = tweets.filter(filterLanguage).filter(filterContainGeoLocation)

    //val statuses = entweets.map(status => (status.getText(), SentimentUtils.detectSentimentScore(status.getText())))
    val statuses = entweets.map(getLocationAndSentiment)

    // blow out each word into a new DStream
    //val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))

    // eliminate anything that's not a hashtag
    //val hashtags = tweetwords.filter(word => word.startsWith("#"))
    //val hashtags = statuses.map(getHashTags)

    // map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding up the values
    val hashtagKeyValues = statuses.map(addCountToHashTags)

    // count them up over a 5 minute window sliding every one second
    val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow(plusForTwo, minusForTwo, Seconds(36000), Seconds(1))
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


  def calcSentimentFromSearchApi(k: String = "", count: Int = 90): Double = {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromString(TwitterClient.getFromSearchApiByKeyword(k.replaceAll(" ","%20"),count))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = rts.flatMap(_.toOption)
    val tss = rs.map(r => r.statuses)
    //for (t <- tss) println(t.size)

    val ts = tss.flatten

    //println(ts.size)

    val sts = ts.par.map(s => SentimentUtils.detectSentimentScore(s.text))

    println(sts.sum/sts.size)

    sts.sum/sts.size
  }

  def compareSentiment(a:Double,b:Double) :Boolean = if (a > b) true else false
}
