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

  it should "has 7 responses" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromString(TwitterClient.getFromSearchApiByKeyword("Trump",5))
    val ts = for (t <- ingester(source).toSeq) yield t
    ts.size shouldBe 7
    source.close()
  }

  it should "match pattern" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromString(TwitterClient.getFromSearchApiByKeyword("Trump",5))
    val ts = for (t <- ingester(source).toSeq) yield t
    ts should matchPattern { case Stream(Success(_),Success(_),Success(_),Success(_),Success(_),Success(_),Success(_)) => }
    source.close()
  }

  it should "contains Tweet" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromString(TwitterClient.getFromSearchApiByKeyword("Trump",5))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = for (a <- rts) yield a match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    rs.head.statuses.head should matchPattern { case Tweet(_,_,_,_,_,_) => }
    source.close()
  }

  //This unit test may not match because that count in metadata is the input parameter, however result may smaller than it.
/*
  it should "match the size with count field" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromString(TwitterClient.getFromSearchApiByKeyword("Trump",5))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = for (a <- rts) yield a match {
      case Success(x) => x
      case Failure(e) => throw new Exception("err:"+e)
    }
    rs.head.statuses.size shouldBe rs.head.search_metadata.count
    source.close()
  }
*/

}
