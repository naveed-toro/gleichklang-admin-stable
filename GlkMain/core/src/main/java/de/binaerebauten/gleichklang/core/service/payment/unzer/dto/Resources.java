package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resources {
    private String customerId;

    private String typeId;

    private String paymentId;

    private String metadataId;

    private String traceId;

    private String basketId;

    private String payPageId;

    public void setCustomerId(String customerId){
        this.customerId = customerId;
    }
    public String getCustomerId(){
        return this.customerId;
    }
    public void setTypeId(String typeId){
        this.typeId = typeId;
    }
    public String getTypeId(){
        return this.typeId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getMetadataId() {
        return metadataId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public String getBasketId() {
        return basketId;
    }

    public void setBasketId(String basketId) {
        this.basketId = basketId;
    }

    public String getPayPageId() {
        return payPageId;
    }

    public void setPayPageId(String payPageId) {
        this.payPageId = payPageId;
    }
}
