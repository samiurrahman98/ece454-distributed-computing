import org.apache.spark.{SparkContext, SparkConf}
import scala.collection.mutable.{ListBuffer}

// please don't change the object name
object Task3 {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Task 3")
    val sc = new SparkContext(conf)

    val textFile = sc.textFile(args(0))

    val output = textFile.flatMap(line => 
      {
        var ratings = line.split(",", -1);
        var key = ListBuffer.empty[(Int, Int)];

        for (i <- 1 until ratings.length) {
          if (ratings(i) != "") {
            var tuple = (i, 1);
            key += tuple;
          }
        }

        key
      }
    ).map(tuple => 
      {
        (tuple._1, (tuple._2, 1));
      }
    ).reduceByKey((t1, t2) =>
      {
        (t1._1 + t2._1, t1._2 + t2._2);
      }
    ).map((tuple) => 
      {
        // var avg: Double = 0;
        // avg = tuple._2._1.toDouble / tuple._2._2.toDouble;
        // f"${tuple._1},${avg}%1.2f"
        
        f"${tuple._1},${tuple._2._2}"
      }
    );
    
    output.coalesce(1).saveAsTextFile(args(1))
  }
}
