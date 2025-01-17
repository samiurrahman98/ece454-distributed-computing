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
  public static class UserRatingMapper extends Mapper<Object, Text, IntWritable, IntWritable> {
    private final static IntWritable zero = new IntWritable(0);
    private final static IntWritable one = new IntWritable(1);
    private IntWritable userId = new IntWritable();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String[] tokens = value.toString().split(",", -1);

      for (int i = 1; i < tokens.length; i++) {
        userId.set(i);
        if (!tokens[i].isEmpty())
          context.write(userId, one);
        else
          context.write(userId, zero);
      }
    }
  }

  public static class UserRatingReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
      int count = 0;

      for (IntWritable val: values)
        count += val.get();
      
      result.set(count);
      context.write(key, result);
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

    Job job = Job.getInstance(conf, "Task III: total number of ratings per user");

    job.setJarByClass(Task3.class);
    job.setMapperClass(Task3.UserRatingMapper.class);
    job.setCombinerClass(Task3.UserRatingReducer.class);
    job.setReducerClass(Task3.UserRatingReducer.class);

    job.setOutputKeyClass(IntWritable.class);
    job.setOutputValueClass(IntWritable.class);

    TextInputFormat.addInputPath(job, new Path(otherArgs[0]));
    TextOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
