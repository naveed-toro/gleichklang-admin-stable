package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription;
import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription_;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ActivateDeactiveSubscriptionRepository;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

@Service
public class ActivateDeactivateService {

    @Autowired
    private ActivateDeactiveSubscriptionRepository repository;

        public LazyBeanItemContainer.LazyBeanFilteredItemsHandler<ActiveDeactiveSubscription> activeDeactiveHandler(User user, Subscription subscription)
        {
            final Specifications<ActiveDeactiveSubscription> specs = Specifications.where((root, query, cb) -> cb.equal(root.get(ActiveDeactiveSubscription_.subscription), subscription));
            return (specification, pageable) -> repository.findAll(specs.and(specification), pageable);
        }
    }

