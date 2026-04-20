package io.onedev.server.web.editable.drawcardbeanlist;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

/**
 * Reusable list editor that renders a sortable summary table with add/edit/delete actions
 * driven by a modal {@link DrawCardBeanItemEditPanel}. Subclasses provide the type-specific
 * data columns, the modal editor for an individual entry, and the tooltip used on the
 * "add new" action.
 */
public abstract class DrawCardBeanListEditPanel<T extends Serializable> extends PropertyEditor<List<Serializable>> {

	private static final long serialVersionUID = 1L;

	protected final List<T> items;

	@SuppressWarnings("unchecked")
	public DrawCardBeanListEditPanel(String id, PropertyDescriptor descriptor, IModel<List<Serializable>> model) {
		super(id, descriptor, model);

		items = new ArrayList<>();
		for (Serializable each : model.getObject())
			items.add((T) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		ModalLink addNewLink = new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return newEditPanel(id, items, -1, newCallback(modal));
			}

		};
		addNewLink.add(AttributeModifier.replace("data-tippy-content", getAddTooltip()));
		add(addNewLink);

		List<IColumn<T, Void>> columns = new ArrayList<>();
		columns.add(newGripColumn());
		columns.addAll(getDataColumns());
		columns.add(newActionColumn());

		IDataProvider<T> dataProvider = new ListDataProvider<T>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<T> getData() {
				return items;
			}

		};

		DataTable<T, Void> dataTable = new DataTable<>("items", columns, dataProvider, Integer.MAX_VALUE);
		add(dataTable);
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable, Model.of(_T("Unspecified"))));
		dataTable.add(new NoRecordsBehavior());

		dataTable.add(new SortBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				CollectionUtils.move(items, from.getItemIndex(), to.getItemIndex());
				onPropertyUpdating(target);
				target.add(DrawCardBeanListEditPanel.this);
			}

		}.sortable("tbody"));
	}

	private IColumn<T, Void> newGripColumn() {
		return new AbstractColumn<T, Void>(Model.of("")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
				cellItem.add(new SpriteImage(componentId, "grip") {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("svg");
						tag.put("class", "icon drag-indicator");
					}

				});
			}

			@Override
			public String getCssClass() {
				return "minimum actions";
			}

		};
	}

	private IColumn<T, Void> newActionColumn() {
		return new AbstractColumn<T, Void>(Model.of("")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", DrawCardBeanListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						int index = cellItem.findParent(Item.class).getIndex();
						return newEditPanel(id, items, index, newCallback(modal));
					}

				});
				fragment.add(new AjaxLink<Void>("delete") {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						items.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(DrawCardBeanListEditPanel.this);
					}

				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "minimum actions";
			}

		};
	}

	private DrawCardBeanItemEditPanel.EditCallback newCallback(ModalPanel modal) {
		return new DrawCardBeanItemEditPanel.EditCallback() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSave(AjaxRequestTarget target) {
				markFormDirty(target);
				modal.close();
				onPropertyUpdating(target);
				target.add(DrawCardBeanListEditPanel.this);
			}

			@Override
			public void onCancel(AjaxRequestTarget target) {
				modal.close();
			}

		};
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PropertyUpdating) {
			event.stop();
			onPropertyUpdating(((PropertyUpdating) event.getPayload()).getHandler());
		}
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		return new ArrayList<>(items);
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

	protected abstract List<IColumn<T, Void>> getDataColumns();

	protected abstract DrawCardBeanItemEditPanel<T> newEditPanel(String id, List<T> items, int index,
			DrawCardBeanItemEditPanel.EditCallback callback);

	/**
	 * @return english tooltip text for the "add new" button; will be passed through translation.
	 */
	protected abstract String getAddTooltip();

}
