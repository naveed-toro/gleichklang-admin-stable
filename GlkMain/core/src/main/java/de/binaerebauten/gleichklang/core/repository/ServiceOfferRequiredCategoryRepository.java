package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.payment.ServiceOfferRequiredCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Project: Import Export
 * Created by Domi on 14.06.2016.
 */
public interface ServiceOfferRequiredCategoryRepository extends JpaRepository<ServiceOfferRequiredCategory, Long> {

    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(soc.id, CONCAT(so.i18nKey, '_', soc.category)) FROM ServiceOfferRequiredCategory soc JOIN soc.serviceOffer so")
    List<IdKeyPairResult> getAllAsIdKeyPair();

}
