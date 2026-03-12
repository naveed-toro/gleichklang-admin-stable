package de.binaerebauten.gleichklang.core.model.paypaltoken;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;

@Entity
@Table(name="paypal_token")
public class PaypalToken extends BaseEntity {
    @Column(name = "token")
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public PaypalToken(){}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
