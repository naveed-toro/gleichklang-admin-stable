package de.binaerebauten.gleichklang.adminweb.service.payment;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.ServiceOffer;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Unit tests for {@link ProductAdminService}.
 */
public class ProductAdminServiceTest extends BasePersistenceTest
{
	private static final String TEST_PRODUCT_I18N_KEY = "test_product";

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	@Autowired
	private ProductRepository<Product> productRepository;

	@Autowired
	private I18NRepository i18NRepository;

	private ProductAdminService productAdminService;

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	private ServiceOffer testProduct;
	private I18NEntity i18NEntity;

	@Before
	public void setup()
	{
		testProduct = paymentEntityFactory.createServiceOffer("Test Product", LocalDateTime.now(), 12, RecommendationCategory.PARTNERSHIP);

		i18NEntity = defaultEntityFactory.
				createDefaultI18NEntry(I18NEntity.BaseName.PRODUCT_DESCRIPTION, I18NEntity.Language.DE, TEST_PRODUCT_I18N_KEY, "Test Produkt");
		productAdminService = new ProductAdminService(productRepository, i18NRepository);
	}

	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void testSave() throws ValidationException
	{
		productAdminService.save(testProduct, Collections.singleton(i18NEntity));

		productAdminService.save(testProduct, Collections.singleton(i18NEntity)); // save it twice to trigger revalidation
	}

	@Test
	public void testSave_UniqueValidationException() throws ValidationException
	{
		productAdminService.save(testProduct, Collections.singleton(i18NEntity));

		productAdminService.save(testProduct, Collections.singleton(i18NEntity));

		I18NEntity duplicateI18NEntity	 = defaultEntityFactory.
				createDefaultI18NEntry(I18NEntity.BaseName.PRODUCT_DESCRIPTION, I18NEntity.Language.DE, TEST_PRODUCT_I18N_KEY, "Test Produkt");

		thrown.expect(UniqueValidationException.class);

		productAdminService.save(testProduct, Collections.singleton(duplicateI18NEntity));
	}
}
