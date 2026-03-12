package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Customer {

    public String id;

    private String lastname;

    private String firstname;

    private String salutation;

    private String company;

    private String customerId;

    private String birthDate;

    private String email;

    private String phone;

    private String mobile;

    private GeoLocation geoLocation;

    private CompanyInfo companyInfo;

    private BillingAddress billingAddress;

    private ShippingAddress shippingAddress;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastname(String lastname){
        this.lastname = lastname;
    }
    public String getLastname(){
        return this.lastname;
    }
    public void setFirstname(String firstname){
        this.firstname = firstname;
    }
    public String getFirstname(){
        return this.firstname;
    }
    public void setSalutation(String salutation){
        this.salutation = salutation;
    }
    public String getSalutation(){
        return this.salutation;
    }
    public void setCompany(String company){
        this.company = company;
    }
    public String getCompany(){
        return this.company;
    }
    public void setCustomerId(String customerId){
        this.customerId = customerId;
    }
    public String getCustomerId(){
        return this.customerId;
    }
    public void setBirthDate(String birthDate){
        this.birthDate = birthDate;
    }
    public String getBirthDate(){
        return this.birthDate;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public String getEmail(){
        return this.email;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    public String getPhone(){
        return this.phone;
    }
    public void setMobile(String mobile){
        this.mobile = mobile;
    }
    public String getMobile(){
        return this.mobile;
    }
    public void setBillingAddress(BillingAddress billingAddress){
        this.billingAddress = billingAddress;
    }
    public BillingAddress getBillingAddress(){
        return this.billingAddress;
    }
    public void setShippingAddress(ShippingAddress shippingAddress){
        this.shippingAddress = shippingAddress;
    }
    public ShippingAddress getShippingAddress(){
        return this.shippingAddress;
    }

    public void setGeoLocation(GeoLocation geoLocation){
        this.geoLocation = geoLocation;
    }
    public GeoLocation getGeoLocation(){
        return this.geoLocation;
    }
    public void setCompanyInfo(CompanyInfo companyInfo){
        this.companyInfo = companyInfo;
    }
    public CompanyInfo getCompanyInfo(){
        return this.companyInfo;
    }
}
