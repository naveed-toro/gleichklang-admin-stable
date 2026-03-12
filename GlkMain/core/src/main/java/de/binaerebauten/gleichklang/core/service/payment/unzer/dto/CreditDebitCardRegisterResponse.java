package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditDebitCardRegisterResponse {
    private String id;

    private String method;

    private String number;

    private String iban;

    private String brand;

    private String cvc;

    private String expiryDate;

    private boolean threeDS;

    private String cardHolder;

    private String holder;

    private String bic;

    private CardDetails cardDetails;

    private GeoLocation geoLocation;

    private Processing processing;

    private String recurring;

    private String email;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setMethod(String method){
        this.method = method;
    }
    public String getMethod(){
        return this.method;
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

    public void setBrand(String brand){
        this.brand = brand;
    }
    public String getBrand(){
        return this.brand;
    }
    public void setCvc(String cvc){
        this.cvc = cvc;
    }
    public String getCvc(){
        return this.cvc;
    }
    public void setExpiryDate(String expiryDate){
        this.expiryDate = expiryDate;
    }
    public String getExpiryDate(){
        return this.expiryDate;
    }

    public boolean isThreeDS() {
        return threeDS;
    }

    public void setThreeDS(boolean threeDS) {
        this.threeDS = threeDS;
    }

    public void setCardHolder(String cardHolder){
        this.cardHolder = cardHolder;
    }
    public String getCardHolder(){
        return this.cardHolder;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public void setCardDetails(CardDetails cardDetails){
        this.cardDetails = cardDetails;
    }
    public CardDetails getCardDetails(){
        return this.cardDetails;
    }
    public void setGeoLocation(GeoLocation geoLocation){
        this.geoLocation = geoLocation;
    }

    public Processing getProcessing() {
        return processing;
    }

    public void setProcessing(Processing processing) {
        this.processing = processing;
    }

    public String getRecurring() {
        return recurring;
    }

    public void setRecurring(String recurring) {
        this.recurring = recurring;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public GeoLocation getGeoLocation(){
        return this.geoLocation;
    }
}
