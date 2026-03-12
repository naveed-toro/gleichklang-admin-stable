package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDetails
{
    private String cardType;

    private String account;

    private String countryIsoA2;

    private String countryName;

    private String issuerName;

    private String issuerUrl;

    private String issuerPhoneNumber;

    public void setCardType(String cardType){
        this.cardType = cardType;
    }
    public String getCardType(){
        return this.cardType;
    }
    public void setAccount(String account){
        this.account = account;
    }
    public String getAccount(){
        return this.account;
    }
    public void setCountryIsoA2(String countryIsoA2){
        this.countryIsoA2 = countryIsoA2;
    }
    public String getCountryIsoA2(){
        return this.countryIsoA2;
    }
    public void setCountryName(String countryName){
        this.countryName = countryName;
    }
    public String getCountryName(){
        return this.countryName;
    }
    public void setIssuerName(String issuerName){
        this.issuerName = issuerName;
    }
    public String getIssuerName(){
        return this.issuerName;
    }
    public void setIssuerUrl(String issuerUrl){
        this.issuerUrl = issuerUrl;
    }
    public String getIssuerUrl(){
        return this.issuerUrl;
    }
    public void setIssuerPhoneNumber(String issuerPhoneNumber){
        this.issuerPhoneNumber = issuerPhoneNumber;
    }
    public String getIssuerPhoneNumber(){
        return this.issuerPhoneNumber;
    }
}
