package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;

@Entity
@Table(name = "active_inactive")
public class ActiveDeactiveSubscription extends BaseEntity{

    @Column(name = "type")
    private String type;

   @Column(name="user_type")
   private String userType;


    @ManyToOne // default to load user eager is fine here!
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne // default to load user eager is fine here!
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name="alias")
    private String alias;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}