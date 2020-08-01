import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

public class A4Application {

    public static void main(String[] args) throws Exception {
		// do not modify the structure of the command line
		String bootstrapServers = args[0];
		String appName = args[1];
		String studentTopic = args[2];
		String classroomTopic = args[3];
		String outputTopic = args[4];
		String stateStoreDir = args[5];

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.STATE_DIR_CONFIG, stateStoreDir);

		// add code here if you need any additional configuration options

		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> student = builder.stream(studentTopic);
		KStream<String, String> classroom = builder.stream(classroomTopic);

		// {studentId: classroomId}
		KTable<String, String> student2Classroom = student.groupByKey()
														   .reduce((aggregateValue, newValue) -> newValue);
		
		// {classroomId: occupancy}
		KTable<String, Long> classroom2Occupancy = student2Classroom.groupBy((studentId, roomId) -> new KeyValue<>(roomId, studentId))
																	.count();

		// {classroomId: capacity}
		KTable<String, String> classroom2Capacity = classroom.groupByKey()
															 .reduce((aggregateValue, newValue) -> newValue);

		// {clasroomId: (occupancy, capacity)}
		KTable<String, String> classroomStatus = classroom2Occupancy.join(classroom2Capacity, (occupancy, capacity) -> {
			return occupancy.toString() + "," + capacity.toString();
		});

		KTable<String, String> output = classroomStatus.toStream()
													   .groupByKey()
													   .aggregate(() -> null, (key, newValue, oldValue) -> {
														   int occupancy = Integer.parseInt(newValue.split(",")[0]);
														   int capacity = Integer.parseInt(newValue.split(",")[1]);

														   if (occupancy > capacity) {
															   return String.valueOf(occupancy);
														   } else {
															   if (StringUtils.isNumeric(oldValue)) {
																   return "OK";
															   } else {
																   return null;
															   }
														   }
													   });

		Serde<String> serdeString = Serdes.String();
		output.toStream().filter((key, value) -> value != null)
						 .to(outputTopic, Produced.with(serdeString, serdeString));

		KafkaStreams streams = new KafkaStreams(builder.build(), props);

		// this line initiates processing
		streams.start();

		// shutdown hook for Ctrl+C
		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
