package de.binaerebauten.gleichklang.memberweb.presenter;

import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.service.*;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.memberweb.presenter.handler.RelationshipHandler;
import de.binaerebauten.gleichklang.memberweb.view.RelationshipView;
import de.binaerebauten.gleichklang.memberweb.view.RelationshipView.RelationshipViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RelationshipTable.RelationshipTableHandler;
import de.binaerebauten.gleichklang.memberweb.view.popup.RelationshipPopup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.*;
import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

public class RelationshipPresenter extends NavigatePresenter implements RelationshipViewListener, RelationshipTableHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(RelationshipPresenter.class);

	private final RelationshipView view;

	private final SubscriptionService subscriptionService;
	private final RelationshipService relationshipService;
	private final UserDataService userDataService;
	
	private final RelationshipHandler relationshipHandler;
	private final DefaultFilterControlHandler filterControlHandler;
	private final FilterSpecificationBuilder filterSpecificationBuilder;
	private final User currentUser;
	
	private final ClientInformation.Device device;
	
	public RelationshipPresenter(ApplicationContext ctx, RelationshipView view, ClientInformation.Device device)
	{
		super(view);
		
		this.view = view;
		this.device = device;
		
		relationshipHandler = new RelationshipHandler(ctx, this);
		relationshipService = ctx.getBean(RelationshipService.class);
		userDataService = ctx.getBean(UserDataService.class);
		subscriptionService = ctx.getBean(SubscriptionService.class);
		
		filterControlHandler = new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));
		filterControlHandler.setUserFilterTypes(AGE_FILTER, ALIAS_FILTER, REGION_FILTER, SEX_CHOICE_QUESTION_FILTER, FREE_TEXT_FILTER);
		filterControlHandler.setConsiderSingleUsage(true);
		filterControlHandler.setFilterControlFeatures();
		filterControlHandler.setTemplateContext(TemplateContext.RELATIONSHIP);
		
		filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);
		
		currentUser = ctx.getBean(UserService.class).getCurrentUser();
		
		view.setListener(this);
	}
	
	@Override
	public void enter(String category)
	{
		refreshView();
		this.view.setActiveRecommendationCategories(currentUser.getOrderedCategories(), category);
		this.view.setFilterHandlerAndBuilder(filterControlHandler, filterSpecificationBuilder);
		showEmptyRelationshipsNotification();
	}
	
	private void showEmptyRelationshipsNotification()
	{
		final boolean initialUser = subscriptionService.countSubscriptions(currentUser) == 1;
		if (!initialUser) return;
		
		final boolean existsRelationships = relationshipService.getRelationshipCountForUser(currentUser, null) > 0;
		if (existsRelationships) return;
		
		final Subscription currentSubscription = subscriptionService.findCurrentSubscription(currentUser).orElse(null);
		final boolean activeSubscription = currentSubscription != null && SubscriptionState.ACTIVE.equals(currentSubscription.getState());
		if (!activeSubscription) return;
		
		final boolean endOfDisplaying = LocalDateTime.now().minusHours(48).isBefore(currentSubscription.getBegin());
		if (!endOfDisplaying) return;
		
		MessageBox.show(I18N.RELATIONSHIPRESENTER_NOTIFICATION_EMPTYRELATIONSHIPS.msg());
	}
	
	@Override
	public void showRelationship(Relationship relationship) {

		final RecommendationCategory category = view.getSelectedCategory();
		final RelationshipPopup popup = relationshipHandler.createRelationshipPopup(relationship, category, e -> updateView(relationship), device);
		tryOpenPopup(popup);
	}

	private void updateView(Relationship relationship)
	{
		if (relationship.isDeleted())
			refreshView();
		else
			view.updateItem(relationship);
	}
	
	private void refreshView()
	{
		this.view.setRelationshipHandler(relationshipService.createRelationshipHandler(currentUser), this);
	}
	
	@Override
	public void leave()
	{
		super.leave();
		this.view.setRelationshipHandler(null, null);
	}
	
	@Override
	public File getThumbnailAvatarImage(Relationship relationship)
	{
		return userDataService.getThumbnailAvatarImage(relationship.getTargetUser(), relationship.getMainCategory());
	}
	
	@Override
	public String getZipRegionsCountries(User user, List<Object> savedRegionSearchRequest)
	{
		StringBuilder result = new StringBuilder();

		String resultFinal = "";
		Set<String> regionSet=new HashSet<>();
		final Set<Address> addresses = new LinkedHashSet<>(user.getAddresses());
		for (Address a : addresses)
		{
			nullSafe(() -> a.getZip().getZip())
					.filter(zip -> zip.length() > 2)
					.ifPresent(zip -> result
							.append("PLZ ")
							.append(zip.substring(0, 2))
							.append(("..., ")));

			nullSafe(() -> a.getRegion().getName())
					.ifPresent(region -> result
							.append(region)
							.append(", "));
			nullSafe(() -> a.getCountry().getName())
					.ifPresent(country -> result
							.append(country)
							.append("; "));

			///////Logic for matcing saved region search for source use with sugested target user addres region//////

			int region_name_index=0;
			int region_category_key_index=1;
			StringBuilder region_full=new StringBuilder("no_region");

			for(int i=0;i<savedRegionSearchRequest.size();i++)
			{
				String region_name=(String)((Object[]) savedRegionSearchRequest.get(i))[region_name_index];
				String region_category_key=(String)((Object[]) savedRegionSearchRequest.get(i))[region_category_key_index];

				if((a.getRegion()!=null) && (region_name != null && region_name.toLowerCase().equals(a.getRegion().getName().toLowerCase())))
					region_full.append(region_name+", "+a.getCountry().getName()+"; ");

				else if(region_name != null && region_name.equals("") && a.getCountry()!=null)
					region_full.append(a.getCountry().getName()+"; ");


				String region_actual=region_full.toString().substring(region_full.toString().indexOf("no_region")+9,region_full.toString().length());


				if(!region_actual.toString().equals("") && result.toString().toLowerCase().contains(region_actual.toLowerCase()))
				{


					if(result.toString().contains("PLZ"))
					{
						region_actual=a.getZip()!=null ?"PLZ "+a.getZip().getZip().substring(0, 2)+"..., "+region_actual:region_actual;
					}
					regionSet.add(region_actual.toString());
				}
			}

			/////////////
		}

		resultFinal=(result.length() > 2 ? result.substring(0, result.length() - 2) : "").toString();

		if(!resultFinal.equals(""))
		{
			if(regionSet.size()>0)
			{
				return  convertToList(regionSet).get(0)+(resultFinal=(result.length() > 2 ? result.substring(0, result.length() - 2) : "").toString())+";savedRegionSearch";
			}
		}
		return resultFinal;
	}


	// Generic function to convert set to list
	public List<String> convertToList(Set<String> set)
	{
		return new ArrayList<String>(set);
	}

	@Override
	public Boolean checkUserAccess(User user){

		if(user.getOrderedCategories().size() ==0 || user.isCanceled()|| user.isDataDeleted()|| user.getMemberStatus().equals(MemberStatus.DELETED) || user.getMemberStatus().equals(MemberStatus.CANCELED) || user.getMemberStatus().equals(MemberStatus.ADMIN_CANCELED) || user.getMemberStatus().equals(MemberStatus.ADMIN_DELETED)){
			return true;
		}
		else return false;
	}
}
