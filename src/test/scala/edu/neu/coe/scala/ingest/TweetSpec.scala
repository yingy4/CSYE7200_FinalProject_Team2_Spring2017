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
    tweet.retweet_count shouldBe 2301
    tweet.created_at shouldBe "Tue Aug 23 13:53:11 +0000 2016"
    tweet.lang shouldBe "en"
    tweet.text shouldBe "It is being reported by virtually everyone, and is a fact, that the media pile on against me is the worst in American political history!"
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
    ts.map(x => x.retweet_count).toList shouldBe List(2301, 2547, 4310)
    source.close()
  }
}
