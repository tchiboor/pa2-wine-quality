package edu.njit.cs643.pa2;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class WineQualityPredictor {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: WineQualityPredictor <input_csv> <saved_model_dir>");
            System.exit(1);
        }

        String inputPath = args[0];
        String modelPath = args[1];

        SparkSession spark = SparkSession.builder()
                .appName("WineQualityPredictor")
                .master("local[*]")
                .getOrCreate();

        try {
            System.out.println("Loading input dataset from: " + inputPath);
            Dataset<Row> input = WineDataUtils.loadWineCsv(spark, inputPath);

            System.out.println("Loading saved model from: " + modelPath);
            PipelineModel model = PipelineModel.load(modelPath);

            Dataset<Row> predictions = model.transform(input);

            predictions.select(
                    WineDataUtils.LABEL_COLUMN,
                    "prediction"
            ).show(50, false);

            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setLabelCol(WineDataUtils.LABEL_COLUMN)
                    .setPredictionCol("prediction")
                    .setMetricName("f1");

            double f1 = evaluator.evaluate(predictions);

            System.out.println("======================================");
            System.out.println("Prediction F1 Score = " + f1);
            System.out.println("======================================");

        } finally {
            spark.stop();
        }
    }
}
