package de.binaerebauten.gleichklang.adminweb.service.payment;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.ServiceOffer;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.UpgradeOffer;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.util.Collection;

/**
 * Service for the product entities {@link de.binaerebauten.gleichklang.core.model.payment.Product}
 * in the admin-web module.
 */
@Service
public class ProductAdminService
{
	private static final Logger LOG = LoggerFactory.getLogger(ProductAdminService.class);

	private final ProductRepository<Product> productRepository;

	private final I18NRepository i18NRepository;

	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param productRepository
	 * @param i18NRepository
	 */
	@Autowired
	public ProductAdminService(ProductRepository<Product> productRepository, I18NRepository i18NRepository)
	{
		this.productRepository = productRepository;
		this.i18NRepository = i18NRepository;
	}

	/**
	 * Saves the given product together with the given translations.
	 *
	 * @param product          the non-null product
	 * @param i18nDescriptions the non-null translations
	 * @throws UniqueValidationException
	 */
	@CheckedTransactional
	public void save(Product product, Collection<I18NEntity> i18nDescriptions)
			throws ValidationException
	{
		try
		{
			i18NRepository.save(i18nDescriptions);
			i18NRepository.flush();
			if (product instanceof SubscriptionOffer)
			{
				SubscriptionOffer subscriptionOffer = (SubscriptionOffer) product;
				// make sure that the subscription offer is correctly set on the subscription offer category
				subscriptionOffer.getCategories().forEach(c -> c.setSubscriptionOffer(subscriptionOffer));
			}
			else if (product instanceof ServiceOffer)
			{
				ServiceOffer serviceOffer = (ServiceOffer) product;
				// make sure that the service offer is correctly set on the service offer required category
				serviceOffer.getRequiredCategories().forEach(c -> c.setServiceOffer(serviceOffer));
			}
			productRepository.saveAndFlush(product);
		}
		catch (final DataIntegrityViolationException e)
		{
			throw new UniqueValidationException(e);
		}
		catch (final ConstraintViolationException e)
		{
			throw new ValidationException(e);
		}
	}
	
	@Transactional
	public Product fetchProduct(Product product)
	{
		if(product == null || product.getId() == null) return null;
		
		product = productRepository.findOne(product.getId());
		
		if(product instanceof UpgradeOffer)
		{
			Hibernate.initialize(((UpgradeOffer)product).getSubscriptionOffers());
		}
		
		return product;
	}
}
