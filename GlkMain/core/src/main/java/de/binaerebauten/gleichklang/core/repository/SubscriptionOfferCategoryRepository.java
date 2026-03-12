package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOfferCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Project: Import Export
 * Created by Domi on 14.06.2016.
 */
@Repository
public interface SubscriptionOfferCategoryRepository extends JpaRepository<SubscriptionOfferCategory, Long> {

    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(soc.id, CONCAT(so.i18nKey, '_', soc.category)) FROM SubscriptionOfferCategory soc JOIN soc.subscriptionOffer so")
    List<IdKeyPairResult> getAllAsIdKeyPair();

}
