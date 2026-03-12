package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivateDeactiveSubscriptionRepository extends JpaRepository<ActiveDeactiveSubscription, Long>, JpaSpecificationExecutor<ActiveDeactiveSubscription> {

}
