import org.apache.spark.{SparkContext, SparkConf}

// please don't change the object name
object Task4 {
  def calc(arr1: Array[String], arr2: Array[String]): String = {
    val arr1_i = arr1.drop(1).map(x => if (x.nonEmpty) x.toInt else 0)
    val arr2_i = arr2.drop(1).map(y => if (y.nonEmpty) y.toInt else 0)

    var mag1 = 0
    var mag2 = 0
    var dot = 0

    for (i <- 0 to arr1_i.length - 1) {
      dot = dot + (arr1_i(i) * arr2_i(i))
      mag1 += arr1_i(i) * arr1_i(i)
      mag2 += arr2_i(i) * arr2_i(i)
    }

    val cosine = dot / (Math.sqrt(mag1) * Math.sqrt(mag2))
    arr1(0) + "," + arr2(0) + "," + f"$cosine%1.2f"
  }

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Task 4")
    val sc = new SparkContext(conf)

    val textFile = sc.textFile(args(0))

    val lines = textFile.map(line => line.split(",", -1));
    val output = lines.cartesian(lines)
                      .filter(pair => pair._1(0).compareTo(pair._2(0)) < 0)
                      .map(pair => calc(pair._1, pair._2))
    
    output.saveAsTextFile(args(1))
  }
}
