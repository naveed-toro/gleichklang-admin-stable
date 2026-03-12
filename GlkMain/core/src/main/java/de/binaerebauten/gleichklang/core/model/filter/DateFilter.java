package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.XmlLocalDateAdapter;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class DateFilter extends UserFilter
{
    @XmlAttribute
    @Column(name = "create_date", insertable = false, updatable = false)
    @NotNull
    private LocalDateTime startDate;

    @XmlAttribute
    @Column(name = "change_date" ,insertable = false, updatable = false)
    @NotNull
    private LocalDateTime endDate;

    @XmlAttribute
    @XmlJavaTypeAdapter(XmlLocalDateAdapter.class)
    @Column(name = "DTYPE" , insertable= false, updatable=false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Product.ProductType productType;

    @XmlAttribute
    @Column(name = "enum_value")
    @Enumerated(EnumType.STRING)
    private PaymentState paymentState;

    @XmlValue
    @Column(name = "number_value")
    private Long value;

    @XmlAttribute
    @Column(name = "number_value1")
    private Long value1;

    public Long getValue()
    {
        return value;
    }

    public void setValue(Long value)
    {
        this.value = value;
    }

    public Long getValue1() {
        return value1;
    }

    public void setValue1(Long value1) {
        this.value1 = value1;
    }

    public PaymentState getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(PaymentState paymentState) {
        this.paymentState = paymentState;
    }

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

    public Product.ProductType getProductType() {
        return productType;
    }

    public void setProductType(Product.ProductType productType) {
        this.productType = productType;
    }

    @Override
    public <T> T accept(FilterVisitor<T> filterVisitor)
    {
        return filterVisitor.visit(this);
    }

    @Override
    public String getName()
    {
        return UserFilterType.DATE_FILTER.toString() + ": " + startDate + " - " + endDate+ ", ProductType- " + productType + ", PaymentState- " +paymentState +", AmountGreaterThan- " +value + ",AmountLessThan- "+value1;
    }

    public static String convertToTitleCaseIteratingChars(String text) {
        StringBuilder converted = new StringBuilder();
        if(text.equalsIgnoreCase("Initial offer") || text.equalsIgnoreCase("Initiales Angebot")){
            return "InitialSubscriptionOffer";
        }
        else if(text.equalsIgnoreCase("Renewal offer") || text.equalsIgnoreCase("Verlängerungsangebot")){
            return "RenewalOffer";
        }
        else if(text.equalsIgnoreCase("Upgrade offer") || text.equalsIgnoreCase("Erweiterungsangebot")){
            return "UpgradeOffer";
        }
        else if(text.equalsIgnoreCase("Service offer") || text.equalsIgnoreCase("Dienstleistungsangebot")){
            return "ServiceOffer";
        }
        else
            return "Chargeback";
    }
}
