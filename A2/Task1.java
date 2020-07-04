import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Task1 {
  public static class Rating extends Mapper<Object, Text, Text, Text> {
    private Text movieTitle = new Text();
    private Text highestRatings = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      StringBuilder sb = new StringBuilder();
      String[] tokens = value.toString().split(",", -1);

      movieTitle.set(tokens[0]);
      int highestRating = 0;

      for (int i = 1; i < tokens.length; i++) {
        String token = tokens[i];
        int rating = !token.isEmpty() ? Integer.valueOf(tokens[i]) : 0;

        if (rating > highestRating) {
          sb = new StringBuilder();
          sb.append(String.valueOf(i));
          highestRating = rating;
        } else if (rating == highestRating)
          sb.append("," + String.valueOf(i));
      }

      highestRatings.set(sb.toString());
      context.write(movieTitle, highestRatings);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    conf.set("mapreduce.output.textoutputformat.separator", ",");

    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 2) {
      System.err.println("Usage: <in> <out>");
      System.exit(2);
    }


    Job job = new Job(conf, "Task I: highest rating user per movie");
    job.setJarByClass(Task1.class);
    job.setMapperClass(Task1.Rating.class);
    job.setNumReduceTasks(0);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    TextInputFormat.addInputPath(job, new Path(otherArgs[0]));
    TextOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}