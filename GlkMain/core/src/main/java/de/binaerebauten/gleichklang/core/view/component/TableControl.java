package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.model.SourceUserEntity;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.message.*;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import org.hibernate.mapping.Join;
import org.springframework.data.jpa.domain.Specification;

import javax.management.relation.Relation;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 *
 * This class generate typically control units for a table. Everything is possible, nothing is necessary. ;)
 *
 * - Creation
 * - Editing
 * - Deleting
 * - Undeleting
 * - Moving
 *
 * Warning: By using {@link UndeleteCallback} {@link T} must extend {@link
 * DeletableEntity}! Explanation: This {@link TableControl} can also be used for
 * editing, creating, etc. for not deletable entities. So it would not be
 * necessary to inheritance from {@link DeletableEntity}. This can also be
 * reached with inheritance of this class, but therefore it is to much
 * overhead.
 *
 * @param <T>
 */
public class TableControl<T extends BaseEntity> extends CustomComponent {
	public interface NewCallback {
		void newItem();
	}

	public interface EditCallback<T> {
		void editItem(T item);
	}

	public interface DeleteCallback<T> {
		void deleteItem(T item);
	}

	public interface UndeleteCallback<T> {
		void undeleteItem(T item);
	}

	public interface MoveHandler<T> {
		Map<Integer, T> moveItems(Map<Integer, T> selectedItems, Direction direction, Specification<T> specification);
	}

	public interface ItemButtonCallback<T> {
		void clickedButton(T item);
	}

	public interface ButtonCallback {
		void clickedButton();
	}

	public interface ShowButtonCallback<T> {
		boolean onButtonShow(T item);
	}

	public enum Direction {
		UP,
		DOWN
	}

	private final AbstractOrderedLayout layout;
	private final HorizontalLayout controlLayout;
	private User currenSelectedUser;
	private final Button newButton;
	private final Button editButton;
	private final Button deleteButton;
	private final Button undeleteButton;
	private final Button upButton;
	private final Button downButton;
	private final Map<Button, ShowButtonCallback<T>> otherItemButtons = new HashMap<>();
	private final List<Button> otherButtons = new ArrayList<>();

	private final CheckBox showDeletedCheckBox;
	private final CheckBox oneDirectionCheckBox;
	private final CheckBox deletedSuggestionsCheckBox;
	private final LazyBeanItemComponent<T> table;

	private final Specification<T> noDeletedFilter;
	private Specification<T> oneDirectionFilter;
	private Specification<T> onlyDeletedSuggestionFilter;
	private final Label tableSize;
	private NewCallback newCallback = null;
	private EditCallback<T> editCallback = null;
	private DeleteCallback<T> deleteCallback = null;
	private UndeleteCallback<T> undeleteCallback = null;
	private MoveHandler<T> moveHandler = null;

	public TableControl(LazyBeanItemComponent<T> table) {
		noDeletedFilter = (root, query, cb) -> cb.equal(root.get(DeletableEntity.DELETED), false);

		this.table = table;

		layout = new VerticalLayout();
		layout.setSpacing(true);

		controlLayout = new HorizontalLayout();
		controlLayout.setSpacing(true);
		controlLayout.setHeightUndefined();

		tableSize = new Label(I18N.TABLECONTROL_LABEL_COUNT.msg(table.getSize()));
		table.addSizeChangedListener(size -> tableSize.setValue(I18N.TABLECONTROL_LABEL_COUNT.msg(size)));

		newButton = new Button(I18N.TABLECONTROL_ACTION_NEW.msg(), event -> newCallback.newItem());
		editButton = new Button(I18N.TABLECONTROL_ACTION_EDIT.msg(), event -> editValues());
		deleteButton = new Button(I18N.TABLECONTROL_ACTION_DELETE.msg(), event -> deleteValues());
		undeleteButton = new Button(I18N.TABLECONTROL_ACTION_UNDELETE.msg(), event -> undeleteValues());
		upButton = new Button(I18N.TABLECONTROL_ACTION_UP.msg(), event -> moveValues(Direction.UP));
		downButton = new Button(I18N.TABLECONTROL_ACTION_DOWN.msg(), event -> moveValues(Direction.DOWN));
		showDeletedCheckBox = new CheckBox(I18N.TABLECONTROL_ACTION_SHOWDELETED.msg());
		showDeletedCheckBox.addValueChangeListener(event -> refreshTableFilter());
		oneDirectionCheckBox = new CheckBox("Show one Direction");
		oneDirectionCheckBox.addValueChangeListener(event -> refreshTableFilter());
		deletedSuggestionsCheckBox = new CheckBox("Only Deleted Suggestions");
        deletedSuggestionsCheckBox.addValueChangeListener(event -> refreshTableFilter());


		newButton.setVisible(false);
		editButton.setVisible(false);
		deleteButton.setVisible(false);
		undeleteButton.setVisible(false);
		upButton.setVisible(false);
		downButton.setVisible(false);
		showDeletedCheckBox.setVisible(false);
		if (table.getDescription() != null && table.getDescription().equalsIgnoreCase("Beziehungen")) {
			oneDirectionCheckBox.setVisible(true);
			deletedSuggestionsCheckBox.setVisible(true);
		} else {
			oneDirectionCheckBox.setVisible(false);
			deletedSuggestionsCheckBox.setVisible(false);
		}


		editButton.setEnabled(false);
		upButton.setEnabled(false);
		downButton.setEnabled(false);

		table.addValueChangeListener(this::refreshControlPanel);

		controlLayout.addComponents(newButton, editButton, deleteButton, undeleteButton, upButton, downButton, showDeletedCheckBox, oneDirectionCheckBox, deletedSuggestionsCheckBox);
		controlLayout.setComponentAlignment(showDeletedCheckBox, Alignment.MIDDLE_LEFT);
		controlLayout.setComponentAlignment(oneDirectionCheckBox, Alignment.MIDDLE_RIGHT);
		controlLayout.setComponentAlignment(deletedSuggestionsCheckBox, Alignment.MIDDLE_RIGHT);

		layout.addComponents(controlLayout, tableSize, table);
		layout.setSizeFull();
		layout.setExpandRatio(controlLayout, 0.0f);
		layout.setExpandRatio(table, 1.0f);

		setCompositionRoot(layout);
	}

	//    public void oneDirectionFilterSpecification(User user) {
//        final Specification<Relationship> mailCountSpecification = (root, query, cb) ->
//        {
//            final Predicate oneDirection = cb.equal(root.join(Relationship_.sourceUser).get(User_.alias), currenSelectedUser.getAlias());
//            return  oneDirection;
//        };
//    }
//    public static class OneDirectionFilterSpecification implements Specification<Relationship>
//    {
//        private final User user;
//
//        public OneDirectionFilterSpecification()
//        {
//            this(null);
//        }
//
//        public OneDirectionFilterSpecification(User user)
//        {
//            this.user = user;
//        }
//
//        @Override
//        public Predicate toPredicate(Root<Relationship> root, CriteriaQuery<?> query, CriteriaBuilder cb)
//        {
//
//            final Predicate oneDirection = cb.equal(root.join(Relationship_.sourceUser).get(User_.alias), user.getAlias());
//            return  oneDirection;
//
//        }
//    }
	private void refreshControlPanel(Set<T> values) {
		final int size = values != null ? values.size() : 0;

		editButton.setEnabled(size == 1);
		upButton.setEnabled(size > 0);
		downButton.setEnabled(size > 0);

		if (size == 1) {
			final T value = values.iterator().next();
			if (value instanceof DeletableEntity) {
				final DeletableEntity<?> entity = (DeletableEntity<?>) value;
				deleteButton.setVisible(!entity.isDeleted() && deleteCallback != null);
				undeleteButton.setVisible(entity.isDeleted() && undeleteCallback != null);
			} else {
				deleteButton.setVisible(deleteCallback != null);
				undeleteButton.setVisible(undeleteCallback != null && deleteCallback == null);
			}

			otherItemButtons.keySet().forEach(button ->
			{
				final ShowButtonCallback<T> showButtonCallback = otherItemButtons.get(button);
				button.setEnabled(showButtonCallback == null || showButtonCallback.onButtonShow(values.iterator().next()));
			});
		} else {
			deleteButton.setVisible(false);
			undeleteButton.setVisible(false);

			otherItemButtons.keySet().forEach(button -> button.setEnabled(false));
		}
	}

	private void refreshTableFilter() {
		table.clearValue();

		if (showDeletedCheckBox.getValue() || !showDeletedCheckBox.isVisible()) {
			table.removeFilter(noDeletedFilter);
		} else {
			table.addFilter(noDeletedFilter);
		}

		if (currenSelectedUser != null) {
			if (oneDirectionCheckBox.getValue() || !oneDirectionCheckBox.isVisible()) {
				table.addFilter(oneDirectionFilter);
			} else {
				table.removeFilter(oneDirectionFilter);
			}
		}
		if (deletedSuggestionsCheckBox.getValue() || !deletedSuggestionsCheckBox.isVisible()) {
			table.removeFilter(noDeletedFilter);
			table.addFilter(onlyDeletedSuggestionFilter);
		} else {
			table.removeFilter(onlyDeletedSuggestionFilter);
		}


		refreshControlPanel(null);
	}

	private void moveValues(Direction direction) {
		table.setValue(moveHandler.moveItems(table.getValuesWithIndex(), direction, table.getSpecification()));
		table.refresh();
	}

	private void deleteValues() {
		deleteCallback.deleteItem(table.getValues().iterator().next());
		table.clearValue();
		refreshControlPanel(null);
	}

	private void undeleteValues() {
		undeleteCallback.undeleteItem(table.getValues().iterator().next());
		table.clearValue();
		refreshControlPanel(null);
	}

	private void editValues() {
		editCallback.editItem(table.getValues().iterator().next());
	}

	public void setButtonCaptions(String newButtonCaption, String editButtonCaption, String deleteButtonCaption) {
		newButton.setCaption(Strings.isNullOrEmpty(newButtonCaption) ? I18N.TABLECONTROL_ACTION_NEW.msg() : newButtonCaption);
		editButton.setCaption(Strings.isNullOrEmpty(editButtonCaption) ? I18N.TABLECONTROL_ACTION_EDIT.msg() : editButtonCaption);
		deleteButton.setCaption(Strings.isNullOrEmpty(deleteButtonCaption) ? I18N.TABLECONTROL_ACTION_DELETE.msg() : deleteButtonCaption);
	}

	public void setNewCallback(NewCallback newCallback) {
		this.newCallback = newCallback;
		newButton.setVisible(newCallback != null);
	}

	public void setEditCallback(EditCallback<T> editCallback) {
		this.editCallback = editCallback;
		editButton.setVisible(editCallback != null);
	}

	public void setDeleteCallback(DeleteCallback<T> deleteCallback) {
		this.deleteCallback = deleteCallback;
	}


	/**
	 * With using of this handler it is necessary that the Generic {@link T}
	 * extends {@link DeletableEntity}. In additional with assigning this
	 * handler a checkbox appears to show and hide the deleted entries.
	 * Therefore a filter will automatically set and unset to the table.
	 *
	 * @param undeleteCallback
	 */
	public void setUndeleteCallback(UndeleteCallback<T> undeleteCallback) {
		this.undeleteCallback = undeleteCallback;
		showDeletedCheckBox.setVisible(undeleteCallback != null);
		refreshTableFilter();
	}

	public void setMoveHandler(MoveHandler<T> moveHandler) {
		this.moveHandler = moveHandler;
		upButton.setVisible(moveHandler != null);
		downButton.setVisible(moveHandler != null);
		table.setSortableByMoveHandler(moveHandler != null);
	}

	public LazyBeanItemComponent<T> getTable() {
		return table;
	}

	public void setMargin(boolean enabled) {
		layout.setMargin(enabled);
	}

	public void addButton(String caption, ItemButtonCallback<T> itemButtonCallback) {
		addButton(caption, itemButtonCallback, null);
	}

	public void addButton(String caption, ItemButtonCallback<T> itemButtonCallback, ShowButtonCallback<T> showButtonCallback) {
		final Button button = new Button(caption, event -> itemButtonCallback.clickedButton(table.getValue()));
		button.setEnabled(false);
		otherItemButtons.put(button, showButtonCallback);
		controlLayout.addComponent(button);
	}

	public void addButton(String caption, ButtonCallback buttonCallback) {
		final Button button = new Button(caption, event -> buttonCallback.clickedButton());
		otherButtons.add(button);
		controlLayout.addComponent(button);
	}

	public void removeAdditionalButtons() {
		otherItemButtons.keySet().forEach(controlLayout::removeComponent);
		otherButtons.forEach(controlLayout::removeComponent);

		otherItemButtons.clear();
		otherButtons.clear();
	}

	public void setTableSizeVisible(boolean visible) {
		tableSize.setVisible(visible);
	}

	public void setCurrenSelectedUser(User currenSelectedUser) {
		this.currenSelectedUser = currenSelectedUser;
		if (currenSelectedUser != null) {
			oneDirectionFilter = (root, query, cb) -> cb.equal(root.get(SourceUserEntity.SOURCE_USER), currenSelectedUser);
		}
	}

	public void onlyDeletedSuggestions(User currenSelectedUser) {
		this.currenSelectedUser = currenSelectedUser;
		if (currenSelectedUser != null) {
			onlyDeletedSuggestionFilter = (root, query, cb) -> cb.equal(root.get(DeletableEntity.DELETED), true);

		}
	}
}