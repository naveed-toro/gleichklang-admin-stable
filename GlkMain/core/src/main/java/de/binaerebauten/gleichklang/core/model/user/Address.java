package de.binaerebauten.gleichklang.core.model.user;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.security.SanitizeContent;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "address")
public class Address extends BaseEntity
{
	@SanitizeContent
	@Column
	private String city;

	@SanitizeContent
	@Column
	private String streetWithNumber;

	@Column(name="tmp_zip")
	private String tmpZip;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "continent_id", nullable = false)
	private Continent continent;

	@ManyToOne
	@JoinColumn(name = "country_id", nullable = false)
	private Country country;

	@ManyToOne
	@JoinColumn(name = "region_id")
	private Region region;

	@ManyToOne
	@JoinColumn(name = "zip_id", nullable = false)
	private Zip zip;

	@Column
	private Boolean payment;

	/**
	 * This flag is set to null if this address had to be changed during the migration.
	 * It signals to the user that a relocation happened and that he has to check his "new"
	 * address. Then this flag will be set to true.
	 */
	@Column
	private Boolean checked;

	public Country getCountry()
	{
		return country;
	}
	
	public void setCountry(Country country)
	{
		this.country = country;
	}
	
	public String getCity()
	{
		return city;
	}
	
	public void setCity(String city)
	{
		this.city = city;
	}
	
	public String getStreetWithNumber()
	{
		return streetWithNumber;
	}
	
	public void setStreetWithNumber(String streetWithNumber)
	{
		this.streetWithNumber = streetWithNumber;
	}
	
	public Zip getZip()
	{
		return zip;
	}
	
	public void setZip(Zip zip)
	{
		this.zip = zip;
	}

	public Continent getContinent()
	{
		return continent;
	}

	public String getTmpZip()
	{
		return tmpZip;
	}

	public void setTmpZip(String tmpZip)
	{
		this.tmpZip = tmpZip;
	}

	public void setContinent(
			Continent continent)
	{
		this.continent = continent;
	}

	public Region getRegion()
	{
		return region;
	}

	public void setRegion(
			Region region)
	{
		this.region = region;
	}

	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}

	public Boolean isPayment()
	{
		return payment != null && payment;
	}
	
	/**
	 * Sets the payment flag for this address
	 * @param payment true or null
	 * @throws IllegalArgumentException when trying to set this field to false.
	 */
	public void setPayment(Boolean payment)
	{
		Preconditions.checkArgument(Boolean.TRUE.equals(payment) || payment == null);
		this.payment = payment;
	}


	public Boolean isChecked()
	{
		return checked != null && checked;
	}

	public void setChecked(Boolean checked)
	{
		this.checked = checked;
	}
}
