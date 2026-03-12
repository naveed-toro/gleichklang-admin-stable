package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a heidelpay transaction id. It has a type {@link
 * de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type}
 * and an optional id which either references an external payment {@link
 * de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type#PAYMENT},
 * {@link de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type#REGISTRATION}
 * or references an external subscription payment {@link
 * de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type#SUBSCRIPTION_RENEWAL}
 * or references an external payment registration {@link
 * de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type#CHANGE_REGISTRATION}
 * <p>
 * Additionally each transaction id hast a {@link #getFromHostId()} which
 * identifies from which host this transaction was send. This alows us to use
 * the same heidelpay channel for different hosts. It makes maintenance and
 * testing easier.
 * <p>
 * ATTENTION: Right now we use the host name directly as host id {@link
 * #getHostId()}, but as soon as we deploy to nodes with different host names we
 * have to find a better way.
 */
public class TransactionID
{
	private static final Logger LOG = LoggerFactory.getLogger(TransactionID.class);
	
	public enum Type
	{
		/**
		 * {@link TransactionID#id} references an {@link ExternalPayment#externalReferenceId}
		 */
		REGISTRATION,
		/**
		 * {@link TransactionID#id}references an {@link ExternalPayment#externalReferenceId}
		 */
		PAYMENT,
		
		/**
		 * {@link TransactionID#id} references an {@link ExternalPaymentRegistration#externalReferenceId}
		 */
		CHANGE_REGISTRATION
	}
	
	private final Type type;
	
	private final String id;
	
	private final String fromHost;
	
	private TransactionID(Type type, String id, String fromHost)
	{
		Objects.requireNonNull(type, "type == null");
		
		this.type = type;
		this.id = id;
		this.fromHost = fromHost;
	}
	
	/**
	 * The id of either an external payment {@link ExternalPayment} or
	 * a subscription {@link Subscription}.
	 *
	 * @return the id of the triggering entity
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * The type of this transactions associated trigger.
	 *
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * Returns the host id from which this transaction id was send.
	 *
	 * @return the host id
	 */
	public String getFromHostId()
	{
		return fromHost;
	}
	
	/**
	 * Returns the textual representation of this id (Format:
	 * [TYPE]:[USER_ID]:[ID]:[FROM_HOST].
	 *
	 * @return the transaction id as string
	 */
	@Override
	public String toString()
	{
		List<Object> params = new ArrayList<>();
		params.add(type.name());
		params.add(id != null ? id : "");
		params.add(fromHost);
		
		return Joiner.on(":").join(params);
	}
	
	/**
	 * Checks the given string to match the transaction id (Format:
	 * [TYPE]:[USER_ID]:[ID]).
	 *
	 * @param value the non-null value
	 * @return the matching result
	 */
	public static boolean match(String value)
	{
		List<String> parsedValues = Splitter.on(":").splitToList(value);
		return parsedValues.size() == 3 && Arrays.stream(Type.values()).anyMatch(t -> t.name().equals(parsedValues.get(0)));
	}
	
	/**
	 * Parses the given transaction id (Format: [TYPE]:[USER_ID]:[ID]).
	 *
	 * @param value the non-null value
	 * @return the parsed transaction id
	 */
	public static TransactionID parse(String value)
	{
		List<String> parsedValues = Splitter.on(":").splitToList(value);
		
		Preconditions.checkArgument(parsedValues.size() == 3, "Invalid transaction ID: " + value);
		
		Type type = Type.valueOf(parsedValues.get(0));
		
		String id = Strings.emptyToNull(parsedValues.get(1));
		
		String fromHost = parsedValues.get(2);
		
		return new TransactionID(type, id, fromHost);
	}
	
	/**
	 * Returns the transaction id for the given parameter.
	 *
	 * @param externalPaymentRegistration the non-null externalPaymentRegistration
	 * @return the transaction id
	 */
	public static TransactionID toTransactionID(ExternalPaymentRegistration externalPaymentRegistration)
	{
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");
		
		String externalReferenceId = externalPaymentRegistration.getExternalReferenceId();
		String id = TransactionID.match(externalReferenceId) ? TransactionID.parse(externalReferenceId).getId() : externalReferenceId;
		
		return new TransactionID(Type.CHANGE_REGISTRATION, id, getHostId());
	}
	
	/**
	 * Returns the transaction id for the given parameter.
	 * Can be applied to create an auto renewal job after changing a payment method from prepayment.
	 *
	 * @param externalPaymentRegistration the non-null externalPaymentRegistration
	 * @param type                        the transaction type
	 * @return the transaction id
	 */
	public static TransactionID toTransactionID(ExternalPaymentRegistration externalPaymentRegistration, Type type)
	{
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");
		Objects.requireNonNull(type, "type == null");

		String externalReferenceId = externalPaymentRegistration.getExternalReferenceId();
		String id = TransactionID.match(externalReferenceId) ? TransactionID.parse(externalReferenceId).getId() : externalReferenceId;
		
		return new TransactionID(type, id, getHostId());
	}
	
	/**
	 * Returns the transaction id for the given parameter.
	 *
	 * @param externalPayment the non-null payment
	 * @param type            the transaction type
	 * @return the transaction id
	 */
	public static TransactionID toTransactionID(ExternalPayment externalPayment, Type type)
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");
		Objects.requireNonNull(type, "type == null");
		Preconditions.checkArgument(type != Type.CHANGE_REGISTRATION, "Invalid transaction id type: " + type);
		
		return new TransactionID(type, externalPayment.getExternalReferenceId(), getHostId());
	}
	
	private static String getHostId()
	{
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			LOG.error("Unknown host error", e);
			return "UnknownHost";
		}
	}
	
	public boolean isFromLocalHost()
	{
		return getHostId().equals(getFromHostId());
	}
}
