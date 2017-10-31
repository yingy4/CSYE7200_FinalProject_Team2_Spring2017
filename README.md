# CSYE7200_FinalProject_Team2_Spring2017  [![CircleCI](https://circleci.com/gh/yingy4/CSYE7200_FinalProject_Team2_Spring2017/tree/master.svg?style=svg)](https://circleci.com/gh/yingy4/CSYE7200_FinalProject_Team2_Spring2017/tree/master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/6553cd3c228a4db2914589fd1839eacd)](https://www.codacy.com/app/yingy4/CSYE7200_FinalProject_Team2_Spring2017?utm_source=github.com&utm_medium=referral&utm_content=yingy4/CSYE7200_FinalProject_Team2_Spring2017&utm_campaign=badger)
This is the CSYE7200 FinalProject for Team2 Spring2017

Team member:

Yuan Ying - ying.yua@husky.neu.edu

Mushtaq Rizvi - rizvi.m@husky.neu.edu

Wei Huang - huang.wei3@husky.neu.edu

Jinjin Zhang - zhang.jinj@husky.neu.edu

# Sentiment Analysis on Tweets

<a href="https://docs.google.com/a/husky.neu.edu/presentation/d/1-KjtC4Uy5i0-RVGBogw1Nqr2QRDuedXLVl85UjXmvUY/edit?usp=sharing">Planning presentation</a>

<a href="https://docs.google.com/a/husky.neu.edu/presentation/d/1zS0zwbe4KTycNU0Fb0l5b6tjdckRTYGNMPtq6TUjMBA/edit?usp=sharing">Final presentation</a>

# Abstract
The goal is to process real Twitter datasets to extract meaningful analysis by performing Sentiment Analysis. In this project, we utilize information available through the Twitter API to gather information about the tweets and their users. Sentiment analysis is used to see if a text is neutral, positive or negative. Since Twitter restricts each tweet to be less than 140 characters, users’ comments tend to be straightforward. In addition, because of its huge influences, many people have started to include a hashtag in their tweets to attract social attention. Therefore, Twitter has become a great platform to examine people’s feedback. Thus twitter is full of sentiments. This project is using Twitter Search and Streaming API through Spark Streaming to retreive the tweets, Stanford NLP library to detect the sentiments and Apache Zeppelin to visualize the results. 

# Methodology
1\. Tweets acquired by Search API are in JSON format with a maximum limit of 100 per request. Built a JSON parser to correctly parse the and filter those attributes which are not required.  
2\. More filtering by specifying the language and Geolocation of tweets.  
3\. Special characters are removed to increase the accuracy of the sentiment scores.  
4\. Using Stanford NLP to calculate the sentiment score which tells whether the particular tweet is positive or negative.  
5\. Using Spark Streaming to receive the stream of tweets and perform some of the analysis like popular hashtags and location based sentiment scores.  
6\. Last but not the least, Apache Zeppelin is used for the visualization to display the bar plots showing the popular hashtags and their respective counts. Also, the leaflet geo map which shows the sentiment score of a particular location using latitudes and longitudes.  

# Inputs and Outputs   
If the user inputs a city or company name, the system calculates the sentiment score for the city weather and company stock in last 7 days.   
If the user inputs a keyword, the system generates top 10 popular hashtags in a 5 minute window. It also shows the count of tweets and sentiment score for each hashtag and there is a bar chart in Apache Zeppelin where results can be visualized.  

The arguments that can be passed while running the jar are:-   
1\. hashtags => It will run popularHashTags without keyword  
2\. hashtags New York => It will run popularHashTags with keyword "New York"  
3\. map => It will run popularLocations without keyword   
4\. map New York => It will run popularLocations with keyword "New York"  
5\. weather => It will run 10 city weather comparison   
6\. weather New York => It will return sentiment score for New York weather  
7\. stock => It will run 10 company stock comparison   
8\. stock Bank of America => It will return sentiment score for Bank of America stock   

If you are using SBT, you can run as:  
1\. sbt "run hashtags"  
2\. sbt "run hashtags New York"  
3\. etc...

# Continuous integration
This project is using CircleCI as the continuous integration tool.

Current Status:
[![CircleCI](https://circleci.com/gh/yingy4/CSYE7200_FinalProject_Team2_Spring2017/tree/master.svg?style=svg)](https://circleci.com/gh/yingy4/CSYE7200_FinalProject_Team2_Spring2017/tree/master)
