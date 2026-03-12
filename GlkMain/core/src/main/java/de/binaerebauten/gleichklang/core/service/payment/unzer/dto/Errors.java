package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

public class Errors {

    private String code;

    private String customerMessage;

    public void setCode(String code){
        this.code = code;
    }
    public String getCode(){
        return this.code;
    }
    public void setCustomerMessage(String customerMessage){
        this.customerMessage = customerMessage;
    }
    public String getCustomerMessage(){
        return this.customerMessage;
    }
}
