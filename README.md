# CS 643 Programming Assignment 2
## Wine Quality Prediction in AWS using Apache Spark and Docker

**Student:** Trevor Henry Chiboora  
**Course:** CS 643 – Cloud Computing  
**Language:** Java  
**Framework:** Apache Spark MLlib  
**Cloud Platform:** AWS EC2 + Amazon S3  
**Container:** Docker

---

## 1. Objective
The objective of this project was to build a wine quality prediction machine learning application in AWS using Apache Spark MLlib. The assignment required:

- training the model in parallel on **4 EC2 instances**;
- using `TrainingDataset.csv` for training and `ValidationDataset.csv` for validation/tuning;
- computing **F1 score** using Spark MLlib;
- saving the trained model;
- loading the saved model in a **single-instance Spark prediction application**;
- containerizing the prediction application using **Docker**;
- publishing the code and container image.

---

## 2. Architecture
The environment used a Spark standalone cluster with:

- **1 Spark Master**
- **3 Spark Workers**
- **Amazon S3** for shared dataset and model storage
- **Docker** for packaging the final predictor

### Environment Summary
- AWS Region: `us-east-1`
- Spark Version: `3.5.1`
- Java: `OpenJDK 11`
- Build Tool: `Maven`
- OS: `Ubuntu Linux`

---

## 3. Dataset Paths
The datasets and saved model were stored in S3:

```bash
s3a://trevor-cs643-pa2/TrainingDataset.csv
s3a://trevor-cs643-pa2/ValidationDataset.csv
s3a://trevor-cs643-pa2/model/wine-quality-model
```

---

## 4. Model Development
The application was written in Java with Spark MLlib. The data pipeline used a `VectorAssembler` to combine the 11 feature columns into one `features` vector.

The following model families/configurations were tested:

| Trial | Model | Parameters | Validation F1 |
|------:|-------|------------|---------------:|
| 1 | Random Forest | 100 trees, depth 10 | 0.50382098 |
| 2 | Random Forest | 150 trees, depth 12 | 0.52684300 |
| 3 | Random Forest | 50 trees, depth 8 | 0.50342627 |
| 4 | Random Forest | 200 trees, depth 15 | 0.49927326 |
| 5 | Logistic Regression | maxIter=100, regParam=0.1, elasticNet=0.0 | **0.54413690** |

### Final Selected Model
The best model was:

- **Logistic Regression**
- `maxIter = 100`
- `regParam = 0.1`
- `elasticNetParam = 0.0`

This model achieved the **highest validation F1 score: 0.5441369018288305**.

---

## 5. Training and Prediction Commands

### Build the project
```bash
mvn clean package
```

### Train the model in parallel on the Spark cluster
```bash
$SPARK_HOME/bin/spark-submit   --class edu.njit.cs643.pa2.WineQualityTrainer   --master spark://spark-master:7077   --deploy-mode client   --conf spark.hadoop.fs.s3a.aws.credentials.provider=com.amazonaws.auth.InstanceProfileCredentialsProvider   target/pa2-wine-quality-1.0-SNAPSHOT.jar   s3a://trevor-cs643-pa2/TrainingDataset.csv   s3a://trevor-cs643-pa2/ValidationDataset.csv   s3a://trevor-cs643-pa2/model/wine-quality-model
```

### Run the saved-model predictor on one EC2 instance
```bash
$SPARK_HOME/bin/spark-submit   --class edu.njit.cs643.pa2.WineQualityPredictor   --master local[*]   --conf spark.hadoop.fs.s3a.aws.credentials.provider=com.amazonaws.auth.InstanceProfileCredentialsProvider   target/pa2-wine-quality-1.0-SNAPSHOT.jar   s3a://trevor-cs643-pa2/ValidationDataset.csv   s3a://trevor-cs643-pa2/model/wine-quality-model
```

---

## 6. Docker Packaging
The predictor application was containerized using Docker.

### Build image
```bash
docker build -t wine-quality-predictor .
```

### Tag and push to Docker Hub
```bash
docker login -u tchiboor
docker tag wine-quality-predictor tchiboor/wine-quality-predictor:latest
docker push tchiboor/wine-quality-predictor:latest
```

---

## 7. Repository and Image Links
- **GitHub:** https://github.com/tchiboor/pa2-wine-quality
- **Docker Hub:** https://hub.docker.com/r/tchiboor/wine-quality-predictor

---

## 8. Screenshots

### Spark Master UI
![Spark Cluster UI](screenshots/spark_cluster_ui.png)

### EC2 Instances
![EC2 Instances](screenshots/ec2_instances.png)

### Security Group Configuration
![Security Group](screenshots/security_group.png)

### S3 / Worker Setup
![Workers Setup](screenshots/workers_s3_setup.png)

### Best Model Result
![Best F1 Score](screenshots/model_trials_best_score.png)

### Docker Push
![Docker Push](screenshots/docker_push.png)

### GitHub Push
![GitHub Push](screenshots/github_push.png)

---

## 9. Conclusion
This project successfully completed the full assignment pipeline:

- Spark cluster deployed on **4 EC2 instances**;
- training executed in parallel;
- multiple models evaluated using **validation F1**;
- best model selected and saved to **Amazon S3**;
- saved model loaded by a **single-node predictor**;
- predictor packaged and published with **Docker**;
- code published to **GitHub**.

The best-performing model was **Logistic Regression**, with a validation F1 score of **0.5441369018288305**.
