package de.binaerebauten.gleichklang.core.view.component.message;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.message.*;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.utils.LocaleAware;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanPagingComponent;
import de.binaerebauten.gleichklang.core.view.component.TextFieldWithClearComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.filter.AbstractCategoryFilter;
import de.binaerebauten.gleichklang.core.view.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter.NestedAttributeFilter1;
import de.binaerebauten.gleichklang.core.view.filter.SimpleStringFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleStringFilter.NestedStringFilter2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("serial")
public class MessageTable extends CustomComponent implements LocaleAware
{

	public static class FilterConfiguration
	{
		public enum Filter
		{
			ALIAS,
			SUBJECT,
			ATTACHMENT,
			UNREAD,
			CANCEL_MESSAGE
		}
		
		private final EnumSet<Filter> activatedFilter;
		
		public FilterConfiguration(boolean activateFilter)
		{
			activatedFilter = activateFilter ? EnumSet.allOf(Filter.class) : EnumSet.noneOf(Filter.class);
		}
		
		public void setActivatedFilter(Filter... filter)
		{
			this.activatedFilter.clear();
			this.activatedFilter.addAll(Arrays.asList(filter));
		}
		
		public void addActivatedFilter(Filter... filter)
		{
			this.activatedFilter.addAll(Arrays.asList(filter));
		}
		
		public void removeActivatedFilter(Filter... filter)
		{
			this.activatedFilter.removeAll(Arrays.asList(filter));
		}
	}
	
	public interface NewButtonClickListener
	{
		void newButtonClicked();
	}
	
	public interface OpenButtonClickListener
	{
		void openButtonClicked(Message message);
	}
	
	public interface TableButtonClickListener
	{
		void buttonClicked(Set<Message> messages);
	}
	
	public enum Directory
	{
		INCOMING,
		OUTGOING,
		DRAFT,
		DELETED
	}
	
	private enum IconColumn
	{
		REPLY,
		UNREAD,
		ATTACHMENT,
		READ_BY_RECEIVER
	}
	
	private enum TextColumn
	{
		FROM,
		TO,
		SENT,
		SUBJECT,
		READ_BY_RECEIVER
	}

	private static final String REPLY_COLUMN = "reply_column";
	
	private static class MessageUserAliasFilter extends SimpleStringFilter<Message>
	{
		@Override
		protected Collection<Path<String>> getSimplePath(Root<Message> root)
		{
			final Path<String> receiverPath = root.get(Message_.receiverEnvelope).get(Envelope_.user).get(User_.alias);
			final Path<String> senderPath = root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.alias);
			
			return Arrays.asList(receiverPath, senderPath);
		}
	}
	
	public static class CategoryFilter extends AbstractCategoryFilter<Message>
	{
		public CategoryFilter(boolean initialActivated, Collection<RecommendationCategory> visibleCategories)
		{
			super(initialActivated, visibleCategories);
		}
		
		@Override
		protected Specification<Message> createSingleFilter(RecommendationCategory category)
		{
			return (root, query, cb) ->
			{
				final Subquery<Message> sq = query.subquery(Message.class);
				final Root<Message> m = sq.from(Message.class);
				final Root<Relationship> r = sq.from(Relationship.class);
				
				final Predicate restriction = cb.and(
						cb.equal(m.join(Message_.senderEnvelope).get(SenderEnvelope_.user), r.get(Relationship_.sourceUser)),
						cb.equal(m.join(Message_.receiverEnvelope).get(ReceiverEnvelope_.user), r.get(Relationship_.targetUser)),
						cb.isMember(category, r.get(Relationship_.categories))
				);
				
				sq.where(restriction).select(m);
				
				return cb.in(root).value(sq);
			};
		}
	}
	
	private final HorizontalLayout tableFooter;
	private final ComponentContainer rootLayout;
	private final Directory directory;
	private final DateTimeFormatter dateTimeFormatter;
	private final Button openButton;
	private final Button newButton;
	private CategoryFilter categoryFilter = null;
	private LazyBeanPagingComponent<Message> messageTable;
	private OpenButtonClickListener openButtonClickListener = null;
	private NewButtonClickListener newButtonClickListener = null;
	private Admin currentAdmin;

	public MessageTable(Directory directory, boolean withFilter, Device device, Admin currentAdmin)
	{
		this(directory, new FilterConfiguration(withFilter), device, currentAdmin);
	}
	
	public MessageTable(Directory directory, FilterConfiguration filterConfiguration, Device device, Admin currentAdmin)
	{
		Objects.requireNonNull(directory);
		Objects.requireNonNull(device);

		this.directory = directory;
		
		dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(LocaleAware.super.getLocale());
		
		tableFooter = createTableFooter();
		
		openButton = createOpenButton();
		newButton = createNewButton();
		messageTable = createTable(device);
		
		tableFooter.addComponent(newButton, 0);
		tableFooter.addComponent(openButton, 1);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.addComponent(createFilterComponent(filterConfiguration));
		layout.addComponents(tableFooter, messageTable);
		layout.setComponentAlignment(tableFooter, Alignment.MIDDLE_LEFT);
		
		rootLayout = layout;
		setCompositionRoot(rootLayout);
		this.currentAdmin = currentAdmin;

	}
	
	private Component createFilterComponent(FilterConfiguration filterConfiguration)
	{
		final AbstractFilter<Message, String, String> aliasFilter;
		final AbstractFilter<Message, String, String> subjectFilter;
		final AbstractFilter<Message, Long, Long> attachmentFilter;
		final AbstractFilter<Message, Boolean, Boolean> unreadFilter;
		final AbstractFilter<Message, MessageType, MessageType> cancelMessageFilter;
		
		subjectFilter = new SimpleStringFilter<>(Message_.subject, true);
		attachmentFilter = new SimpleAttributeFilter<>(Message_.sumAttachements, true);
		unreadFilter = new NestedAttributeFilter1<>(Message_.receiverEnvelope, ReceiverEnvelope_.read);
		cancelMessageFilter = new SimpleAttributeFilter<>(Message_.messageType, true);
		
		switch (directory)
		{
			case INCOMING:
				aliasFilter = new NestedStringFilter2<>(Message_.senderEnvelope, ReceiverEnvelope_.user, User_.alias);
				break;
			case DRAFT:
			case OUTGOING:
				aliasFilter = new NestedStringFilter2<>(Message_.receiverEnvelope, ReceiverEnvelope_.user, User_.alias);
				break;
			default:
				aliasFilter = new MessageUserAliasFilter();
		}
		
		aliasFilter.setItemComponent(messageTable);
		subjectFilter.setItemComponent(messageTable);
		attachmentFilter.setItemComponent(messageTable);
		unreadFilter.setItemComponent(messageTable);
		cancelMessageFilter.setItemComponent(messageTable);
		
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		
		final TextFieldWithClearComponent aliasTextField = new TextFieldWithClearComponent(ComponentFactory.getInstance().createField(TextField.class, I18N.MESSAGETABLE_FILTER_ALIAS.msg()));
		aliasTextField.getTextField().addTextChangeListener(event ->
		{
			aliasFilter.setValue(event.getText());
		});
		
		final TextFieldWithClearComponent subjectTextField = new TextFieldWithClearComponent(ComponentFactory.getInstance().createField(TextField.class, I18N.MESSAGETABLE_FILTER_SUBJECT.msg()));
		subjectTextField.getTextField().addTextChangeListener(event ->
		{
			subjectFilter.setValue(StringUtils.deSanitizeSpecialCharacters(event.getText()));
		});
		
		final CheckBox attachmentCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.MESSAGETABLE_FILTER_ATTACHEMENT.msg());
		attachmentCheckBox.addValueChangeListener(event ->
		{
			attachmentFilter.setValue(attachmentCheckBox.getValue() ? 0L : null);
		});
		
		final CheckBox unreadCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.MESSAGETABLE_FILTER_UNREAD.msg());
		unreadCheckBox.addValueChangeListener(event ->
		{
			unreadFilter.setValue(unreadCheckBox.getValue() ? false : null);
		});
		unreadCheckBox.setVisible(directory == Directory.INCOMING);
		
		final CheckBox cancelMessageCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.MESSAGETABLE_FILTER_CANCELMESSAGE.msg());
		cancelMessageCheckBox.addValueChangeListener(event ->
		{
			cancelMessageFilter.setValue(cancelMessageCheckBox.getValue() ? MessageType.CANCEL_MESSAGE : null);
		});
		cancelMessageCheckBox.setVisible(directory != Directory.DRAFT);
		cancelMessageCheckBox.setValue(true);
		cancelMessageCheckBox.addStyleName(CssStyle.CANCEL_CHECKBOX.getStyleName());
		
		for(FilterConfiguration.Filter filter : filterConfiguration.activatedFilter)
		{
			switch(filter)
			{
				case ALIAS:
					layout.addComponent(aliasTextField);
					break;
				case SUBJECT:
					layout.addComponent(subjectTextField);
					break;
				case ATTACHMENT:
					layout.addComponent(attachmentCheckBox);
					break;
				case UNREAD:
					layout.addComponent(unreadCheckBox);
					break;
				case CANCEL_MESSAGE:
					layout.addComponent(cancelMessageCheckBox);
					break;
			}
		}
		
		layout.setStyleName(CssStyle.TABLE_FILTERBAR.getStyleName());

		return layout;
	}
	
	public void onDeviceChanged(Device device)
	{
		Objects.requireNonNull(device);
		
		final LazyBeanPagingComponent<Message> messageTable = createTable(device);
		
		messageTable.setHandler(this.messageTable.getHandler());
		
		rootLayout.replaceComponent(this.messageTable, messageTable);
		this.messageTable = messageTable;
	}
	
	private HorizontalLayout createTableFooter()
	{
		final HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setStyleName(CssStyle.USER_MESSAGE_FOOTER.getStyleName());
		return tableFooter;
	}
	
	private LazyBeanPagingComponent<Message> createTable(Device device)
	{
		final LazyBeanPagingComponent<Message> table = new LazyBeanPagingComponent<>();
		
		table.setStyleName(CssStyle.MESSAGE_TABLE.getStyleName());
		table.addValueChangeListener(this::onMessageSelect);
		table.addFilter(categoryFilter);
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSizeFull();
		table.initView(device);
		table.addItemClickListener(this::onOpenMessage);
		table.setCellStyleGenerator((source, itemId, propertyId) -> getColumnStyle(propertyId));
		
		if (device == Device.DESKTOP)
			initDesktopTable(table);
		else
			initMobileTable(table, device);

		return table;
	}

	private CssStyle getColumnStyle(Object columnId) {
	    if (columnId == null) {
	        return null;
        }

	    if (columnId.equals(I18N.MESSAGETABLE_HEADER_SENDER.msg()) || columnId.equals(I18N.MESSAGETABLE_HEADER_RECEIVER.msg())) {
	        return CssStyle.MESSAGE_TABLE_COLUMN_SENDER;
        } else if (columnId.equals(I18N.MESSAGETABLE_HEADER_SUBJECT.msg())) {
	        return CssStyle.MESSAGE_TABLE_COLUMN_SUBJECT;
        } else if (columnId.equals(I18N.MESSAGETABLE_ATTACHMENT.msg())) {
	        return  CssStyle.MESSAGE_TABLE_COLUMN_ATTACHMENT;
        } else if (columnId.equals(I18N.MESSAGETABLE_HEADER_SENDDATE.msg())) {
	        return CssStyle.MESSAGE_TABLE_COLUMN_DATE;
        } else if (columnId.equals(I18N.MESSAGETABLE_HEADER_READ.msg())) {
	        return CssStyle.MESSAGE_TABLE_COLUMN_READ_STATUS;
        } else if (columnId.equals(I18N.MESSAGETABLE_HEADER_VIEWED.msg())) {
	        return CssStyle.MESSAGE_TABLE_COLUMN_VIEWED;
        } else if (columnId.equals(REPLY_COLUMN)) {
	    	return CssStyle.MESSAGE_TABLE_COLUMN_REPLY;
		}

	    return null;
    }
	
	private void initDesktopTable(LazyBeanPagingComponent<Message> table)
	{
		switch (directory)
		{
			case INCOMING:
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_READ.msg(), createIconColumn(IconColumn.UNREAD));
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SENDER.msg(), itemId -> createLabel(itemId, TextColumn.FROM, Device.DESKTOP));
				table.addGeneratedColumn(REPLY_COLUMN, createIconColumn(IconColumn.REPLY));
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SUBJECT.msg(), this::createSubjectColumn);

				break;
			case OUTGOING:
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_RECEIVER.msg(), itemId -> createLabel(itemId, TextColumn.TO, Device.DESKTOP));
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_VIEWED.msg(), createIconColumn(IconColumn.READ_BY_RECEIVER));
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SUBJECT.msg(), this::createSubjectColumnForOutgoing);
				break;
			case DRAFT:
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_RECEIVER.msg(), m -> getValue(m) );
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SUBJECT.msg(), this::createSubjectColumn);

				break;
			case DELETED:
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SENDER.msg(), m -> getSenderAlias(m));
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_RECEIVER.msg(), m-> getValue(m) );
				table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SUBJECT.msg(), this::createSubjectColumnForDeletedMessage);

				break;
		}


		table.addGeneratedColumn(I18N.MESSAGETABLE_ATTACHMENT.msg(), createIconColumn(IconColumn.ATTACHMENT));
		table.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SENDDATE.msg(), this::createMessageDateTime);


		if (directory == Directory.DRAFT)
			table.setSortPropertyId(false, Message_.createDate);
		else
			table.setSortPropertyId(false, Message_.sendDate);

		table.setColumnHeaderVisible(false);
	}

	private String getSenderAlias(Message m)
	{

		if(m!=null && m.getSenderEnvelope()!=null && m.getSenderEnvelope().getUser()!=null && m.getSenderEnvelope().getUser().getAlias()!=null){
			return m.getSenderEnvelope().getUser().getAlias();
		}
		return "";
	}

	private String getValue(Message m)

	{
		if(m!=null && m.getReceiverEnvelope()!=null && m.getReceiverEnvelope().getUser()!=null && m.getReceiverEnvelope().getUser().getAlias()!=null){
			return m.getReceiverEnvelope().getUser().getAlias();
		}
		return "";
	}
	
	private String createSubjectColumn(Message message)
	{
		//TODO : Changes for blocking messages of spam users


		if(currentAdmin!=null && message.getSenderEnvelope() != null && message.getSenderEnvelope().getUser() != null && (message.getSenderEnvelope().getUser().isBlocked() || message.getReceiverEnvelope().getUser().isBlocked())){
			return StringUtils.deSanitizeSpecialCharacters(message.getSubject()+" (BLOCKED)");
		}

//		if(message.getSenderEnvelope() != null && message.getSenderEnvelope().getUser() != null && message.getSenderEnvelope().getUser().isBlocked())
//		{
//
//				//return I18N.IN_REVIEW_BY_TEAM.msg();
//		}


		return StringUtils.deSanitizeSpecialCharacters(message.getSubject());
	}

	private String createSubjectColumnForDeletedMessage(Message message)
	{
		//TODO : Changes for blocking messages of spam users

		return StringUtils.deSanitizeSpecialCharacters(message.getSubject());
	}

	private String createSubjectColumnForOutgoing(Message message) {
		//TODO : Changes for blocking messages of spam users

		if(message.getSubject().contains("(Mobile")){
			message.setSubject(message.getSubject().split("\\(Mobile")[0]);
		}
		else if(message.getSubject().contains("(Desktop")){
			message.setSubject(message.getSubject().split("\\(Desktop")[0]);
		}

//		if (message.getReceiverEnvelope() != null && message.getReceiverEnvelope().getUser() != null && message.getReceiverEnvelope().getUser().isBlocked()) {
//			return I18N.IN_REVIEW_BY_TEAM.msg();
//		}
//       if {
			if(currentAdmin!=null && message.getSenderEnvelope() != null && message.getSenderEnvelope().getUser() != null && (message.getSenderEnvelope().getUser().isBlocked() || message.getReceiverEnvelope().getUser().isBlocked())){
				return StringUtils.deSanitizeSpecialCharacters(message.getSubject()+" (BLOCKED)");
			}
			else {
				return StringUtils.deSanitizeSpecialCharacters(message.getSubject());
			}
		}
	//}

	
	private String createMessageDateTime(Message message)
	{
		LocalDateTime dateTime = message.getSendDate();
		if(dateTime == null)
		{
			dateTime = message.getChangeDate();
			if(dateTime == null) dateTime = message.getCreateDate();
		}
		return dateTime.format(dateTimeFormatter);
	}
	
	private void initMobileTable(LazyBeanPagingComponent<Message> table, Device device)
	{
		table.addGeneratedColumn(I18N.MESSAGETABLE_MESSAGE.msg(), (source, itemId, columnId) ->
		{
			switch (directory)
			{
				case INCOMING:
					return createIncomingCell(itemId, device);
				case OUTGOING:
					return createOutgoingCell(itemId, device);
				case DRAFT:
					return createDraftCell(itemId, device);
				case DELETED:
					return createDeletedCell(itemId, device);
			}
			
			return new Label();
		});

		table.setColumnHeaderVisible(false);
		table.setSortPropertyId(false, Message_.sendDate);
	}
	
	private Component createIncomingCell(Message message, Device device)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.MESSAGE_CELL_WRAPPER.getStyleName());
		
		final HorizontalLayout firstLine = new HorizontalLayout();
		final HorizontalLayout secondLine = new HorizontalLayout();
		final HorizontalLayout thirdLine = new HorizontalLayout();
		
		final Label sentDate = createLabel(message, TextColumn.SENT, device);
		sentDate.addStyleName(CssStyle.SMALL.getStyleName());
		
		final Label subject = createLabel(message, TextColumn.SUBJECT, device);
		subject.addStyleName(message.getReceiverEnvelope().isRead() ? CssStyle.READ.getStyleName() : CssStyle.UNREAD.getStyleName());
		
		firstLine.addComponent(createLabel(message, TextColumn.FROM, device));
		secondLine.addComponent(subject);
		thirdLine.addComponent(sentDate);
		thirdLine.addComponent(createLabel(message, IconColumn.ATTACHMENT));
		thirdLine.addComponent(createLabel(message, IconColumn.REPLY));

		thirdLine.setPrimaryStyleName("adjust");
		
		layout.addComponent(firstLine);
		layout.addComponent(secondLine);
		layout.addComponent(thirdLine);
		
		return layout;
	}
	
	private Component createOutgoingCell(Message message, Device device)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.MESSAGE_CELL_WRAPPER.getStyleName());

		final HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setWidthUndefined();
		wrapper.setStyleName(CssStyle.MESSAGE_HEADER_ROW.getStyleName());
		final Label receiver = createLabel(message, TextColumn.TO, device);
		receiver.setWidthUndefined();
		final Label viewed = createLabel(message, TextColumn.READ_BY_RECEIVER, device);
		wrapper.addComponents(receiver, viewed);

		layout.addComponent(wrapper);

		final Label subject = createLabel(message, TextColumn.SUBJECT, device);
		subject.setStyleName(CssStyle.MESSAGE_SUBJECT.getStyleName());
		subject.setSizeUndefined();
		layout.addComponent(subject);
		layout.addComponent(createLabel(message, TextColumn.SENT, device));
		
		return layout;
	}
	
	private Component createDraftCell(Message message, Device device)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.MESSAGE_CELL_WRAPPER.getStyleName());
		
		layout.addComponent(createLabel(message, TextColumn.TO, device));
		layout.addComponent(createLabel(message, TextColumn.SUBJECT, device));
		
		return layout;
	}
	
	private Component createDeletedCell(Message message, Device device)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.MESSAGE_CELL_WRAPPER.getStyleName());
		
		layout.addComponent(createLabel(message, TextColumn.FROM, device));
		layout.addComponent(createLabel(message, TextColumn.TO, device));
		layout.addComponent(createLabel(message, TextColumn.SUBJECT, device));
		layout.addComponent(createLabel(message, TextColumn.SENT, device));
		
		return layout;
	}
	
	private Function<Message, Component> createIconColumn(IconColumn iconColumn)
	{
		switch (iconColumn)
		{
			case ATTACHMENT:
				return itemId -> itemId.getSumAttachements() > 0 ?
						new Label(FontAwesome.PAPERCLIP.getHtml(), ContentMode.HTML) : new Label();
			case UNREAD:
				return itemId -> itemId.getReceiverEnvelope().isRead() ?
                        new Image(null, new ThemeResource("img/mail-read.svg")) : new Label(FontAwesome.ENVELOPE.getHtml(), ContentMode.HTML);
			case REPLY:
				return itemId -> itemId.getReplyToMessage() == null ?
						new Label() : new Label(FontAwesome.ARROW_LEFT.getHtml(), ContentMode.HTML);
			case READ_BY_RECEIVER:
				return itemId -> itemId.getReceiverEnvelope().isRead() ?
						new Label(FontAwesome.EYE.getHtml(), ContentMode.HTML) : new Label();
		}
		
		return itemId -> new Label();
	}
	
	private Label createLabel(Message message, IconColumn column)
	{
		Label result = new Label();
		
		switch (column)
		{
			case REPLY:
				if (message.getSumReplies() > 0)
				{
					final Label answered = new Label();
					answered.setIcon(FontAwesome.ARROW_RIGHT);
					answered.addStyleName(CssStyle.MESSAGE_ANSWERED_ICON.getStyleName());
					result = answered;
				}
				break;
			case UNREAD:
				if (!message.getReceiverEnvelope().isRead())
				{
					final Label unread = new Label();
					unread.setIcon(FontAwesome.ENVELOPE);
					unread.addStyleName(CssStyle.MESSAGE_UNREAD_ICON.getStyleName());
					result = unread;
				}
				break;
			case ATTACHMENT:
				if (message.getSumAttachements() > 0)
				{
					final Label label = new Label();
					label.setIcon(FontAwesome.PAPERCLIP);
					label.setStyleName(CssStyle.MESSAGE_ATTACHMENT_ICON.getStyleName());
					result = label;
				}
				break;
		}
		
		return result;
	}
	
	private Label createAliasLabel(User user, I18N label, Device device)
	{
		if (user == null)
			return createLabel(I18N.MESSAGETABLE_TEAM_GK.msg(), label, device);
		
		return createLabel(user.getAlias(), label, device);
	}
	
	private Label createLabel(Message message, TextColumn column, Device device)
	{
		switch (column)
		{
			case FROM:
				return createAliasLabel(message.getSenderEnvelope().getUser(), I18N.MESSAGETABLE_LABEL_FROM, device);
			case TO:
				return createAliasLabel(message.getReceiverEnvelope().getUser(), I18N.MESSAGETABLE_LABEL_TO, device);
			case SENT:
				return createLabel(createMessageDateTime(message), I18N.MESSAGETABLE_LABEL_SENT, device);
			case SUBJECT:
				if(message.getSubject().contains("(Mobile")){
					message.setSubject(message.getSubject().split("\\(Mobile")[0]);
				}
				else if(message.getSubject().contains("(Desktop")){
					message.setSubject(message.getSubject().split("\\(Desktop")[0]);
				}
				if(message.getSenderEnvelope() != null && message.getSenderEnvelope().getUser() != null && message.getSenderEnvelope().getUser().isBlocked())
				{
					return createLabel(StringUtils.deSanitizeSpecialCharacters(I18N.IN_REVIEW_BY_TEAM.msg()), I18N.MESSAGETABLE_LABEL_SUBJECT, device);
				}
				return createLabel(StringUtils.deSanitizeSpecialCharacters(message.getSubject()), I18N.MESSAGETABLE_LABEL_SUBJECT, device);
			case READ_BY_RECEIVER:
				final Label label = new Label();
				if (message.getReceiverEnvelope().isRead())
					label.setIcon(FontAwesome.EYE);
				return label;
		}
		
		return new Label();
	}
	
	private Label createLabel(String value, I18N label, Device device)
	{
		return device == Device.MOBILE ? new Label(value, ContentMode.HTML) : new Label(label.msg() + " " + value, ContentMode.HTML);
	}
	
	private void onMessageSelect(Set<Message> messages)
	{
		//exclude open and new-button
		for (int i = 2; i < tableFooter.getComponentCount(); i++)
		{
			tableFooter.getComponent(i).setEnabled(messages != null && messages.size() >= 1);
		}
		openButton.setEnabled(messages != null && messages.size() == 1);
		//always enabled
		newButton.setEnabled(true);
		
	}
	
	private Button createOpenButton()
	{
		final Button openButton = new Button();
		openButton.setCaption(I18N.MESSAGETABLE_READBUTTON.msg());
		openButton.setIcon(FontAwesome.EYE);
		openButton.setEnabled(false);
		openButton.setVisible(false);
		
		openButton.addClickListener(event -> onOpenMessage(messageTable.getValue()));
		
		return openButton;
	}
	
	private Button createNewButton()
	{
		final Button newButton = new Button();
		newButton.setCaption(I18N.MESSAGETABLE_NEWBUTTON.msg());
		newButton.setIcon(FontAwesome.PLUS_CIRCLE);
		newButton.setStyleName(CssStyle.BUTTON_NEW.getStyleName());
		newButton.addStyleName(CssStyle.MESSAGE_BUTTON_NEW.getStyleName());
		newButton.setVisible(false);
		
		newButton.addClickListener(event -> newButtonClickListener.newButtonClicked());
		
		return newButton;
	}
	
	public void addTableButton(Resource icon, String caption, TableButtonClickListener listener)
	{
		Objects.requireNonNull(listener);
		
		final Button tableButton = new Button(caption);
		tableButton.setIcon(icon);
		tableButton.setEnabled(false);
		tableButton.addClickListener(event -> listener.buttonClicked(messageTable.getValues()));
		
		if (tableFooter.getComponentCount() == 3)
			tableButton.setStyleName(CssStyle.MESSAGE_UNREAD_BUTTON.getStyleName());
		
		tableFooter.addComponent(tableButton, tableFooter.getComponentCount());
	}

	public void clearValue()
	{
		messageTable.clearValue();
	}
	
	public void refresh()
	{
		messageTable.refresh();
	}
	
	public void setMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler)
	{
		messageTable.setHandler(handler);
	}
	
	public void setMessagesHandler(LazyBeanItemsHandler<Message> handler)
	{
		messageTable.setHandler(handler);
	}
	
	public void setCategoryFilter(CategoryFilter categoryFilter)
	{
		messageTable.replaceFilter(this.categoryFilter, categoryFilter);
		this.categoryFilter = categoryFilter;
	}
	
	public void setOpenButtonClickListener(OpenButtonClickListener openButtonClickListener)
	{
		this.openButtonClickListener = openButtonClickListener;
		openButton.setVisible(openButtonClickListener != null);
	}
	
	public void setNewButtonClickListener(NewButtonClickListener newButtonClickListener)
	{
		this.newButtonClickListener = newButtonClickListener;
		newButton.setVisible(newButtonClickListener != null);
	}
	
	private void onOpenMessage(Message message)
	{
		if(openButtonClickListener != null) openButtonClickListener.openButtonClicked(message);
	}
	
	public Directory getDirectory()
	{
		return directory;
	}

	public void setSelectable(boolean  selectable)
	{
		messageTable.setSelectable(selectable);
	}
}
