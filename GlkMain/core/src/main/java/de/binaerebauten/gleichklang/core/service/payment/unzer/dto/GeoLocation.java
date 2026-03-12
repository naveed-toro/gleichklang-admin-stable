package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoLocation {

    private String clientIp;
    private String countryCode;
    private String countryIsoA2;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryIsoA2() {
        return countryIsoA2;
    }

    public void setCountryIsoA2(String countryIsoA2) {
        this.countryIsoA2 = countryIsoA2;
    }
}
