package edu.neu.coe.scala.sentiment

import edu.neu.coe.scala.ingest._
import edu.neu.coe.scala.retrieval.TwitterClient
import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}
import scala.util._

/**
  * Created by YY on 2017/4/6.
  */
class SentimentUtilsSpec extends FlatSpec with Matchers {

  behavior of "detectSentiment"

  it should "detect a positive sentiment" in {

    SentimentUtils.detectSentiment("It was a very nice experience.") shouldBe (SentimentUtils.POSITIVE)

  }

  it should "work for one tweet" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//tweet1_POSITIVE.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    val tweet:Tweet = ts.head match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    SentimentUtils.detectSentiment(tweet.text) shouldBe SentimentUtils.POSITIVE
    source.close()
  }

  it should "work for three tweets" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//tweet3.json")
    val tts = for (t <- ingester(source).toSeq) yield t
    val ts = tts.flatMap(_.toOption)
    ts.map(x => SentimentUtils.detectSentiment(x.text)).toList shouldBe List(SentimentUtils.NEGATIVE, SentimentUtils.NEGATIVE, SentimentUtils.NEGATIVE)
    source.close()
  }


  it should "work for search api" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromString(TwitterClient.getFromSearchApiByKeyword("Hi",2))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = rts.flatMap(_.toOption)
    SentimentUtils.detectSentiment(rs.head.statuses.head.text) should matchPattern {
      case SentimentUtils.VERY_NEGATIVE =>
      case SentimentUtils.NEGATIVE =>
      case SentimentUtils.NEUTRAL =>
      case SentimentUtils.POSITIVE =>
      case SentimentUtils.VERY_POSITIVE =>
      case SentimentUtils.NOT_UNDERSTOOD =>
    }
    source.close()
  }

  it should "work for search api with muti tweets " in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromString(TwitterClient.getFromSearchApiByKeyword("Hi",2))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = rts.flatMap(_.toOption)
    rs.head.statuses.map(x => SentimentUtils.detectSentiment(x.text)).size shouldBe rs.head.statuses.size
    source.close()
  }

  behavior of "detectSentimentScore"

  it should "detect 3.0" in {
    SentimentUtils.detectSentimentScore("It was a very nice experience.") shouldBe 3.0
  }

}
