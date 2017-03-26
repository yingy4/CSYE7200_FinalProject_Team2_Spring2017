package edu.neu.coe.scala.ingest

import scala.util._

/**
  * Created by Yuan Ying on 2017/3/26.
  */
case class Tweet(text: String,lang: String,created_at: String,retweet_count: Int)

object Tweet extends App {
  import spray.json._

  object TweetProtocol extends DefaultJsonProtocol {
    implicit val formatTweet = jsonFormat4(Tweet.apply)
  }

  trait IngestibleTweet extends Ingestible[Tweet] {

    def fromString(w: String): Try[Tweet] = {
      println("w="+w.parseJson.prettyPrint)
      import TweetProtocol._
      Try(w.parseJson.convertTo[Tweet])
    }
  }

  implicit object IngestibleTweet extends IngestibleTweet

}
