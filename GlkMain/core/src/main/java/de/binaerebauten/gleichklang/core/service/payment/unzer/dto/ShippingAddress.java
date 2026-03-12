package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShippingAddress {

    private String name;

    private String street;

    private String state;

    private String zip;

    private String city;

    private String country;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setStreet(String street){
        this.street = street;
    }
    public String getStreet(){
        return this.street;
    }
    public void setState(String state){
        this.state = state;
    }
    public String getState(){
        return this.state;
    }
    public void setZip(String zip){
        this.zip = zip;
    }
    public String getZip(){
        return this.zip;
    }
    public void setCity(String city){
        this.city = city;
    }
    public String getCity(){
        return this.city;
    }
    public void setCountry(String country){
        this.country = country;
    }
    public String getCountry(){
        return this.country;
    }
}
