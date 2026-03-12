package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * This class represents a heidelpay transaction.
 */
@Entity
@Table(name = "heidelpay_transaction")
public class HeidelpayTransaction extends BaseEntity
{
	@NotNull
	@ManyToOne // default to load payment eager is fine here!
	@JoinColumn(name = "payment_id")
	private ExternalPayment payment;

	@NotNull
	@Column(name = "return_code")
	private String returnCode;

	@NotNull
	@Column(name = "return_message")
	private String returnMessage;

	@NotNull
	@Column(name = "result")
	@Enumerated(EnumType.STRING)
	private ProcessingResultType result;

	public ExternalPayment getPayment()
	{
		return payment;
	}

	public void setPayment(ExternalPayment payment)
	{
		this.payment = payment;
	}

	public ProcessingResultType getResult()
	{
		return result;
	}

	public void setResult(ProcessingResultType result)
	{
		this.result = result;
	}

	public String getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(String returnCode)
	{
		this.returnCode = returnCode;
	}

	public String getReturnMessage()
	{
		return returnMessage;
	}

	public void setReturnMessage(String returnMessage)
	{
		this.returnMessage = returnMessage;
	}
}
