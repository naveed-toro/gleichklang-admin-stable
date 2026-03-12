package de.binaerebauten.gleichklang.core.service.report;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

/**
 * Affinity User Result for a single affinity category.
 */
@SqlResultSetMapping(
        name = "AffinityResultMapping",
        classes = @ConstructorResult(
                targetClass = AffinityResult.class,
                columns = {
                        @ColumnResult(name = "mean", type = Float.class),
                        @ColumnResult(name = "standardDeviation", type = Float.class),
                        @ColumnResult(name = "category", type = String.class)
                }
        )
)
public class AffinityResult {

    private Float mean;
    private Float standardDeviation;
    private String category;

    public AffinityResult(String category) {
        this.mean = 0.0f;
        this.standardDeviation = 0.0f;
        this.category = category;

    }

    public AffinityResult(Float mean, Float standardDeviation, String category) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.category = category;
    }

    public Float getMean() {
        return mean;
    }

    public Float getStandardDeviation() {
        return standardDeviation;
    }

    public String getCategory() {
        return category;
    }

    public void setMean(Float mean) {
        this.mean = mean;
    }

    public void setStandardDeviation(Float standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
