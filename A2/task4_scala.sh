#!/bin/sh

hostname | grep ecehadoop
if [ $? -eq 1 ]; then
    echo "This script must be run on ecehadoop :("
    exit -1
fi

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
export SCALA_HOME=/usr
export HADOOP_HOME=/opt/hadoop-latest/hadoop
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop/
export SPARK_HOME=/opt/spark-latest/
#export SPARK_HOME=/opt/spark-2.4.6-bin-without-hadoop-scala-2.12/
#export SPARK_DIST_CLASSPATH=`$HADOOP_HOME/bin/hadoop classpath`
MAIN_SPARK_JAR=`ls $SPARK_HOME/jars/spark-core*.jar`
export CLASSPATH=".:$MAIN_SPARK_JAR"


echo --- Deleting
rm Task4.jar
rm Task4*.class

echo --- Compiling
$SCALA_HOME/bin/scalac -J-Xmx1g Task4.scala
if [ $? -ne 0 ]; then
    exit
fi

echo --- Jarring
$JAVA_HOME/bin/jar -cf Task4.jar Task4*.class

echo --- Running
INPUT=/user/${USER}/smalldata.txt
OUTPUT=/user/${USER}/Task4_scala_output/

$HADOOP_HOME/bin/hdfs dfs -rm -R $OUTPUT
$HADOOP_HOME/bin/hdfs dfs -copyFromLocal sample_input/smalldata.txt /user/${USER}/
time $SPARK_HOME/bin/spark-submit --master yarn --class Task4 --driver-memory 4g --executor-memory 4g Task4.jar $INPUT $OUTPUT
$HADOOP_HOME/bin/hdfs dfs -get $OUTPUT /home/vskottur/ece454/assignments/A2/

export HADOOP_ROOT_LOGGER="WARN"
$HADOOP_HOME/bin/hdfs dfs -ls $OUTPUT
$HADOOP_HOME/bin/hdfs dfs -cat $OUTPUT*

cat Task4_scala_output/part-00000 | sort > sample_output/Task4_scala_output.txt
# cat sample_output/Task4_sample.txt | sort > sample_output/Task4_sample_ordered.txt
echo "BEFORE"
diff sample_output/Task4_scala_output.txt sample_output/Task4_sample.txt
echo "DONE"
