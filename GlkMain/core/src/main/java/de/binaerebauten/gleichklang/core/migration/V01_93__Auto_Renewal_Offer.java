package de.binaerebauten.gleichklang.core.migration;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Migrates the auto renewal offer of all products.
 */
public class V01_93__Auto_Renewal_Offer implements SpringJdbcMigration
{
	private static final Logger LOG = LoggerFactory.getLogger(V01_93__Auto_Renewal_Offer.class);

	public static class Offer
	{
		private String name;

		private String no;

		private String offer_renewalcode_source;

		private String offer_renewalcode_destination;

		private String assumed_searchdomains;

		private String offer_searchdomains;

		private Timestamp begin;

		private Timestamp end;

		private boolean is_renewal_offer;

		private String tariff;

		private Range<Timestamp> validRange;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getNo()
		{
			return no;
		}

		public void setNo(String no)
		{
			this.no = no;
		}

		public String getOffer_renewalcode_source()
		{
			return offer_renewalcode_source;
		}

		public void setOffer_renewalcode_source(String offer_renewalcode_source)
		{
			this.offer_renewalcode_source = offer_renewalcode_source;
		}

		public String getOffer_renewalcode_destination()
		{
			return offer_renewalcode_destination;
		}

		public void setOffer_renewalcode_destination(String offer_renewalcode_destination)
		{
			this.offer_renewalcode_destination = offer_renewalcode_destination;
		}

		public String getAssumed_searchdomains()
		{
			return assumed_searchdomains;
		}

		public void setAssumed_searchdomains(String assumed_searchdomains)
		{
			this.assumed_searchdomains = assumed_searchdomains;
		}

		public String getOffer_searchdomains()
		{
			return offer_searchdomains;
		}

		public void setOffer_searchdomains(String offer_searchdomains)
		{
			this.offer_searchdomains = offer_searchdomains;
		}

		public Timestamp getBegin()
		{
			return begin;
		}

		public void setBegin(Timestamp begin)
		{
			this.begin = begin;
		}

		public Timestamp getEnd()
		{
			return end;
		}

		public void setEnd(Timestamp end)
		{
			this.end = end;
		}

		public boolean is_renewal_offer()
		{
			return is_renewal_offer;
		}

		public void setIs_renewal_offer(boolean is_renewal_offer)
		{
			this.is_renewal_offer = is_renewal_offer;
		}

		public String getTariff()
		{
			return tariff;
		}

		public void setTariff(String tariff)
		{
			this.tariff = tariff;
		}

		public Range<Timestamp> getValidRange()
		{
			if (validRange == null)
			{
				if (begin == null && end == null)
				{
					validRange = Range.all();
				}
				else if (begin == null)
				{
					validRange = Range.atMost(end);
				}
				else if (end == null)
				{
					validRange = Range.atLeast(begin);
				}
				else
				{
					validRange = Range.closed(begin, end);
				}
			}
			return validRange;
		}
	}

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		fixBeginAfterEndDate(jdbcTemplate);

		resetAllRenewalOffers(jdbcTemplate);

		List<Offer> allOffers = getAllOffers(jdbcTemplate);

		Multimap<String, Offer> renewalOfferBySource = getRenewalOfferBySource(allOffers);

		for (Offer offer : allOffers)
		{
			updateRenewalOffer(jdbcTemplate, renewalOfferBySource, offer);
		}
	}

	private void fixBeginAfterEndDate(JdbcTemplate jdbcTemplate)
	{
		jdbcTemplate.update("UPDATE product SET end = begin WHERE begin > end");
	}

	private void resetAllRenewalOffers(JdbcTemplate jdbcTemplate)
	{
		jdbcTemplate.update("UPDATE product dp SET dp.auto_renewal_offer_id = NULL");
	}

	private void updateRenewalOffer(JdbcTemplate jdbcTemplate, Multimap<String, Offer> renewalOfferBySource, Offer offer)
	{
		Collection<Offer> offersWithDestinationSource = renewalOfferBySource.get(offer.getOffer_renewalcode_destination());

		if (offersWithDestinationSource != null)
		{
			List<Offer> matchingRenewalOffers = offersWithDestinationSource
					.stream()
					.filter(s -> isValidSourceOffer(s, offer))
					.collect(Collectors.toList());

			if (matchingRenewalOffers.size() > 1)
			{
				LOG.warn("Found multiple renewal offers!");
			}

			if (matchingRenewalOffers.size() > 0)
			{
				Offer sourceOffer = matchingRenewalOffers.get(0);
				jdbcTemplate.update(
						"UPDATE product dp, product sp"
								+ " SET dp.auto_renewal_offer_id = sp.id"
								+ " WHERE sp.legacy_id = ? AND dp.legacy_id = ?",
						sourceOffer.no, offer.no);
			}
		}
	}

	private Multimap<String, Offer> getRenewalOfferBySource(List<Offer> allOffers)
	{
		List<Offer> renewalOffers = allOffers
				.stream()
				.filter(this::isRenewalOffer)
				.collect(Collectors.toList());

		return Multimaps.index(renewalOffers, Offer::getOffer_renewalcode_source);
	}

	private boolean isRenewalOffer(Offer offer)
	{
		return offer.is_renewal_offer && offer.offer_renewalcode_source != null;
	}

	private boolean isValidSourceOffer(Offer source, Offer destination)
	{
		boolean searchdomains = source.getAssumed_searchdomains().equals(destination.getOffer_searchdomains());

		Range<Timestamp> sourceRange = source.getValidRange();
		Range<Timestamp> destinationRange = destination.getValidRange();

		boolean intersect = sourceRange.isConnected(destinationRange)
				&& !sourceRange.intersection(destinationRange).isEmpty();

		boolean tariff = Objects.equals(source.getTariff(), destination.getTariff());

		return searchdomains && intersect && tariff;
	}

	private List<Offer> getAllOffers(JdbcTemplate jdbcTemplate)
	{
		return jdbcTemplate.query(
				"SELECT"
						+ " p.name, o.no, o.offer_renewalcode_source, o.offer_renewalcode_destination,"
						+ " o.assumed_searchdomains, o.offer_searchdomains,"
						+ " p.begin, p.end,"
						+ " o.is_renewal_offer,"
						+ " p.tariff"
						+ " FROM comppayment_offer o JOIN product p ON p.legacy_id = o.no",
				new BeanPropertyRowMapper<>(Offer.class));
	}
}
