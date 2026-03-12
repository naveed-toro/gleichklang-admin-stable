package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.time.LocalDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class ActivatedProlongationsFilter extends UserFilter {

    @XmlAttribute
    @Column(name = "change_date", insertable = false, updatable = false)
    @NotNull
    private LocalDateTime startDate;

    @XmlAttribute
    @Column(name = "create_date", insertable = false, updatable = false)
    @NotNull
    private LocalDateTime endDate;

   /* @XmlValue
    @Column(name = "boolean_value")
    @NotNull
    private boolean	value;*/


    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

   /* public boolean isValue()
    {
        return value;
    }

    public void setValue(boolean value)
    {
        this.value = value;
    }*/



    @Override
    public <T> T accept(FilterVisitor<T> filterVisitor)
    {
        return filterVisitor.visit(this);
    }



    @Override
    public String getName()
    {
         return UserFilterType.ACTIVATED_PROLONGATION_DATE_RANGE_FILTER.toString() + ": " + startDate + " - " + endDate/*+"-"+value*/;
         //return "";
    }


}
