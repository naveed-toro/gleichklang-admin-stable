package de.binaerebauten.gleichklang.core.model.user;


import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_pin")
public class UserPin extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(unique = true, nullable = false)
    private Long pin;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPin() {
        return pin;
    }

    public void setPin(Long pin) {
        this.pin = pin;
    }
}
