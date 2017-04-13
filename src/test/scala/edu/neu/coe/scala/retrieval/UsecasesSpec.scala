package edu.neu.coe.scala.retrieval

import org.scalatest.{FlatSpec, Matchers}
import twitter4j._
import org.scalamock.scalatest.MockFactory



/**
  * Created by YY on 2017/4/12.
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

/*
  behavior of "getTextAndSentiment"

  it should "match size" in {
    (mockedStatus1.getText _).expects().returning("test1 #ABC").anyNumberOfTimes()
    val mappedList1 = mockedList.map(Usecases.getTextAndSentiment)
    mappedList1.length shouldBe mockedList.length
  }
*/

  behavior of "getHashTags"

  val mappedList2 = mockedList.map(s => (s.getText,1.0)).map(Usecases.getHashTags)

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
    mappedList3.head shouldBe ("#ABC",(1,1.0))
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


}
