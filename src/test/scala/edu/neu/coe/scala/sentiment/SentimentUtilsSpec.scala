package edu.neu.coe.scala.sentiment

import edu.neu.coe.scala.ingest._
import edu.neu.coe.scala.retrieval.TwitterClient
import edu.neu.coe.scala.sentiment._
import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}
import scala.util._

/**
  * Created by YY on 2017/4/6.
  */
class SentimentUtilsSpec extends FlatSpec with Matchers {

  behavior of "detectSentiment"


  it should "detect a very positive sentiment" in {

    SentimentUtils.detectSentiment("It was a very nice experience.") shouldBe (SentimentUtils.VERY_POSITIVE)

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
    SentimentUtils.detectSentiment(tweet.text) shouldBe SentimentUtils.VERY_POSITIVE
    source.close()
  }

  it should "work for three tweets" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//tweet3.json")
    val tts = for (t <- ingester(source).toSeq) yield t
    val ts = for (a <- tts) yield a match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    ts.map(x => SentimentUtils.detectSentiment(x.text)).toList shouldBe List(SentimentUtils.NEGATIVE, SentimentUtils.NOT_UNDERSTOOD, SentimentUtils.NEGATIVE)
    source.close()
  }


  it should "work for search api" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromInputStream(TwitterClient.getFromSearchApiByKeyword("Trump",10))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = for (a <- rts) yield a match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
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
    val source = Source.fromInputStream(TwitterClient.getFromSearchApiByKeyword("Trump",10))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = for (a <- rts) yield a match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    rs.head.statuses.map(x => SentimentUtils.detectSentiment(x.text)).size shouldBe rs.head.search_metadata.count
    source.close()
  }

  behavior of "detectSentimentScore"

  it should "detect 3.0" in {
    SentimentUtils.detectSentimentScore("It was a very nice experience.") shouldBe 3.0
  }

}
