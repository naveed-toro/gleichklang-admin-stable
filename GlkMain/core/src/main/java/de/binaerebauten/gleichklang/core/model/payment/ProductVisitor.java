package de.binaerebauten.gleichklang.core.model.payment;

/**
 * Visitor interface for {@link Product} types.
 * Just visits the concrete sub classes of product.
 */
public interface ProductVisitor<T>
{
	T visit(InitialSubscriptionOffer initialSubscriptionOffer);

	T visit(RenewalOffer renewalOffer);

	T visit(UpgradeOffer upgradeOffer);

	T visit(ServiceOffer serviceOffer);

	T visit(Chargeback chargeback);
}
