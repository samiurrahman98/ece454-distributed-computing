import org.apache.spark.{SparkContext, SparkConf}
import scala.collection.mutable.ArrayBuffer

// please don't change the object name
object Task3 {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Task 3")
    val sc = new SparkContext(conf)

    val textFile = sc.textFile(args(0))

    val output = textFile.flatMap(line => {
      val ratings = line.split(",", -1)
      var userRatingCount = ArrayBuffer[(Int, Int)]()

      for (i <- 1 until ratings.length) {
        if (ratings(i) != "") {
          val count = (i, 1)
          userRatingCount += count
        } else {
          val count = (i, 0)
          userRatingCount += count
        }
      }
      userRatingCount
    }).reduceByKey(_ + _)
      .map(x => x._1 + "," + x._2)
      
      output.coalesce(1).saveAsTextFile(args(1))
  }
}