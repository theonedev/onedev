package io.onedev.server.web.editable.buildspec.param.spec;

import static io.onedev.server.web.translation.Translation._T;

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

import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.web.asset.inputspec.InputSpecCssResourceReference;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;

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
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}
			
		});		
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				ParamSpec param = rowModel.getObject();
				cellItem.add(new Label(componentId, EditableUtils.getDisplayName(param.getClass())));
			}
			
		});		
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				int paramIndex = cellItem.findParent(Item.class).getIndex();
				Fragment fragment = new Fragment(componentId, "showDetailFrag", ParamSpecListViewPanel.this);
				fragment.add(new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

							@Override
							protected Component newTitle(String componentId) {
								String paramType = EditableUtils.getDisplayName(paramSpecs.get(paramIndex).getClass());
								return new Label(componentId, "Parameter Spec (type: " + paramType + ")");
							}

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("class", "param-spec input-spec"));
							}

							@Override
							protected Component newBody(String id) {
								Set<String> excludedProperties = Sets.newHashSet("canBeChangedBy", "nameOfEmptyValue");
								return BeanContext.view(id, paramSpecs.get(paramIndex), excludedProperties, true);
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
				addBottomToolbar(new NoRecordsToolbar(this, Model.of(_T("Unspecified"))));
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
	
}
