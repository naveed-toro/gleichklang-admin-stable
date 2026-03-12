package de.binaerebauten.gleichklang.core.service.report;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

/**
 * Affinity User Result for a single affinity category.
 */
@SqlResultSetMapping(
        name = "AffinityUserResultMapping",
        classes = @ConstructorResult(
                targetClass = AffinityUserResult.class,
                columns = {
                        @ColumnResult(name = "summe", type = Integer.class),
                        @ColumnResult(name = "category", type = String.class)
                }
        )
)
public class AffinityUserResult {

    private String category;
    private Integer summe;

    public AffinityUserResult(String category, Integer sum) {
        this.category = category;
        this.summe = sum;
    }

    public String getCategory() {
        return category;
    }

    public Integer getSum() {
        return summe;
    }
}
