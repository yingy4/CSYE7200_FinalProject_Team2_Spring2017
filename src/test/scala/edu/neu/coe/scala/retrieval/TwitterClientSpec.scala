package edu.neu.coe.scala.retrieval

import edu.neu.coe.scala.ingest._

import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}

import scala.util._

/**
  * Created by Team2 on 2017/3/31.
  */
class TwitterClientSpec extends FlatSpec with Matchers{

  behavior of "Tweet convert from api output json file"

  it should "has 2 responses" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//sample3.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    ts.size shouldBe 2
    source.close()
  }

  it should "match pattern" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//sample3.json")
    val ts = for (t <- ingester(source).toSeq) yield t
    ts should matchPattern { case Stream(Success(_),Success(_)) => }
    source.close()
  }

  it should "contains Tweet" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromFile("testdata//sample3.json")
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = rts.flatMap(_.toOption)
    rs.headOption.getOrElse(fail()).statuses.headOption.getOrElse(fail()) should matchPattern { case Tweet(_,_,_,_,_,_) => }
    source.close()
  }

  //This unit test may not match because that count in metadata is the input parameter, however result may smaller than it.
/*
  it should "match the size with count field" in {
    val ingester = new Ingest[Response]()
    implicit val codec = Codec.UTF8
    val source = Source.fromString(TwitterClient.getFromSearchApiByKeyword("Trump",5))
    val rts = for (t <- ingester(source).toSeq) yield t
    val rs = rts.flatMap(_.toOption)
    rs.head.statuses.size shouldBe rs.head.search_metadata.count
    source.close()
  }
*/

}
