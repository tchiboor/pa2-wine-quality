FROM eclipse-temurin:11-jre-jammy

ENV SPARK_VERSION=3.5.1
ENV HADOOP_PROFILE=hadoop3
ENV SPARK_HOME=/opt/spark
ENV PATH=$PATH:/opt/spark/bin:/opt/spark/sbin

RUN apt-get update && apt-get install -y wget bash procps python3 && rm -rf /var/lib/apt/lists/*

RUN wget https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-${HADOOP_PROFILE}.tgz -O /tmp/spark.tgz \
    && tar -xzf /tmp/spark.tgz -C /opt \
    && mv /opt/spark-${SPARK_VERSION}-bin-${HADOOP_PROFILE} /opt/spark \
    && rm /tmp/spark.tgz

RUN wget -P /opt/spark/jars https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.4/hadoop-aws-3.3.4.jar \
    && wget -P /opt/spark/jars https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.262/aws-java-sdk-bundle-1.12.262.jar

WORKDIR /app

COPY target/pa2-wine-quality-1.0-SNAPSHOT.jar /app/app.jar
COPY run_predict.sh /app/run_predict.sh

RUN chmod +x /app/run_predict.sh

CMD ["/app/run_predict.sh"]
