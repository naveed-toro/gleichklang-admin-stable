package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * Repository for the {@link Product} entities.
 */
public interface ProductRepository<T extends Product> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(p.id, p.i18nKey, TYPE(p)) FROM #{#entityName} p WHERE p.i18nKey IS NOT NULL")
    List<IdKeyPairResult> getAllAsIdKeyPair();

    @Query(value = "select * from product p inner join invoice_item i on\n" +
            "p.id = i.product_id inner join invoice inv on \n" +
            "i.invoice_id = inv.id inner join payment pp  on pp.invoice_id = inv.id where pp.user_id = ?1 and pp.current = 1" ,nativeQuery = true)
    Product findProduct(Long userId);

    @Query("FROM Product p WHERE p.i18nKey IS NOT NULL")
    Set<Product> getAll();

}
