#!/bin/bash
JAVA_HOME="/usr/local/jdk1.7.0_80"
JAVA_OPTS="-Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"
MAIN_CLASS="com.ewfresh.pay.worker.Start"
RUN_JAR="pay-worker-1.0.jar"
CURRENT=`pwd`;
cd $CURRENT;
jarFile='';

for jar in $CURRENT/lib/*.jar; do
    jarFile=$jarFile:'/lib/':$jar
done

echo "jarFile==>"..$jarFile
echo "启动成功"

nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $jarFile:./$RUN_JAR:. $MAIN_CLASS &

