package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.utils.XmlLocalDateTimeAdapter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * This enitity represents an abstract product.
 */
@XmlTransient
@XmlSeeAlso({Chargeback.class, ServiceOffer.class, InitialSubscriptionOffer.class, RenewalOffer.class, UpgradeOffer.class})
@Table(name = "product")
@Entity
public abstract class Product extends LocalizedEntity
{
	/**
	 * Enumerates all available concrete product types.
	 */
	public enum ProductType implements DefaultEnumI18N
	{
		RENEWAL_OFFER(RenewalOffer.class),
		INITIAL_SUBSCRIPTION_OFFER(InitialSubscriptionOffer.class),
		UPGRADE_OFFER(UpgradeOffer.class),
		
		CHARGEBACK(Chargeback.class),
		SERVICE_OFFER(ServiceOffer.class);
		
		private final Class<? extends Product> productClass;
		
		ProductType(Class<? extends Product> productClass)
		{
			this.productClass = productClass;
		}
		
		public Class<? extends Product> getProductClass()
		{
			return productClass;
		}
		
		public static ProductType of(Product product)
		{
			Objects.requireNonNull(product, "o == null");
			
			return Arrays.stream(ProductType.values())
					.filter(t -> t.getProductClass() == product.getClass())
					.findFirst().get();
		}
		
		public static ProductType of(Class<? extends Product> productClass)
		{
			Objects.requireNonNull(productClass, "o == null");
			
			return Arrays.stream(ProductType.values())
					.filter(t -> t.getProductClass() == productClass)
					.findFirst().get();
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	public static final String TYPE = "dtype";
	public static final String DESCRIPTION = "description";
	
	/**
	 * This field is needed for filtering.
	 */
	@Transient
	private Class dtype;

	@XmlAttribute
	@NotEmpty
	@Column
	private String name;

	@XmlAttribute
	@Column(name="DTYPE", insertable = false, updatable = false)
	private String dtypeValue;

	@XmlElement
	@Valid
	@Embedded
	private MonetaryAmount amount;

	/**
	 * The date on which this product begins to exist.
	 */
	@NotNull
	@XmlAttribute
	@XmlJavaTypeAdapter(XmlLocalDateTimeAdapter.class)
	private LocalDateTime begin;

	/**
	 * The date on which this product ends to exist. May be null.
	 */
	@XmlAttribute
	@XmlJavaTypeAdapter(XmlLocalDateTimeAdapter.class)
	private LocalDateTime end;

	/**
	 * This is a trick to enable polymorphic queries with the jpa criteria api.
	 * <p/>
	 * Currently only {@link InitialSubscriptionOffer} provides {@link InitialSubscriptionOffer#getActionCode()}
	 * and {@link InitialSubscriptionOffer#setActionCode(String)} for this field.
	 */
	@XmlAttribute
	@Column(name = "action_code")
	protected String actionCode;

	/**
	 * The product with the action code will be suggested additionally with the
	 * standard products.
	 */
	@XmlAttribute
	private boolean additional;
	
	public Class getDtype()
	{
		return getClass();
	}
	
	public String getName()
	{
		return name;
	}

	public String getDtypeValue() {
		return dtypeValue;
	}

	public void setDtypeValue(String dtypeValue) {
		this.dtypeValue = dtypeValue;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public MonetaryAmount getAmount()
	{
		return amount;
	}

	public void setAmount(MonetaryAmount amount)
	{
		this.amount = amount;
	}

	public LocalDateTime getBegin()
	{
		return begin;
	}

	public void setBegin(LocalDateTime begin)
	{
		this.begin = begin;
	}

	public LocalDateTime getEnd()
	{
		return end;
	}

	public void setEnd(LocalDateTime end)
	{
		this.end = end;
	}
	
	public String getActionCode()
	{
		return actionCode;
	}
	
	public void setActionCode(String actionCode)
	{
		this.actionCode = actionCode;
	}
	
	public boolean isAdditional()
	{
		return additional;
	}

	public void setAdditional(boolean additional)
	{
		this.additional = additional;
	}

	@Override
	protected I18NEntity.BaseName doGetBaseName()
	{
		return I18NEntity.BaseName.PRODUCT_DESCRIPTION;
	}

	/**
	 * Returns the localized description of this product.
	 *
	 * @return the localized description of this product
	 */
	public String getDescription()
	{
		return msg();
	}
	
	public abstract ProductType getProductType();

	public abstract <T> T accept(ProductVisitor<T> visitor);
}
