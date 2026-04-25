#!/bin/bash
set -e

INPUT_FILE=${1:-s3a://trevor-cs643-pa2/ValidationDataset.csv}
MODEL_PATH=${2:-s3a://trevor-cs643-pa2/model/wine-quality-model}

$SPARK_HOME/bin/spark-submit \
  --class edu.njit.cs643.pa2.WineQualityPredictor \
  --master local[*] \
  --conf spark.hadoop.fs.s3a.aws.credentials.provider=com.amazonaws.auth.EnvironmentVariableCredentialsProvider \
  /app/app.jar \
  "$INPUT_FILE" \
  "$MODEL_PATH"
