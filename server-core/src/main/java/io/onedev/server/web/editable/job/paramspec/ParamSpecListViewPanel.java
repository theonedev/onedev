package io.onedev.server.web.editable.job.paramspec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.apache.wicket.markup.ComponentTag;
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

import com.google.common.collect.Sets;

import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.web.asset.inputspec.InputSpecCssResourceReference;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;

@SuppressWarnings("serial")
class ParamSpecListViewPanel extends Panel {

	private final List<ParamSpec> paramSpecs = new ArrayList<>();
	
	public ParamSpecListViewPanel(String id, Class<?> elementClass, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			paramSpecs.add((ParamSpec) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<ParamSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getName());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				ParamSpec param = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, EditableUtils.getDisplayName(param.getClass()));
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new SpriteImage(componentId, "ellipsis") {

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								tag.setName("svg");
								tag.put("class", "icon");
							}
							
						};
					}
					
				});
				
			}

			@Override
			public String getCssClass() {
				return "text-right ellipsis";
			}
			
		});		
		
		IDataProvider<ParamSpec> dataProvider = new ListDataProvider<ParamSpec>() {

			@Override
			protected List<ParamSpec> getData() {
				return paramSpecs;
			}

		};
		
		add(new DataTable<ParamSpec, Void>("paramSpecs", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this, Model.of("Not defined")));
				add(new NoRecordsBehavior());
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new InputSpecCssResourceReference()));
		response.render(CssHeaderItem.forReference(new ParamSpecCssResourceReference()));
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, int index) {
			super(id, "columnFrag", ParamSpecListViewPanel.this);
			this.index = index;
		}
		
		protected abstract Component newLabel(String componentId);
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

						@Override
						protected Component newTitle(String componentId) {
							ParamSpec param = paramSpecs.get(index);
							return new Label(componentId, "Parameter Spec (type: " + EditableUtils.getDisplayName(param.getClass()) + ")");
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "param-spec input-spec"));
						}

						@Override
						protected Component newBody(String id) {
							Set<String> excludedProperties = Sets.newHashSet("canBeChangedBy", "nameOfEmptyValue");
							return BeanContext.view(id, paramSpecs.get(index), excludedProperties, true);
						}

					};
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
		
	}
}
