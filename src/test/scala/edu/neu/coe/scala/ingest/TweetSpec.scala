package edu.neu.coe.scala.ingest

import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}
import scala.util._

/**
  * Created by Yuan Ying on 2017/3/26.
  */
class TweetSpec extends FlatSpec with Matchers{


  behavior of "Tweet convert for tweet1.json"

  it should "match the size" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//tweet1.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    ts.size shouldBe 1
    source.close()
  }

  it should "match pattern" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//tweet1.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    ts should matchPattern { case Stream(Success(_)) => }
    source.close()
  }

  it should "match content" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//tweet1.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    val tweet:Tweet = ts.head match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    tweet.retweet_count shouldBe 3746
    tweet.created_at shouldBe "Mon Aug 22 01:23:16 +0000 2016"
    tweet.lang shouldBe "en"
    tweet.text shouldBe "\"@SinAbunz_TM: @realDonaldTrump TRUMP VICTORY IN NOVEMBER! #MAGA #TrumpPence16\""
    tweet.user.id shouldBe 25073877
    tweet.user.favourites_count shouldBe 35
    tweet.user.location shouldBe "New York, NY"
    tweet.user.name shouldBe "Donald J. Trump"
    tweet.entities.hashtags.map(x => x.text) shouldBe List("MAGA","TrumpPence16")
    source.close()
  }


  behavior of "Tweet convert for tweet3.json"

  it should "match the size" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//tweet3.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    ts.size shouldBe 3
    source.close()
  }

  it should "match content" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//tweet3.json")
    val tts = for (t <- ingester(source).toSeq) yield t
    val ts = for (a <- tts) yield a match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    ts.map(x => x.retweet_count).toList shouldBe List(3746, 2301, 2547)
    ts.map(x => x.user.id).toList shouldBe List(25073877, 25073877, 25073877)
    ts.map(x => x.entities.hashtags).map(y => y.map(z => z.text)) shouldBe List(List("MAGA","TrumpPence16"),Nil,Nil)
    source.close()
  }


  behavior of "Tweet convert for searchapi_sample.json"

  it should "match the size" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//searchapi_sample.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    ts.size shouldBe 1
    source.close()
  }

  it should "match pattern" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//searchapi_sample.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    ts should matchPattern { case Stream(Success(_)) => }
    source.close()
  }

  it should "match content" in {
    val ingester = new Ingest[Tweet]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//searchapi_sample.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    val tweet:Tweet = ts.head match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    tweet.retweet_count shouldBe 0
    tweet.created_at shouldBe "Mon Mar 27 05:30:27 +0000 2017"
    tweet.lang shouldBe "en"
    tweet.text shouldBe "#2weeksleft Northeastern Illinois University (Chicago) is accepting applications for Police Sergeant until 4/3/17. https://t.co/c9SBw5NRfC"
    tweet.user.id shouldBe 135013634
    tweet.user.favourites_count shouldBe 19
    tweet.user.location shouldBe ""
    tweet.user.name shouldBe "The Blue Line"
    tweet.entities.hashtags.map(x => x.text) shouldBe List("2weeksleft")
    source.close()
  }
}
