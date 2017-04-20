package edu.neu.coe.scala.retrieval

import org.scalatest.{FlatSpec, Matchers}
import twitter4j._
import org.scalamock.scalatest.MockFactory



/**
  * Created by Team2 on 2017/4/12.
  */
class UsecasesSpec extends FlatSpec with Matchers with MockFactory {

  val mockedStatus1 = mock[Status]
  val mockedStatus2 = mock[Status]
  (mockedStatus1.getLang _).expects().returning("en").anyNumberOfTimes()
  (mockedStatus2.getLang _).expects().returning("zh").anyNumberOfTimes()
  (mockedStatus1.getText _).expects().returning("test1 #ABC").anyNumberOfTimes()
  (mockedStatus2.getText _).expects().returning("test2").anyNumberOfTimes()
  val mockedList = List(mockedStatus1,mockedStatus2)

  behavior of "mockedList"

  it should "match size" in {
    mockedList.length shouldBe 2
  }


  behavior of "filterLanguage"

  val filteredList = mockedList.filter(Usecases.filterLanguage)

  it should "match size" in {
    filteredList.length shouldBe 1
  }

  it should "match text" in {
    (mockedStatus1.getText _).expects().returning("test1 #ABC").anyNumberOfTimes()
    filteredList.head.getText shouldBe "test1 #ABC"
  }


  behavior of "getTextAndSentiment"

  val mappedList1 = mockedList.map(Usecases.getTextAndSentiment)

  it should "match size" in {
    //(mockedStatus1.getText _).expects().returning("test1 #ABC").anyNumberOfTimes()

    mappedList1.length shouldBe mockedList.length
  }


  behavior of "getHashTags"

  val mappedList2 = mappedList1.map(Usecases.getHashTags)

  it should "match size" in {
    mappedList2.length shouldBe mockedList.length
  }

  it should "contain hashtag #ABC" in {
    mappedList2.head._1 shouldBe "#ABC"
  }

  behavior of "addCountToHashTags"

  val mappedList3 = mappedList2.map(Usecases.addCountToHashTags)

  it should "match size" in {
    mappedList3.length shouldBe mockedList.length
  }

  it should "match pattern" in {
    mappedList3.head should matchPattern {case (_,(_,_)) =>}
  }

  it should "match context" in {
    mappedList3.head shouldBe ("#ABC",(1,2.0))
  }


  behavior of "plusForTwo"

  it should "work" in {
    Usecases.plusForTwo((1,1.0),(1,2.0)) shouldBe (2,3.0)
  }


  behavior of "minusForTwo"

  it should "work" in {
    Usecases.minusForTwo((1,2.0),(1,1.0)) shouldBe (0,1.0)
  }

  behavior of "countsAveSentimentScore"

  it should "work" in {
    Usecases.countsAveSentimentScore("#ABC",(2,6.0)) shouldBe ("#ABC",(2,3.0))
  }

  val mockedGeoLocation = mock[GeoLocation]
  val mockedStatus3 = mock[Status]

  behavior of "filterContainGeoLocation"

  (mockedStatus3.getLang _).expects().returning("en").anyNumberOfTimes()
  (mockedStatus3.getText _).expects().returning("test3").anyNumberOfTimes()
  (mockedGeoLocation.getLatitude _).expects().returning(41.730610).anyNumberOfTimes()
  (mockedGeoLocation.getLongitude _).expects().returning(-72.935242).anyNumberOfTimes()
  (mockedStatus1.getGeoLocation _).expects().returning(mockedGeoLocation).anyNumberOfTimes()
  (mockedStatus3.getGeoLocation _).expects().returning(null).anyNumberOfTimes()

  val mockedList2 = List(mockedStatus1,mockedStatus3)

  val filteredList2 = mockedList2.filter(Usecases.filterLanguage)

  val filteredList3 = filteredList2.filter(Usecases.filterContainGeoLocation)

  it should "match size" in {
    filteredList2.size shouldBe 2
    filteredList3.size shouldBe 1
  }


  behavior of "nearLocation"

  it should "return true for nyc location" in {
    (mockedGeoLocation.getLatitude _).expects().returning(41.730610).anyNumberOfTimes()
    (mockedGeoLocation.getLongitude _).expects().returning(-72.935242).anyNumberOfTimes()
    Usecases.nearLocation(mockedGeoLocation,40.730610, -73.935242) shouldBe true
  }

  it should "return false for la location" in {
    (mockedGeoLocation.getLatitude _).expects().returning(41.730610).anyNumberOfTimes()
    (mockedGeoLocation.getLongitude _).expects().returning(-72.935242).anyNumberOfTimes()
    Usecases.nearLocation(mockedGeoLocation,34.052235, -118.243683) shouldBe false
  }

  behavior of "matchLocation"

  it should "match New York City, NY, USA" in {
    (mockedGeoLocation.getLatitude _).expects().returning(41.730610).anyNumberOfTimes()
    (mockedGeoLocation.getLongitude _).expects().returning(-72.935242).anyNumberOfTimes()
    Usecases.matchLocation(mockedGeoLocation) shouldBe "New York City, NY, USA,40.730610, -73.935242"
  }

  it should "match Other Location" in {
    (mockedGeoLocation.getLatitude _).expects().returning(1).anyNumberOfTimes()
    (mockedGeoLocation.getLongitude _).expects().returning(1).anyNumberOfTimes()
    Usecases.matchLocation(mockedGeoLocation) shouldBe "Other Location"
  }

  behavior of "getLocationAndSentiment"

  (mockedGeoLocation.getLatitude _).expects().returning(41.730610).anyNumberOfTimes()
  (mockedGeoLocation.getLongitude _).expects().returning(-72.935242).anyNumberOfTimes()
  (mockedStatus1.getText _).expects().returning("test1 #ABC").anyNumberOfTimes()
  (mockedStatus1.getGeoLocation _).expects().returning(mockedGeoLocation).anyNumberOfTimes()
  val mappedList4 = filteredList3.map(Usecases.getLocationAndSentiment)

  it should "match size" in {
    mappedList4.size shouldBe filteredList3.size
  }

  it should "match context" in {
    //println(mappedList4.head._1)
    //println(mappedList4.head._2)
    mappedList4.head._1 shouldBe "New York City, NY, USA,40.730610, -73.935242"
    mappedList4.head._2 shouldBe 2.0
  }

  behavior of "compareSentiment"

  it should "work" in {
    Usecases.compareSentiment(1.0,2.0) shouldBe false
    Usecases.compareSentiment(3.0,2.0) shouldBe true
  }

  behavior of "calcSentimentFromSearchApi"

  it should "work with Boston" in {
    Usecases.calcSentimentFromSearchApi("Boston weather",1) shouldBe 2.5 +- 2.5
  }


}
