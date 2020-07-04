#!/bin/bash

hostname | grep ecehadoop
if [ $? -eq 1 ]; then
    echo "This script must be run on ecehadoop :("
    exit -1
fi

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
export HADOOP_HOME=/opt/hadoop-latest/hadoop
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop/
export CLASSPATH=`$HADOOP_HOME/bin/hadoop classpath`
export PATH=${PATH}:${HADOOP_HOME}/bin


echo --- Deleting
rm Task1.jar
rm Task1*.class

echo --- Compiling
$JAVA_HOME/bin/javac Task1.java
if [ $? -ne 0 ]; then
    exit
fi

echo --- Jarring
$JAVA_HOME/bin/jar -cf Task1.jar Task1*.class

echo --- Running
INPUT=/user/${USER}/smalldata.txt
OUTPUT=/user/${USER}/Task1_hadoop_output/
$HADOOP_HOME/bin/hdfs dfs -rm -R $OUTPUT
$HADOOP_HOME/bin/hdfs dfs -copyFromLocal sample_input/smalldata.txt /user/${USER}/
time $HADOOP_HOME/bin/yarn jar Task1.jar Task1 -D mapreduce.map.java.opts=-Xmx4g $INPUT $OUTPUT
$HADOOP_HOME/bin/hdfs dfs -get $OUTPUT /home/vskottur/ece454/assignments/A2/
$HADOOP_HOME/bin/hdfs dfs -ls $OUTPUT
$HADOOP_HOME/bin/hdfs dfs -cat $OUTPUT*

cat Task1_hadoop_output/part-m-00000 | sort -r > sample_output/Task1_output.txt
cat sample_output/Task1_sample.txt | sort -r > sample_output/Task1_sample_ordered.txt
echo "BEFORE"
diff sample_output/Task1_output.txt sample_output/Task1_sample_ordered.txt
echo "DONE"