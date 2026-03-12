package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Processing
{
    private String uniqueId;

    private String shortId;

    private String traceId;

    private String iban;
    private String bic;
    private String identification;

    public void setUniqueId(String uniqueId){
        this.uniqueId = uniqueId;
    }
    public String getUniqueId(){
        return this.uniqueId;
    }
    public void setShortId(String shortId){
        this.shortId = shortId;
    }
    public String getShortId(){
        return this.shortId;
    }
    public void setTraceId(String traceId){
        this.traceId = traceId;
    }
    public String getTraceId(){
        return this.traceId;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }
}

