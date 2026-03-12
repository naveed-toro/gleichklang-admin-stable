package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Before;

/**
 * Base test for the subscription offer sub classes.
 */
public abstract class BaseSubscriptionOfferTest
{
	protected User user;
	protected PaymentEntityFactory paymentEntityFactory;

	@Before
	public void setup()
	{
		paymentEntityFactory = new PaymentEntityFactory();

		user = new User();
	}
}
