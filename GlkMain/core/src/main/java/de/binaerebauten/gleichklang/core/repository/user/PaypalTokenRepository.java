package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.paypaltoken.PaypalToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface PaypalTokenRepository extends JpaRepository<PaypalToken, Long>
{

    @Transactional
    @Modifying
    @Query("delete FROM PaypalToken")
    void deletePaypalToken();
}
