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
# INPUT=/tmp/manualtestcase.txt
# INPUT=/tmp/hsperfdata_vskottur/smalldata.txt
INPUT=ece454/assignments/A2/sample_input/smalldata.txt
OUTPUT=ece454/assignments/A2/Task1_hadoop_output

$HADOOP_HOME/bin/hdfs dfs -rm -R $OUTPUT
#$HADOOP_HOME/bin/hdfs dfs -copyFromLocal sample_input/smalldata.txt /user/${USER}/
time $HADOOP_HOME/bin/yarn jar Task1.jar Task1 -D mapreduce.map.java.opts=-Xmx4g $INPUT $OUTPUT

$HADOOP_HOME/bin/hdfs dfs -ls $OUTPUT
$HADOOP_HOME/bin/hdfs dfs -cat $OUTPUT/*

# echo --- Running
# INPUT=/tmp/hsperfdata_vskottur/smalldata.txt
# OUTPUT=Task1_hadoop_output

# rm -fr $OUTPUT
# $HADOOP_HOME/bin/hadoop jar Task1.jar Task1 $INPUT $OUTPUT

# cat $OUTPUT/*