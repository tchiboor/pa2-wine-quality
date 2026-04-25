package edu.njit.cs643.pa2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class WineDataUtils {

    public static final String[] FEATURE_COLUMNS = new String[] {
            "fixed_acidity",
            "volatile_acidity",
            "citric_acid",
            "residual_sugar",
            "chlorides",
            "free_sulfur_dioxide",
            "total_sulfur_dioxide",
            "density",
            "pH",
            "sulphates",
            "alcohol"
    };

    public static final String LABEL_COLUMN = "quality";

    public static Dataset<Row> loadWineCsv(SparkSession spark, String path) {
        Dataset<String> lines = spark.read().textFile(path);

        List<Row> rows = lines.collectAsList().stream()
                .skip(1)
                .filter(line -> line != null && !line.trim().isEmpty())
                .map(WineDataUtils::parseLineToRow)
                .collect(Collectors.toList());

        StructType schema = buildSchema();
        return spark.createDataFrame(rows, schema);
    }

    private static Row parseLineToRow(String line) {
        String cleaned = line.trim();

        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        String[] parts = cleaned.split(";");
        if (parts.length != 12) {
            throw new IllegalArgumentException("Invalid row with " + parts.length + " fields: " + line);
        }

        Object[] values = new Object[12];
        for (int i = 0; i < 11; i++) {
            values[i] = Double.parseDouble(parts[i].replace("\"", "").trim());
        }

        values[11] = Double.parseDouble(parts[11].replace("\"", "").trim());

        return RowFactory.create(values);
    }

    private static StructType buildSchema() {
        List<StructField> fields = new ArrayList<>();

        for (String col : FEATURE_COLUMNS) {
            fields.add(new StructField(col, DataTypes.DoubleType, false, Metadata.empty()));
        }

        fields.add(new StructField(LABEL_COLUMN, DataTypes.DoubleType, false, Metadata.empty()));

        return new StructType(fields.toArray(new StructField[0]));
    }
}
