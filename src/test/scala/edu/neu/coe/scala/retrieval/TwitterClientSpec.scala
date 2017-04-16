package edu.neu.coe.scala.retrieval

import edu.neu.coe.scala.ingest._

import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}

import scala.util._

/**
  * Created by YY on 2017/3/31.
  */
class TwitterClientSpec extends FlatSpec with Matchers{

  behavior of "Tweet convert from api"

  it should "only has one response" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromInputStream(TwitterClient.getFromSearchApiByKeyword("Trump"))
    val ts = for (t <- ingester(source).toSeq) yield t
    ts.size shouldBe 1
    source.close()
  }

  it should "match pattern" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromInputStream(TwitterClient.getFromSearchApiByKeyword("Trump"))
    val ts = for (t <- ingester(source).toSeq) yield t
    ts should matchPattern { case Stream(Success(_)) => }
    source.close()
  }

  it should "contains Tweet" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromInputStream(TwitterClient.getFromSearchApiByKeyword("Trump"))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = for (a <- rts) yield a match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    rs.head.statuses.head should matchPattern { case Tweet(_,_,_,_,_,_) => }
    source.close()
  }

  it should "match the size with count field" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromInputStream(TwitterClient.getFromSearchApiByKeyword("Trump"))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = for (a <- rts) yield a match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    rs.head.statuses.size shouldBe rs.head.search_metadata.count
    source.close()
  }


}
