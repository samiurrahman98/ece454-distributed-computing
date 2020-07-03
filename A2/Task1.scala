import org.apache.spark.{SparkContext, SparkConf}
import scala.collection.mutable.{ListBuffer}

// please don't change the object name
object Task1 {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Task 1")
    val sc = new SparkContext(conf)

    val textFile = sc.textFile(args(0))

    val output = textFile.map(x => 
      {
        var ratings = x.split(",");
        var maxRating = 0;
        var maxUsers = ListBuffer.empty[Int];
        var movieTitle = ratings(0);

        for(i <- 1 until ratings.length) {
            var strRating = ratings(i);
            if (strRating != "") {
                var rating = strRating.toInt;
                if (rating > maxRating) {
                    maxUsers.clear;
                    maxRating = rating;
                    maxUsers += i;
                } else if (rating == maxRating) {
                    maxUsers += i
                }
            }
        }
        movieTitle + "," + maxUsers.mkString(",");
      }
    )
    
    output.saveAsTextFile(args(1))
  }
}
