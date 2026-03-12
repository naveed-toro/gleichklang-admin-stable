package de.binaerebauten.gleichklang.core.view.css;

public enum CssStyle
{

	/*admin messaege table */
	ADMIN_MESSAGE_POPUP("message-popup"),
	ADMIN_LIST_MESSAGES_WRAPPER("admin-list-messages-wrapper"),

	/*age filter component*/
	AGE_FILTER_COMPONENT("age-filter-component"),

	/*avatar popup*/
	BTN_REMOVE_AVATAR("btn-remove-avatar"),
	AVATAR_UPLOAD("avatar-upload"),
	AVATAR_POPUP("avatar-popup"),
	NEW_MEDIA_AVATAR("new-media-avatar"),
	SELECT_FROM_GALLERY_BTN("select-from-gallery-btn"),
	UPLOAD_WRAPPER("upload-wrapper"),
	SELECT_FROM_GALLERY_WRAPPER("select-from-gallery-wrapper"),
	UPLOAD_COMPONENT("upload-component"),
	NEW_AVATAR("new-avatar"),
	EXISTING_AVATAR("existing-avatar"),

	/*edit media popup */
	EDIT_MEDIA_POPUP("edit-media-popup"),
	ROTATE_MEDIA_WRAPPER("rotate-media-wrapper"),
	MEDIA_DESCRIPTION_WRAPPER("media-description-wrapper"),
	EDIT_MEDIA_BTN_WRAPPER("edit-media-btn-wrapper"),
	SET_AS_AVATAR_WRAPPER("set-as-avatar-wrapper"),
	BTN_SAVE_MEDIA("btn-save-media"),


	/* field validation */
	FIELD_VALID("valid"),

	/* file link component*/
	FILE_DOWNLOAD_WRAPPER("file-download-wrapper"),
	FILE_DOWNLOAD_BUTTON("file-download-button"),

	/*filter control component*/
	FILTER_COMBOBOX("filter-combobox"),
	ACTIVATED_FILTER("activated-filter"),
	BTN_ADD_FILTER("btn-add-filter"),
	REMOVE_ALL_FILTER_BTN("remove-all-filter-button"),
	FILTER_ROW("filter-row"),
    FILTER_BTN_OR("filter-or-button"),
	CANCEL_CHECKBOX("filter-cancel-checkbox"),
	MARGIN_LEFT("margin-left-button"),

	/*footprint popup*/
	FOOTPRINT_POPUP("footprint-popup"),
	FOOTPRINT_ITEMS_WRAPPER("footprint-items-wrapper"),

	/*generics */
	TABSHEET_DROPDOWN_POPUP_ITEMS("tabsheet-dropdown-popup-items"),
	AFFILIATE_GREEN("affiliate-green"),
	BOLD("bold"),
	BORDER_BOTTOM("border-bottom"),
	CAROUSEL_BUTTONS("carousel-buttons"),
	HORIZONTAL_CENTER("horizontal-center"),
	FLOAT_RIGHT("float-right"),
	FOCUS_BTN("focus-btn"),
	GK_PANEL("gk-panel"),
	GO_TO_TOP("go-to-top"),
	GENERIC_POPUP("generic-popup"),
	GENERIC_POPUP_HEADER("generic-popup-header"),
	GENERIC_POPUP_CONTROLBUTTON_WRAPPER("control-button-wrapper"),
	INCOMPLETE_QUESTIONNAIRE_YELLOW("incomplete-questionnaire-yellow"),
	SMALL_MARGIN_BOTTOM("small-margin-bottom"),
	MARGIN_BOTTOM("margin-bottom"),
	MARGIN_TOP("margin-top"),
	NEWS_BLUE("news-blue"),
	OFFER_GREEN("offer-green"),
	PANEL_BUTTON("panel-button"),
	PANEL_DEFAULT_TEXT("panel-default-text"),
	AUTO_RENEWAL_BUTTON("auto_renewal_button"),
	AUTO_RENEWAL_BUTTON_MOBILE("auto_renewal_button_mobile"),
	AUTO_RENEWAL_TEXT_FIELD("auto_renewal_text_field"),
	RED("red"),
	GREEN("green"),
	BLUE("blue"),
	RIPPLE_ELEMENT("ripple-element"),
	REPORT_RED("report-red"),
	RESPONSIVE_BUTTONS("responsive-buttons"),
	SECOND_OFFER_BLUE("second-offer-blue"),
	SMALL("small"),
	VIEW_HEADER("view-header"),
	WHITE("white"),
	LOGO("logo"),
	BUILD_LABEL("build-label"),

	/*home view */
	COLUMN_SPACER("column-spacer"),
	HOME_CATEGORY_FILTER_CONTROL("home-category-filter-control"),
	HOME_VIEW_OVERVIEW("home-view-overview"),
	HOME_VIEW_WRAPPER("home-view-wrapper"),
	MISSING_AVATAR_COMPONENT("missing-avatar-component"),
	MISSING_AUDIO_COMPONENT("missing-avatar-component"),
	LINK_LAYOUT("link-layout"),
	PANEL_WRAPPER("panel-wrapper"),
	PANEL_MY_PROFILE("panel-my-profile"),
	PANEL_MY_ACCOUNT("panel-my-account"),
	PANEL_HEADER("panel-header"),
	PANEL_CONTENT("panel-content"),
	TABSHEET_DROPDOWN_HOMEVIEW("tabsheet-dropdown-homeview"),
	ACCOUNT_PLACEHOLDER("account-placeholder"),
	PROFILE_PLACEHOLDER("profile-placeholder"),
	PANEL_STEP_GROW("panel-step-grow"),
	CORONOVIRUS_GROW("corono_virus-grow"),
	/*info popup*/
	ANSWER_COMPONENT("answer-component"),
	INFO_POPUP("info-popup"),
	INFO_HEADER("info-header"),
	QUESTION_CATEGORY("question-category"),
	QUESTION_ANSWER("question-answer"),


	/* login view */
	LOGIN_WRAPPER("login-wrapper"),
	LOGIN_INPUT_FIELDS("login-input-field"),
	LOGIN_FORGOT_WRAPPER("login-forgot-wrapper"),
	LOGIN_FORGOT_BUTTON_PANEL("login-forgot-button-panel"),
	LOGIN_BUTTON("login-button"),
	LOGIN_LINK_LAYOUT("login-link-layout"),

	/*incomplete questionnaire component */
	QUESTIONNAIRE_CAROUSEL("questionnaire-carousel"),
	QUESTIONNAIRE_PLACEHOLDER("questionnaire-placeholder"),
	QUESTIONNAIRE_HEADER("questionnaire-header"),

	/* main view */
	CURRENT_USER("current-user"),
	HEADER_WRAPPER("header-wrapper"),
    HEADER_WRAPPER_MOBILE("header-wrapper-mobile"),
	HEADER_NAVIGATION("navigation-bar"),
	TOP_HEADER_WRAPPER("top-header-wrapper"),
	BUILD_INFO("build-info"),
	LOGOUT("logout"),
	MAIN_VIEW("main-view"),
	MEMBER_MAIN_VIEW("member-main-view"),
	MEMBER_LOGIN_VIEW("member-login-view"),
	MEMBER_LOGIN_VIEW_MESSAGE("member-login-view-message"),
	MEMBER_PREREG_VIEW("member-prereg-view"),
	MENU_WRAPPER("menu-wrapper"),
	MENU_BAR("main-menu"),
	MENU_POPUP("popup-main-menu"),
    BUTTON_MAIN_MENU("main-menu-button"),
	ADMIN_MAIN_VIEW_VIEWPORT_WRAPPER("admin-main-viewport-wrapper"),
	MESSAGE_TO_ADMIN_BUTTON("admin-message-button"),

	/*member profile view*/
	EDIT_PROFILE_BUTTON("edit-profile-button"),
	EDIT_AVATAR_BUTTON("edit-avatar-button"),
	EDIT_STATUS_BUTTON("edit-status-button"),
	PREVIEW_HEADER("preview-header"),
	PREVIEW("preview"),

	/*media gallery component*/
	ADD_MEDIA("add-media"),
	AVATAR_LABEL("avatar-label"),
	BTN_EDIT("btn-edit"),
	BTN_EDIT_MEDIA("btn-edit-media"),
	BTN_REMOVE("btn-remove"),
	CROP("crop"),
	INVISIBLE_PLACEHOLDER("invisible-placeholder"),
	MEDIA_COMMAND("media-command"),
	EMPTY_GALLERY("empty-gallery"),
	GALLERY_NAME("gallery-name"),
	MEDIA_COMPONENT("media-component"),
	MEDIA_WRAPPER("media-wrapper"),
    MEDIA_GALLERY_WRAPPER("media-gallery-wrapper"),
	MEDIA_GALLERY_LIST("media-gallery-list"),
	MEDIA_COMPONENT_IMAGE("media-component-image"),
    MEDIA_COMPONENT_IMAGE_NAME("media-component-image-name"),
	MEDIA_GALLERY_COMPONENT("media-gallery-component"),
	MEDIA_GALLERY_COMPONENT_IMAGE("media-gallery-component-image"),
	MEDIA_GALLERY_NAME_LABEL("media-gallery-name-label"),
	BUTTON_REMOVE_GALLERY("button-remove-gallery"),
	BUTTON_ADD("button-add"),
	BUTTON_REMOVE_MEDIA("button-remove-media"),
	BUTTON_EDIT_GALLERY("button-edit-gallery"),
	IMAGE_VIEWER_IMAGE("image-viewer-image"),

	/*media gallery popup*/
	MEDIA_GALLERY_POPUP("media-gallery-popup"),
	MEDIA_NAME_LABEL("media-name-label"),

	/*media upload popup*/
	MEDIA_UPLOAD_POPUP("media-upload-popup"),

	/*media view */
	MEDIA_CAROUSEL_POPUP("media-carousel-popup"),

	/*message box */
	MESSAGE_BOX("message-box"),

	/* message dialog */
	BTN_ADD_FILE("btn-add-file"),
    COLLAPSED("collapsed"),
	EMOJI_BAR("emoji-bar"),
	EMOJI_MENU("emoji-menu"),
    EXPAND("expand"),
	FILEUPLOAD("fileupload"),
	MESSAGE_BODY("message-body"),
	MESSAGE_DIALOG_ATTACHMENT("message-dialog-attachment"),
	MESSAGE_DIALOG_FOOTER_BUTTONS("message-dialog-footer-buttons"),
	MESSAGE_DIALOG_HEADER("message-dialog-header"),
	MESSAGE_DIALOG_POPUP("message-dialog-popup"),
	MESSAGE_DIALOG_POPUP_NORMAL("message-dialog-popup-normal"),
	MESSAGE_DIALOG_WRAPPER("message-dialog-wrapper"),
	OLD_MESSAGE_WRAPPER("old-message-wrapper"),
	MESSAGE_RELATIONSHIP_CANCELED_MSG("relationship-canceled-msg"),

	/* message table */
	ALIAS_FILTER("alias-filter"),
	BUTTON_NEW("button-new"),
	DATE_LABEL("date-label"),
	DEFAULT(null),
	FIRST_MESSAGE("first-message"),
	REPLY_MESSAGES("reply-message"),
	FONTWEIGHT_CLEARED("fontweight-cleared"),
	GK_MESSAGE_RECEIVED_HEADER("gk-message-received-header"),
	GK_MESSAGE_SENT_HEADER("gk-message-sent-header"),
	LIST_MESSAGES_WRAPPER("list-messages-wrapper"),
	MESSAGE_ATTACHMENT_WRAPPER("message-attachment-wrapper"),
	MESSAGE_ATTACHMENT_ICON("message-attachment-icon"),
	MESSAGE_ANSWERED_ICON("message-answered-icon"),
	MESSAGE_CELL_WRAPPER("message-cell-wrapper"),
	MESSAGE_HEADER("message-header"),
	MESSAGE_RECEIVED_BODY("message-received-body"),
	MESSAGE_RECEIVED_HEADER("message-received-header"),
	MESSAGE_SENT_BODY("message-sent-body"),
	MESSAGE_SENT_HEADER("message-sent-header"),
	MESSAGE_SENDER_WRAPPER("message-sender-wrapper"),
	MESSAGE_CHECKBOX("message-checkbox"),
	MESSAGE_TABLE("message-table"),
	MESSAGE_STATUS("message-status"),
	MESSAGE_SUBJECT("message-subject"),
	MESSAGE_HEADER_ROW("message-header-row"),
	MESSAGE_UNREAD_BUTTON("message-unread-button"),
	MESSAGE_UNREAD_ICON("message-unread-icon"),
	READ("read"),
	TABSHEET_DROPDOWN_BLUE("tabsheet-dropdown-blue"),
	UNREAD("unread"),
	USERNAME_LABEL("username-label"),

	/* message view */
	MESSAGE_VIEW_PANEL("message-view-panel"),
	USER_MESSAGE_FOOTER("user-message-footer"),
	MESSAGE_BUTTON_NEW("message-button-new"),
	MESSAGE_CATEGORY_FILTER_CONTROL("message-category-filter-control"),

	/* new user admin messages component*/
	ADMIN_MESSAGE_ICON("admin-message-icon"),
	ADMIN_MESSAGE_WRAPPER("admin-message-wrapper"),

	/* new footprints component */
	FOOTPRINT_BUTTONS("footprint-buttons"),
	FOOTPRINTS_INNER_WRAPPER("footprints-inner-wrapper"),
	FOOTPRINT_NAME("footprint-name"),
	FOOTPRINT_USER("footprint-user"),
	FOOTPRINTS_WRAPPER("footprints-wrapper"),

	/* new messages and matches component */
	OVERVIEW_WRAPPER("overview-wrapper"),
	CURRENT_RECCAT("current-reccat"),
	ICON_NEW_SUGGESTIONS("icon-new-suggestions"),
	ICON_NEW_MESSAGES("icon-new-messages"),
	MATCH_WRAPPER("match-wrapper"),
	RECCAT_CAROUSEL_WRAPPER("reccat-carousel-wrapper"),
	RECCAT_ICON("reccat-icon"),
	RECCAT_ICON_EMPTY("reccat-icon-empty"),
	RECCAT_NAME("reccat-name"),
	RECCAT_PREVIEW_LEFT("reccat-preview-left"),
	RECCAT_PREVIEW_RIGHT("reccat-preview-right"),
	RECCAT_PREVIEW("reccat-preview"),
	RELATIONSHIP_WRAPPER("relationship-wrapper"),

	/* news component (member) */
	HEADER("header"),
	TITLE("title"),
	TEASER("teaser"),
	TEXT("text"),
	NEWS_PLACEHOLDER("news-placeholder"),
	NEWS_POPUP("news-popup"),
	NEWS_WRAPPER("news-wrapper"),
	NEWS_WRAPEPR_BUTTONS("news-wrapper-buttons"),

	/*number filter component*/
	NUMBER_FILTER_COMPONENT("number-filter-component"),

	/* product popup */
	DOUBLE_WIDTH_TWINCOLSELECT("double-width-twincolselect"),

	/*profile view*/
	PROFILE_VIEW_WRAPPER("profile-view-wrapper"),
	PROFILE_VIEW_CONTENT_WRAPPER("profile-view-content-wrapper"),

	/* preregistration view */
	PREREGISTRATION_PANEL("preregistration-panel"),
	PREREGISTRATION_INFO("preregistration-info"),
	PREREGISTRATION_VIEW("preregistration-view"),
    PREREGISTRATION_BUTTON("preregistration-button"),
	PREREGISTRATION_ADVERTISEMENT("preregistration-advertisement"),
    PREREIGSTRATION_CONTENT("preregistration-content"),
	PREREGISTRATION_RECOMMENDATION("preregistration-recommendation"),
	PREREGISTRATION_CHECKBOX_LEFTALIGN("preregistration-checkbox-left"),
	
	/* password check */
	PASSWORD_CHECK("password-check"),
	PASSWORD_CHECK_EMPTY("none"),
	PASSWORD_CHECK_LABEL("label"),
	PASSWORD_CHECK_VERY_WEAK("very-weak"),
	PASSWORD_CHECK_WEAK("weak"),
	PASSWORD_CHECK_STRONG("strong"),
	PASSWORD_CHECK_VERY_STRONG("very-strong"),

	/* postregistration view */
	REGISTRATION_CONTENT_VIEW("postregistration-content-view"),
    REGISTRATION_MAIN_VIEW("postregistration-main-view"),
	REGISTRATION_MESSAGE_TO_ADMIN_LAYOUT("postregistration-message-to-admin-layout"),

	/* recommendationbreak view*/
	RECOMMENDATIONBREAK_COMPONENT("recommendationbreak-component"),
	RECOMMENDATION_VIEW("recommendation-view"),

	/*region filter component*/
	REGION_FILTER_COMPONENT("region-filter-component"),

	/* relationship popup */
	RELATIONSHIPPOPUP_ALIAS("alias"),
	RELATIONSHIPPOPUP_HEADER("relationship-popup-header"),
    RELATIONSHIPPOPUP_MEMO_WRAPPER("relationship-popup-memo-wrapper"),
	RELATIONSHIPPOPUP_MEMO_LAYOUT("relationship-popup-memo-layout"),
    RELATIONSHIPPOPUP_STATUS_WRAPPER("relationship-status-wrapper"),
	AVATAR_WRAPPER("avatar-wrapper"),
	CHANGE_FOOTPRINT_BUTTON("change-footprint-button"),
	DELETE_RELATIONSHIP_BUTTON("delete-relationship-button"),
	FIRST_COLUMN("first-column"),
	FOOTPRINT_COMBOBOX("footprint-combobox"),
	FOOTPRINT_COMBOBOX_POPUP_ITEMS("footprint-combobox-popup-items"),
	FOOTPRINT_LABEL("footprint-label"),
	FOOTPRINT_TO_USER("footprint-to-user"),
	FOOTPRINT_FROM_USER("footprint-from-user"),
	FOOTPRINT_MOBILE("footprint-mobile"),
	FOOTPRINT_SENDER("footprint-sender"),
	FOOTPRINT_WRAPPER("footprint-wrapper"),
	HIDE_FOOTER("hide-footer"),
	MEDIA_GALLERY("media-gallery"),
	MEMO_TEXTFIELD("memo-textfield"),
	MESSAGE_TABLE_WRAPPER("message_table_wrapper"),
	MOBILE_MIDDLE_HEADER("mobile-middle-header"),
	MOBILE_BOTTOM_HEADER("mobile-bottom-header"),
	PROFILE_FOOTER("profile-footer"),
	PROFILE_HEADER("profile-header"),
	RELATION_ICON_WRAPPER("relation-icon-wrapper"),
	RELATIONSHIP_VIEW_RELATIONSHIP_POPUP("relationship-view-relationship-popup"),
	RELATIONSHIP_POPUP_CATEGORY_FILTER_CONTROL("relationship-popup-category-filter-control"),
	SECOND_COLUMN("second-column"),
	SNIPPET_RIGHT("snippet-right"),
	SNIPPET_LEFT("snippet-left"),
	USER_INFO_BUTTONS("user-info-buttons"),
	USER_INFO_COMBOBOX("user-info-combobox"),
	USER_PROFILE("user-profile"),
	USER_PROFILE_FREETEXT("user-profile-freetext"),
	USER_PROFILE_FREETEXT_WRAPPER("user-profile-freetext-wrapper"),
	USER_PROFILE_IMAGE_WRAPPER("user-profile-image-wrapper"),
    USER_PROFILE_IMAGE("user-profile-image"),
	DESKTOP_USER_RATING_WRAPPER("desktop-user-rating-wrapper"),
	MOBILE_USER_RATING_WRAPPER("mobile-user-rating-wrapper"),

	/*relationship view*/
	CHECKBOX_SPACER("checkbox-spacer"),
	RELATIONSHIP_CATEGORY_FILTER_CONTROL("relationship-category-filter-control"),
	FILTER_LABEL("filter-label"),
	FREE_TEXT_BODY("free-text-body"),
	FREE_TEXT_HEADER("free-text-header"),
	NEW_MESSAGE_BUTTON("new-message-button"),
	RELATIONSHIP_AVATAR("relationship-avatar"),
	RELATIONSHIP_AVATAR_WRAPPER("relationship-avatar-wrapper"),
	RELATIONSHIP_FILTER_CHECKBOX_WRAPPER("relationship-filter-checkbox-wrapper"),
	RELATIONSHIP_FOOTPRINT_WRAPPER("relationship-footprint-wrapper"),
	RELATIONSHIP_NEW("relationship-new"),
	RELATIONSHIP_NEW_WRAPPER("relationship-new-wrapper"),
	RELATIONSHIP_DATE_LABEL("relationship-date-label"),
	RELATIONSHIP_USER_LABEL("relationship-user-label"),
	RELATIONSHIP_LOCATION_LABEL("relationship-location-label"),
	RELATIONSHIP_VIEWED_LABEL("relationship-viewed-label"),
	RELATIONSHIP_USER_UNAVAILABLE("relationship-user-unavailable"),
	RELATIONSHIP_STATE_BUTTONS("relationship-state-buttons"),
	RELATIONSHIP_SYMBOLS("relationship-symbols"),
	RELATIONSHIP_TABLE("relationship-table"),
	RELATIONSHIP_OLD("relationship-old"),
	RELATIONSHIP_USERINFO_WRAPPER("relationship-userinfo-wrapper"),
	RELATIONSHIP_MATCH_WRAPPER("relationship-match-wrapper"),
	RELATIONSHIP_NOTMATCH_WRAPPER("relationship-notmatch-wrapper"),
	RELATIONSHIP_VIEW_WRAPPER("relationship-view-wrapper"),
	RELATIONSHIP_MEMO_WRAPPER("relationship-memo-wrapper"),
	RELATIONSHIP_MEMO_IMAGE("relationship-memo-image"),
	RELATIONSHIP_MEMO_TEXT("relationship-memo-text"),
	TABSHEET_DROPDOWN_GREEN("tabsheet-dropdown-green"),
	TABSHEET_DROPDOWN_LIGHTGREEN("tabsheet-dropdown-lightgreen"),
    TABSHEET_DROPDOWN_WHITE("tabsheet-dropdown-white"),
	UNCHECKED("unchecked"),
	USER_FILTER_COMBOBOX("user-filter-combobox"),
	USER_RATING_WRAPPER("user-rating-wrapper"),
	LASTRELATIONSHIPVIEW_WRAPPER("last-relationship-wrapper"),

	/*question group component*/
	EMPTY_ANSWER("empty-answer"),
	ANSWERED("answered"),

	/*questionnaires views*/
	QUESTIONNAIRE("questionnaire"),
	QUESTIONNAIRE_DROPDOWN("questionnaire-dropdown"),
	QUESTIONNAIRE_VIEW("questionnaire-view"),
	QUESTIONNAIRE_LABEL("questionnaire-label"),
	QUESTIONNAIRE_DESCRIPTION("questionnaire-description"),
	QUESTIONNAIRE_MAIN_HEADER("questionnaire-main-header"),
	QUESTIONNAIRE_ICON_WRAPPER("questionnaire-icon-wrapper"),
    QUESTIONNAIRE_ICON("questionnaire-icon"),
	QUESTION("question"),

    /* multi component panel */
    MULTI_COMPONENT_PANEL("multi-component-panel"),
    MULTI_COMPONENT_CAPTION("multi-component-caption"),
    MULTI_COMPONENT_DESCRIPTION("multi-component-description"),
	
	REQUIRED("required"),
	OPTIONAL("optional"),
	
	SUCCESS("success"),
	DANGER("danger"),
	
	WIZARD_HEADER("wizard-header"),
	WIZARD_STEP("wizard-step"),
	WIZARD_STEP_CAPTIONS("wizard-step-captions"),
	WIZARD_STEP_CAPTION("wizard-step-caption"),
	WIZARD_FOOTER("wizard-footer"),
	WIZARD_FOOTER_NEXT("wizard-footer-next"),
	WIZARD_FOOTER_BACK("wizard-footer-back"),
	
	WIZARD_FOOTER_FINISH("wizard-footer-finish"),

	/*regoin question component and subcomponents*/
	ADD_BUTTON("add-button"),
	SHOW_ADDITIONAL_LOCATION_BUTTON("show-additional-location-button"),
	DELETE_BUTTON("delete-button"),

	HEADER_LABEL("header-label"),
	HIDDEN("hidden"),
	PROXIMITY_ANSWER_COMPONENT("proximity-answer-component"),
	PROXIMITY_SEARCH_REQUEST_ENTRY("proximity-search-request-entry"),
	PROXIMITY_QUESTION_WRAPPER("proximity-question-wrapper"),
	REGION_ANSWER_COMPONENT("region-answer-component"),
	REGION_SEARCH_REQUEST_ENTRY("region-search-request-entry"),
	REGION_SEARCH_REQUEST_ENTRY_SPACER("region-search-request-entry-spacer"),
	REGION_QUESTION_GROUP("region-question-group"),
	REGION_QUESTION_LAYOUT("region-question-layout"),
	REGION_QUESTION_WRAPPER("region-question-wrapper"),
	RELOCATION_COMPONENT("relocation-component"),
	SAVE_BUTTON("save-button"),
	SEARCH_INFO_LABEL("search-info-label"),

	/*subscription view*/
	SUBSCRIPTION_VIEW("subscription-view"),

	/*subscription details view*/
	ABONNEMENT("abonnement"),
	ABONNEMENT_STATE("abonnement-state"),

	/*statistics view*/
	STATISTICS_WRAPPER("statistics-wrapper"),
	STATISTICS_PANEL("statistics-panel"),

	/*sticky footer popup*/
	POPUP_WRAPPER("popup-wrapper"),
	POPUP_CONTENT("popup-content"),
	POPUP_FOOTER("popup-footer"),
	STICKY_FOOTER_COMPONENT("sticky-footer-component"),
	STICKY_FOOTER_POPUP("sticky-footer-popup"),

	/*translation component*/
	REQUIRED_FIELD("required-field"),
	
	/*upload component*/
	FILE_UPLOAD("file-upload"),

	/* useradminmessagetable */
	USER_ADMIN_MESSAGE_TABSHEET_WRAPPER("user-admin-message-tabsheet-wrapper"),

	/* useradminmessageview */
	USER_ADMIN_MESSAGE_TABLE_HEADER("user-admin-message-header"),
	USER_ADMIN_MESSAGE_TABLE("user-admin-message-table"),
	USER_ADMIN_MESSAGE_FOOTER("user-admin-message-footer"),

	/* User data view*/
	USER_DATA_VIEW_WRAPPER("user-data-view-wrapper"),

	/* UserProfileReportDownloadComponent */
	USER_PROFILE_REPORT_DOWNLOAD_WRAPPER("user-profile-report-download-wrapper"),
	USER_PROFILE_REPORT_PLACEHOLDER("report-download-placeholder"),
	
	FORM_PANEL("form-panel"),
	FORM_CONTENT("form-content"),
	FORM_CAPTION("form-caption"),
	FORM_CAPTION_CONTAINER("form-caption-container"),
    FORM_ICON("form-icon"),
	FORM_PART_EMPTY("empty"),
	FORM_DESCRIPTION_LONG("form-description-long"),
	FORM_DESCRIPTION("form-description"),
	FORM_SINGLE_COLUMN_LAYOUT("single-column-layout"),
	FORM_DOUBLE_COLUMN_LAYOUT("double-column-layout"),
	FORM_LABEL("form-label"),
	FORM_REQUIRED("form-required"),
	FORM_LAYOUT("form-layout"),
	FORM_ELEMENT("form-element"),
	FORM_LABEL_LAYOUT("form-label-layout"),
    FORM_BUTTON_DESCRIPTION("form-button-description"),

	/*validation component*/
	LOCATABLE_SEARCH_PANEL_HOME_BUTTON("button-home"),
	LOCATABLE_SEARCH_PANEL_DELETE_BUTTON("button-delete"),

	/* generic component styles */
	COMPONENT_SEPERATOR("component-seperator"),
    TEXTFIELD_CLEARABLE("textfield-clearable"),
    TEXTFIELD_CLEARABLE_WRAPPER("textfield-clearable-wrapper"),
    TEXTFIELD_CLEARABLE_EMPTY("textfield-clearable-empty"),
    TABLE_FILTERBAR("table-filterbar"),

	QUESTIONGROUPTABSHEET_COMMANDROW("question-group-command"),
	COMMAND_FOOTER_ROW("command-footer-row"),

	POPUP("popup"),
	GENERIC_VIEW_HEADER("generic-view-header"),
	GENERIC_VIEW_HEADER_ICON_WRAPPER("generic-view-header-icon-wrapper"),
    GENERIC_VIEW_HEADER_LABEL("generic-view-header-label"),
    GENERIC_VIEW_HEADER_ICON("generic-view-header-icon"),
    GENERIC_VIEW_HEADER_DESCRIPTION("generic-view-header-description"),
	GENERIC_HEADER_BLUE("generic-header-blue"),

	/* Popup-styles */
	POPUP_TYPE_GREEN("popup-type-green"),

	/* Button styles */
	TEXT_BUTTON("text-button"),
	EDIT_BUTTON("edit-button"),
	BUTTON_POSTFIX("postfix-button"),
	LABEL_STYLE("label_style"),

	/* Multi-Column-Layout */
	MULTICOLUMN_LAYOUT("multicolumn-layout"),
    MULTICOLUMN_LAYOUT_COLUMN("multicolumn-layout-column"),
    MULTICOLUMN_LAYOUT_ROW("multicolumn-layout-row"),

	REGISTRATION_SUBSCRITPIONOFFERVIEW_TITLE("subscription-offer-title"),


	/* Admin styles */
	ADMIN_USERMANAGEPOPUP_WRAPPER("usermanage-popup-wrapper"),
	ADMIN_POPUPTABSHEET("admin-popup-tabsheet"),

    AUDIO_STYLE("audio-style"),
	TEXT_COLOR("v-captiontext"),
	/* lazy bean components */
	LAZYBEAN_PAGING_COMPONENT("paging-component"),
	LAZYBEAN_PAGING_LIST("paging-list"),
	LAZYBEAN_PAGING_LIST_FOOTER_CONTOLS("paging-list-footer-controls"),
	LAZYBEAN_PAGING_LIST_PAGECOUNTER("paginglist-pagecounter"),
	LAZYBEAN_PAGING_LIST_ROW("paginglist-row"),
	LAZYBEAN_PAGING_LIST_COLUMN("paginglist-column"),
	LAZYBEAN_PAGING_LIST_ROW_SELECTED("selected"),
	LAZYBEAN_PAGING_LIST_COLUMN_SELECT("paginglist-column-select"),
	LAZYBEAN_PAGING_LIST_HEADER("paging-list-header"),

	MESSAGE_TABLE_COLUMN_READ_STATUS("message-table-column-read"),
	MESSAGE_TABLE_COLUMN_SENDER("message-table-column-sender"),
	MESSAGE_TABLE_COLUMN_SUBJECT("message-table-column-subject"),
	MESSAGE_TABLE_COLUMN_ATTACHMENT("message-table-column-attachment"),
	MESSAGE_TABLE_COLUMN_DATE("message-table-column-date"),
	MESSAGE_TABLE_COLUMN_VIEWED("message-table-column-viewed"),
	MESSAGE_TABLE_COLUMN_REPLY("message-table-column-reply"),
	IMPRESSUM_LABEL("impressum-label"),

	COMBOBOX_LANGUAGE_SELECTOR("language-selector");

	private final String styleName;

	CssStyle(String styleName)
	{
		this.styleName = styleName;
	}

	public String getStyleName()
	{
		return styleName;
	}
}
