package de.binaerebauten.gleichklang.core.model.payment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import java.util.Currency;

/**
 * Enumerates the available currencies.
 * <p/>
 * This enum is named {@link AvailableCurrency} to avoid confusion with
 * the {@link java.util.Currency} class.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public enum AvailableCurrency
{
	EUR;

	public Currency getCurrency()
	{
		return Currency.getInstance(name());
	}
}
