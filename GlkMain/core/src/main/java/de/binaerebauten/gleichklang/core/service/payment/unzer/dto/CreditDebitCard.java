package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditDebitCard {

    private String number;
    private String iban;
    private String expiryDate;
    private String debugEnabled;

    private String cvc;

    private String threeDS;

    private String cardHolder;

    public String getDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(String debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void setNumber(String number){
        this.number = number;
    }
    public String getNumber(){
        return this.number;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setExpiryDate(String expiryDate){
        this.expiryDate = expiryDate;
    }
    public String getExpiryDate(){
        return this.expiryDate;
    }
    public void setCvc(String cvc){
        this.cvc = cvc;
    }
    public String getCvc(){
        return this.cvc;
    }
    public void setThreeDS(String threeDS){
        this.threeDS = threeDS;
    }
    public String getThreeDS(){
        return this.threeDS;
    }
    public void setCardHolder(String cardHolder){
        this.cardHolder = cardHolder;
    }
    public String getCardHolder(){
        return this.cardHolder;
    }

}
