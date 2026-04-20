package io.onedev.server.web.editable.drawcardbeanlist;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.draw.DrawCardPanel;
import io.onedev.server.web.component.draw.DrawPanel;
import io.onedev.server.web.editable.BeanContext;

/**
 * Reusable read-only listing that mirrors {@link DrawCardBeanListEditPanel}: a summary table
 * plus a trailing column whose link opens a {@link DrawCardPanel} side draw with the item's
 * details. Subclasses provide the type-specific data columns and the title shown in the draw
 * card. They may override {@link #newDetailBody(String, Serializable)} to customise what is
 * rendered inside the draw card.
 */
public abstract class DrawCardBeanListViewPanel<T extends Serializable> extends Panel {

	private static final long serialVersionUID = 1L;

	protected final List<T> items = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public DrawCardBeanListViewPanel(String id, List<Serializable> elements) {
		super(id);
		for (Serializable each : elements)
			items.add((T) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<IColumn<T, Void>> columns = new ArrayList<>(getDataColumns());
		columns.add(newDetailColumn());

		IDataProvider<T> dataProvider = new ListDataProvider<T>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<T> getData() {
				return items;
			}

		};

		add(new DataTable<T, Void>("items", columns, dataProvider, Integer.MAX_VALUE) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this, Model.of(_T("Unspecified"))));
				add(new NoRecordsBehavior());
			}

		});
	}

	private IColumn<T, Void> newDetailColumn() {
		return new AbstractColumn<T, Void>(Model.of("")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
				int itemIndex = cellItem.findParent(Item.class).getIndex();
				Fragment fragment = new Fragment(componentId, "showDetailFrag", DrawCardBeanListViewPanel.this);
				fragment.add(new AjaxLink<Void>("link") {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						new DrawCardPanel(target, DrawPanel.Placement.RIGHT) {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								StringBuilder cssClass = new StringBuilder("draw-card-bean-detail");
								String extra = additionalDetailCssClass();
								if (extra != null && extra.length() != 0)
									cssClass.append(' ').append(extra);
								add(AttributeAppender.append("class", cssClass.toString()));
							}

							@Override
							protected Component newTitle(String componentId) {
								return new Label(componentId, getDetailTitle(items.get(itemIndex)));
							}

							@Override
							protected Component newBody(String id) {
								return newDetailBody(id, items.get(itemIndex));
							}

						};
					}

				});

				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "ellipsis text-right";
			}

		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DrawCardBeanListCssResourceReference()));
	}

	protected abstract List<IColumn<T, Void>> getDataColumns();

	protected abstract String getDetailTitle(T item);

	/**
	 * Component rendered inside the detail draw card body. Default uses
	 * {@link BeanContext#view(String, Serializable)}.
	 */
	protected Component newDetailBody(String id, T item) {
		return BeanContext.view(id, item);
	}

	/**
	 * Optional extra CSS class names to append to the detail {@link DrawCardPanel}. Useful when
	 * the bean type has its own legacy floating styles (e.g. {@code .floating.foo ...}).
	 * Default returns {@code null}.
	 */
	protected String additionalDetailCssClass() {
		return null;
	}

}
