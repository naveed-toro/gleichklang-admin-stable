package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChargeAuthorizeCard {

    private String amount;

    private String currency;

    private String card3ds;

    private String returnUrl;

    private String cardHolder;

    private Resources resources;

    public void setAmount(String amount){
        this.amount = amount;
    }
    public String getAmount(){
        return this.amount;
    }
    public void setCurrency(String currency){
        this.currency = currency;
    }
    public String getCurrency(){
        return this.currency;
    }
    public void setCard3ds(String card3ds){
        this.card3ds = card3ds;
    }
    public String getCard3ds(){
        return this.card3ds;
    }
    public void setReturnUrl(String returnUrl){
        this.returnUrl = returnUrl;
    }
    public String getReturnUrl(){
        return this.returnUrl;
    }
    public void setCardHolder(String cardHolder){
        this.cardHolder = cardHolder;
    }
    public String getCardHolder(){
        return this.cardHolder;
    }
    public void setResources(Resources resources){
        this.resources = resources;
    }
    public Resources getResources(){
        return this.resources;
    }
}
