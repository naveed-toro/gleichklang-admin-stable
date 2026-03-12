package de.binaerebauten.gleichklang.core.service.payment.unzer.dto;

public class Amount {

    private String currency;
    private String total;
    private Details details;

    public String charged;
    public String canceled;
    public String remaining;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public String getCharged() {
        return charged;
    }

    public void setCharged(String charged) {
        this.charged = charged;
    }

    public String getCanceled() {
        return canceled;
    }

    public void setCanceled(String canceled) {
        this.canceled = canceled;
    }

    public String getRemaining() {
        return remaining;
    }

    public void setRemaining(String remaining) {
        this.remaining = remaining;
    }
}
