package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

    private String code;

    private String merchant;

    private String customer;

    public void setCode(String code){
        this.code = code;
    }
    public String getCode(){
        return this.code;
    }
    public void setMerchant(String merchant){
        this.merchant = merchant;
    }
    public String getMerchant(){
        return this.merchant;
    }
    public void setCustomer(String customer){
        this.customer = customer;
    }
    public String getCustomer(){
        return this.customer;
    }
}
