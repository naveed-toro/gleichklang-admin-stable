package de.binaerebauten.gleichklang.memberweb.presenter;

import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.RelationshipService;
import de.binaerebauten.gleichklang.core.service.UserDataService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.memberweb.presenter.handler.RelationshipHandler;
import de.binaerebauten.gleichklang.memberweb.view.LastRelationshipView;
import de.binaerebauten.gleichklang.memberweb.view.LastRelationshipView.LastRelationshipViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RelationshipTable.RelationshipTableHandler;
import de.binaerebauten.gleichklang.memberweb.view.popup.RelationshipPopup;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

public class LastRelationshipPresenter extends NavigatePresenter implements LastRelationshipViewListener, RelationshipTableHandler
{
	private final LastRelationshipView view;
	
	private final RelationshipService relationshipService;
	private final UserDataService userDataService;
	
	private final RelationshipHandler relationshipHandler;
	private final User currentUser;
	
	private final ClientInformation.Device device;
	
	public LastRelationshipPresenter(ApplicationContext ctx, LastRelationshipView view, ClientInformation.Device device)
	{
		super(view);
		
		this.view = view;
		this.device = device;
		
		relationshipHandler = new RelationshipHandler(ctx, this);
		relationshipService = ctx.getBean(RelationshipService.class);
		userDataService = ctx.getBean(UserDataService.class);
		
		currentUser = ctx.getBean(UserService.class).getCurrentUser();
		
		view.setListener(this);
	}
	
	@Override
	public void enter(String category)
	{
		refreshView();
	}
	
	@Override
	public void showRelationship(Relationship relationship)
	{
		final RelationshipPopup popup = relationshipHandler.createRelationshipPopup(relationship, null, e -> updateView(relationship), device);
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
		this.view.setLastRelationshipHandler(relationshipService.createLastRelationshipHandler(currentUser), this);
	}
	
	@Override
	public void leave()
	{
		this.view.setLastRelationshipHandler(null, null);
		
		super.leave();
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
		}
		
		return result.length() > 2 ? result.substring(0, result.length() - 2) : "";
	}

	@Override
	public Boolean checkUserAccess(User user){

		if(user.isBlocked() || user.getOrderedCategories().size()==0 || user.isCanceled()|| user.isDataDeleted()|| user.getMemberStatus().equals(MemberStatus.DELETED) || user.getMemberStatus().equals(MemberStatus.CANCELED)|| user.getMemberStatus().equals(MemberStatus.ADMIN_CANCELED)|| user.getMemberStatus().equals(MemberStatus.ADMIN_DELETED)){
			return true;
		}else return false;
	}
}
