import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Task3 {
  public static class RatingAvgMapper extends Mapper<Object, Text, Text, IntWritable> {
    private Text word = new Text();
    private final static IntWritable rating = new IntWritable();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String[] tokens = value.toString().split(",", -1);

      for (int i = 1; i < tokens.length; i++) {
        if (!tokens[i].isEmpty()) {
          word.set(String.valueOf(i));
          rating.set(Integer.valueOf(1));
          context.write(word, rating);
        }
      }
    }
  }

  public static class RatingSumReducer extends Reducer<Text, IntWritable, Text, Text> {
    private final static Text average = new Text();

    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
      int sum = 0;

      for (IntWritable val: values) {
        sum += 1;
      }
      average.set(Integer.toString(sum));
      context.write(key, average);
    }
  }
    
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    conf.set("mapreduce.output.textoutputformat.separator", ",");
    
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 2) {
      System.err.println("Usage: rating avg <in> <out>");
      System.exit(2);
    }

    Job job = Job.getInstance(conf, "Task III: rating avg");
    job.setJarByClass(Task3.class);
    job.setMapperClass(Task3.RatingAvgMapper.class);
    job.setReducerClass(Task3.RatingSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    TextInputFormat.addInputPath(job, new Path(otherArgs[0]));
    TextOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
