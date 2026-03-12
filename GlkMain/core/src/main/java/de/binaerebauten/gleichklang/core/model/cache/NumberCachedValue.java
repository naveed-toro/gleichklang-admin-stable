package de.binaerebauten.gleichklang.core.model.cache;


import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by Domi on 04.10.2016.
 */
@Entity
public class NumberCachedValue extends CachedValue<Double> {

    @Column(name = "number_value")
    private Double numberValue;

    public NumberCachedValue() {
    }

    public NumberCachedValue(String cacheKey, Double doubleValue) {
        this.numberValue = doubleValue;
        setCacheKey(cacheKey);
    }

    @Override
    public Double getValue() {
        return this.numberValue;
    }

    @Override
    public void setValue(Double value) {
        this.numberValue = value;
    }
}