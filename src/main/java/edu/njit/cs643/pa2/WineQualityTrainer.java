package edu.njit.cs643.pa2;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ml.classification.LogisticRegression;

public class WineQualityTrainer {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: WineQualityTrainer <training_csv> <validation_csv> <model_output_dir>");
            System.exit(1);
        }

        String trainingPath = args[0];
        String validationPath = args[1];
        String modelOutputPath = args[2];

        SparkSession spark = SparkSession.builder()
                .appName("WineQualityTrainer")
                .getOrCreate();

        try {
            System.out.println("Loading training dataset from: " + trainingPath);
            Dataset<Row> training = WineDataUtils.loadWineCsv(spark, trainingPath);

            System.out.println("Loading validation dataset from: " + validationPath);
            Dataset<Row> validation = WineDataUtils.loadWineCsv(spark, validationPath);

            System.out.println("Training rows: " + training.count());
            System.out.println("Validation rows: " + validation.count());

            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(WineDataUtils.FEATURE_COLUMNS)
                    .setOutputCol("features");

//            RandomForestClassifier rf = new RandomForestClassifier()
//                    .setLabelCol(WineDataUtils.LABEL_COLUMN)
//                   .setFeaturesCol("features")
//                    .setPredictionCol("prediction")
//                    .setNumTrees(200)
//                    .setMaxDepth(15)
//                    .setSeed(42);

//            Pipeline pipeline = new Pipeline()
//                    .setStages(new PipelineStage[] { assembler, rf });

            LogisticRegression lr = new LogisticRegression()
                 .setLabelCol(WineDataUtils.LABEL_COLUMN)
        .setFeaturesCol("features")
        .setPredictionCol("prediction")
        .setMaxIter(100)
        .setRegParam(0.1)
        .setElasticNetParam(0.0);

Pipeline pipeline = new Pipeline()
        .setStages(new PipelineStage[] { assembler, lr });

            System.out.println("Training model...");
            PipelineModel model = pipeline.fit(training);

            System.out.println("Saving model to: " + modelOutputPath);
            model.write().overwrite().save(modelOutputPath);

            Dataset<Row> predictions = model.transform(validation);

            predictions.select(
                    "features",
                    WineDataUtils.LABEL_COLUMN,
                    "prediction"
            ).show(20, false);

            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setLabelCol(WineDataUtils.LABEL_COLUMN)
                    .setPredictionCol("prediction")
                    .setMetricName("f1");

            double f1 = evaluator.evaluate(predictions);

            System.out.println("======================================");
            System.out.println("Validation F1 Score = " + f1);
            System.out.println("======================================");

        } finally {
            spark.stop();
        }
    }
}
