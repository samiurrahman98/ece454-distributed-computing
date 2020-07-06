import org.apache.spark.{SparkContext, SparkConf}
import scala.collection.Map
import org.apache.spark.rdd.RDD

object Task4 {
  def buildMovieRatingsMap(movieRatings: RDD[String]): Map[String, Array[Byte]] = {
    movieRatings.map(movieRating => {
      val tokens = movieRating.split(",", -1)
      val title = tokens(0)
      val ratings = new Array[Byte](tokens.length - 1)

      for (i <- 0 until ratings.length) {
        if (tokens(i + 1) != "") {
          ratings(i) = tokens(i + 1).toByte
        }
      }
      (title, ratings)
    }).collectAsMap()
  }

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Task 4")
    val sc = new SparkContext(conf)

    val textFile = sc.textFile(args(0))

    val movieRatingsMap = sc.broadcast(buildMovieRatingsMap(textFile))

    val output = textFile.flatMap(movie1 => {
      val title1 = movie1.split(",", 2)(0)
      val ratings1 = movieRatingsMap.value(title1)

      movieRatingsMap.value
        .filterKeys(title2 => title2 > title1)
        .map(movie2 => {
          val title2 = movie2._1
          val ratings2 = movie2._2

          var similarity = 0
          for(i <- 0 until ratings1.length) {
            if (ratings1(i) == ratings2(i) && ratings1(i) != 0)
              similarity += 1
          }

          (title1 + "," + title2 + "," + similarity)
        })
    })
    
    sc.parallelize(Seq(output)).coalesce(1).saveAsTextFile(args(1))
  }
}