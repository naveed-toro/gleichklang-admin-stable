package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer.SimpleReplacer;
import de.binaerebauten.gleichklang.core.view.component.UserProfile;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileData;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileListener;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;

public class ProfileViewImpl extends AbstractNavigateView<ProfileView.MemberProfileViewListener> implements ProfileView
{
	private final SimpleReplacer profileTab = new SimpleReplacer();
	private final VerticalLayout wrapper;
	private final Device device;
	private UserProfile userProfile;

	public ProfileViewImpl(Device device)
	{
		wrapper = new VerticalLayout();
		wrapper.setStyleName(CssStyle.PROFILE_VIEW_WRAPPER.getStyleName());
		wrapper.setSizeFull();
		this.device = device;
		this.userProfile = null;

		setCompositionRoot(wrapper);

		initView(device);
	}

	private Component createTabSheet()
	{
		final TabSheet tabSheet = new TabSheet();

		tabSheet.addTab(profileTab, "");
		tabSheet.addStyleName(CssStyle.PROFILE_VIEW_CONTENT_WRAPPER.getStyleName());

		return tabSheet;
	}

	private Component createViewHeader()
	{
		final GenericViewHeader header = new GenericViewHeader();
		header.setCaption(I18N.PROFILEVIEW_TABSHEET.msg());
		header.setDescription(I18N.PROFILEVIEW_TABSHEET_DESCRIPTION.msg());
		header.setIcon(new ThemeResource("img/icon_questionnaire_aboutme-outline.svg"));
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());

		return header;
	}

	@Override
	public void initView(Device device)
	{
		wrapper.addComponents(createViewHeader(), createTabSheet());
	}

	@Override
	public void onDeviceChanged(Device device)
	{
		userProfile.refreshLayout(device);
	}

	@Override
	public void setUserData(UserProfileData userProfileData, UserProfileListener listener)
	{
		if(userProfileData == null)
		{
			profileTab.setComponent(null);
			return;
		}

		userProfile = new UserProfile(userProfileData, listener, device);
		userProfile.setEditAvatarListener((user) -> fireEvent(eventAction -> eventAction.editAvatar(user)));
		userProfile.setEditFreeTextListener((user) -> fireEvent(eventAction -> eventAction.editFreeText(user)));
		userProfile.setEditStatusListener((user) -> fireEvent(eventAction -> eventAction.editStatus(user)));

		profileTab.setComponent(userProfile);
	}
}