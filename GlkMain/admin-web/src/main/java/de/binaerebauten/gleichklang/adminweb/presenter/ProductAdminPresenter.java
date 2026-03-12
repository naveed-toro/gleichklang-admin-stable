package de.binaerebauten.gleichklang.adminweb.presenter;

import com.google.common.collect.Iterables;
import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.adminweb.service.payment.ProductAdminService;
import de.binaerebauten.gleichklang.adminweb.view.ProductAdminView;
import de.binaerebauten.gleichklang.adminweb.view.popup.ProductPopup;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.filter.ProductValidFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Presenter for the administration of products {@link Product}.
 * Uses {@link ProductAdminView} for viewing a single product.
 */
public class ProductAdminPresenter extends NavigatePresenter implements ProductAdminView.ProductAdminViewListener
{
	private static class ProductCreator implements ProductVisitor<Product>
	{
		/**
		 * Creates new subscription offers for all given product types.
		 */
		public List<Product> createInstances(ProductType... productTypes)
		{
			return Arrays.stream(productTypes)
					.map(this::newInstance).map(o -> o.accept(this))
					.collect(Collectors.toList());
		}
		
		private Product newInstance(ProductType productType)
		{
			try
			{
				Product product = productType.getProductClass().newInstance();
				
				product.setBegin(LocalDate.now().atStartOfDay());
				product.setAmount(new MonetaryAmount(BigDecimal.ZERO, AvailableCurrency.EUR));
				
				return product;
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				throw new IllegalArgumentException("", e);
			}
		}
		
		private void init(SubscriptionOffer subscriptionOffer)
		{
			subscriptionOffer.setDuration(subscriptionOffer.getDefaultDuration());
			subscriptionOffer.setDurationUnit(DurationUnit.MONTHS);
			subscriptionOffer.setTariff(Tariff.STANDARD);
		}
		
		@Override
		public Product visit(InitialSubscriptionOffer initialSubscriptionOffer)
		{
			init(initialSubscriptionOffer);
			
			return initialSubscriptionOffer;
		}
		
		@Override
		public Product visit(RenewalOffer renewalOffer)
		{
			init(renewalOffer);
			
			return renewalOffer;
		}
		
		@Override
		public Product visit(UpgradeOffer upgradeOffer)
		{
			init(upgradeOffer);
			upgradeOffer.setUpgradeType(UpgradeType.TARIFF_CHANGE);
			
			return upgradeOffer;
		}
		
		@Override
		public Product visit(ServiceOffer serviceOffer)
		{
			return serviceOffer;
		}
		
		@Override
		public Product visit(Chargeback chargeback)
		{
			return chargeback;
		}
	}
	
	private final Sort sortOrder = new PageRequest(0, Integer.MAX_VALUE, Sort.Direction.ASC, "name").getSort();
	private final ProductRepository<Product> productRepository;
	private final ProductRepository<SubscriptionOffer> subscriptionOfferRepository;
	private final ProductRepository<RenewalOffer> renewalOfferSubscriptionOfferRepository;
	private final I18NRepository i18NRepository;
	private final ProductAdminService productAdminService;
	private final ProductAdminView view;
	
	public ProductAdminPresenter(ApplicationContext ctx, ProductAdminView productAdminView)
	{
		super(productAdminView);
		
		productRepository = ctx.getBean(ProductRepository.class);
		subscriptionOfferRepository = ctx.getBean(ProductRepository.class);
		renewalOfferSubscriptionOfferRepository = ctx.getBean(ProductRepository.class);
		i18NRepository = ctx.getBean(I18NRepository.class);
		
		productAdminService = ctx.getBean(ProductAdminService.class);
		
		view = productAdminView;
		
		view.setListener(this);
	}
	
	@Override
	public void newProduct()
	{
		List<SubscriptionOffer> allOffers = subscriptionOfferRepository.findAll(
				new ProductValidFilter<>(ProductType.INITIAL_SUBSCRIPTION_OFFER,
						ProductType.RENEWAL_OFFER, ProductType.UPGRADE_OFFER), sortOrder);
		List<RenewalOffer> renewalOffers = renewalOfferSubscriptionOfferRepository.findAll(
				new ProductValidFilter<>(ProductType.RENEWAL_OFFER), sortOrder);
		List<Product> newProducts = new ProductCreator().createInstances(ProductType.values());
		
		ProductPopup productPopup = new ProductPopup(newProducts, allOffers,
				renewalOffers, createSubscriptionOfferCategories(null, RecommendationCategory.values()),
				createServiceOfferRequiredCategories(null, RecommendationCategory.values()),
				null, productAdminService::save);
		productPopup.setActiveEditor(ProductType.INITIAL_SUBSCRIPTION_OFFER);
		productPopup.addCloseListener(e -> refreshView());
		tryOpenPopup(productPopup);
	}
	
	@Override
	public void editProduct(Product product)
	{
		product = productAdminService.fetchProduct(product);
		
		List<SubscriptionOffer> allOffers = subscriptionOfferRepository.findAll(
				new ProductValidFilter<>(ProductType.INITIAL_SUBSCRIPTION_OFFER,
						ProductType.RENEWAL_OFFER, ProductType.UPGRADE_OFFER), sortOrder);
		List<RenewalOffer> renewalOffers = renewalOfferSubscriptionOfferRepository.findAll(
				new ProductValidFilter<>(ProductType.RENEWAL_OFFER), sortOrder);
		List<I18NEntity> i18nDescriptions = i18NRepository.findByBaseNameAndKey(
				I18NEntity.BaseName.PRODUCT_DESCRIPTION, product.getI18nKey());
		
		ProductPopup productPopup = new ProductPopup(product, allOffers,
				renewalOffers, createAllSubscriptionOfferCategories(product),
				createAllServiceOfferRequiredCategories(product),
				i18nDescriptions, this::save);
		productPopup.addCloseListener(e -> refreshView());
		tryOpenPopup(productPopup);
	}
	
	private void save(Product product, Collection<I18NEntity> i18nDescriptions)
	{
		try
		{
			productAdminService.save(product, i18nDescriptions);
		}
		catch (ValidationException e)
		{
			Notification.show(e.getLocalizedMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void enter(String parameters)
	{
		refreshView();
	}
	
	private void refreshView()
	{
		this.view.setProductHandler(productRepository::findAll);
	}
	
	/**
	 * Creates a list of {@link SubscriptionOfferCategory} for the given
	 * subscription offer and categories.
	 *
	 * @param subscriptionOffer the subscription offer for which the categories hould be built
	 *                          {@link SubscriptionOfferCategory#subscriptionOffer}
	 * @param categories        create a {@link SubscriptionOfferCategory} for each category in the given categories
	 * @return the sorted list of subscription offer categories
	 */
	private List<SubscriptionOfferCategory> createSubscriptionOfferCategories(SubscriptionOffer subscriptionOffer, RecommendationCategory... categories)
	{
		List<SubscriptionOfferCategory> subscriptionOfferCategories = Arrays.stream(categories)
				.map(c -> new SubscriptionOfferCategory(subscriptionOffer, c))
				.collect(Collectors.toList());
		Collections.sort(subscriptionOfferCategories);
		
		return subscriptionOfferCategories;
	}
	
	/**
	 * Creates a list of {@link ServiceOfferRequiredCategory} for the given
	 * service offer and categories.
	 *
	 * @param serviceOffer the service offer for which the categories should be built
	 *                     {@link ServiceOfferRequiredCategory#serviceOffer}
	 * @param categories   create a {@link ServiceOfferRequiredCategory} for each category in the given categories
	 * @return the sorted list of service offer required categories
	 */
	private List<ServiceOfferRequiredCategory> createServiceOfferRequiredCategories(ServiceOffer serviceOffer, RecommendationCategory... categories)
	{
		List<ServiceOfferRequiredCategory> subscriptionOfferCategories = Arrays.stream(categories)
				.map(c -> new ServiceOfferRequiredCategory(serviceOffer, c))
				.collect(Collectors.toList());
		Collections.sort(subscriptionOfferCategories);
		
		return subscriptionOfferCategories;
	}
	
	/**
	 * Creates a list which contains all {@link SubscriptionOffer#categories} and adds
	 * new {@link SubscriptionOfferCategory} for any missing {@link RecommendationCategory}.
	 *
	 * @param product the product
	 * @return the sorted list of subscription offer categories
	 */
	private List<SubscriptionOfferCategory> createAllSubscriptionOfferCategories(Product product)
	{
		List<SubscriptionOfferCategory> availableSubscriptionOfferCategories = new ArrayList<>();
		if (product instanceof SubscriptionOffer)
		{
			SubscriptionOffer subscriptionOffer = (SubscriptionOffer) product;
			
			Set<SubscriptionOfferCategory> assignedSubscriptionOfferCategories = subscriptionOffer.getCategories();
			Set<RecommendationCategory> assignedCategories = assignedSubscriptionOfferCategories.stream()
					.map(SubscriptionOfferCategory::getCategory)
					.collect(Collectors.toSet());
			
			EnumSet<RecommendationCategory> unassignedCategories = EnumSet.allOf(RecommendationCategory.class);
			unassignedCategories.removeAll(assignedCategories);
			
			List<SubscriptionOfferCategory> unassignedSubscriptionOfferCategories =
					createSubscriptionOfferCategories(subscriptionOffer, Iterables.toArray(unassignedCategories, RecommendationCategory.class));
			
			availableSubscriptionOfferCategories.addAll(assignedSubscriptionOfferCategories);
			availableSubscriptionOfferCategories.addAll(unassignedSubscriptionOfferCategories);
			
			Collections.sort(availableSubscriptionOfferCategories);
		}
		
		return availableSubscriptionOfferCategories;
	}
	
	/**
	 * Creates a list which contains all {@link ServiceOffer#requiredCategories} and adds
	 * new {@link ServiceOfferRequiredCategory} for any missing {@link RecommendationCategory}.
	 *
	 * @param product the product
	 * @return the sorted list of service offer required categories
	 */
	private List<ServiceOfferRequiredCategory> createAllServiceOfferRequiredCategories(Product product)
	{
		List<ServiceOfferRequiredCategory> availableRequiredServiceOfferCategories = new ArrayList<>();
		if (product instanceof ServiceOffer)
		{
			ServiceOffer serviceOffer = (ServiceOffer) product;
			
			Set<ServiceOfferRequiredCategory> assignedSubscriptionOfferCategories = serviceOffer.getRequiredCategories();
			Set<RecommendationCategory> assignedCategories = assignedSubscriptionOfferCategories.stream()
					.map(ServiceOfferRequiredCategory::getCategory)
					.collect(Collectors.toSet());
			
			EnumSet<RecommendationCategory> unassignedCategories = EnumSet.allOf(RecommendationCategory.class);
			unassignedCategories.removeAll(assignedCategories);
			
			List<ServiceOfferRequiredCategory> unassignedSubscriptionOfferCategories =
					createServiceOfferRequiredCategories(serviceOffer, Iterables.toArray(unassignedCategories, RecommendationCategory.class));
			
			availableRequiredServiceOfferCategories.addAll(assignedSubscriptionOfferCategories);
			availableRequiredServiceOfferCategories.addAll(unassignedSubscriptionOfferCategories);
			
			Collections.sort(availableRequiredServiceOfferCategories);
		}
		
		return availableRequiredServiceOfferCategories;
	}
}
