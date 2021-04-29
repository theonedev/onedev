package io.onedev.server.web.editable.buildspec.step;

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

import io.onedev.server.buildspec.step.Step;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;

@SuppressWarnings("serial")
class StepListViewPanel extends Panel {

	private final List<Step> steps = new ArrayList<>();
	
	public StepListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			steps.add((Step) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<Step, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Step, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Step>> cellItem, String componentId, IModel<Step> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}
			
		});		
		
		columns.add(new AbstractColumn<Step, Void>(Model.of("Condition")) {

			@Override
			public void populateItem(Item<ICellPopulator<Step>> cellItem, String componentId, IModel<Step> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getCondition().getDisplayName()));
			}
		});		
		
		columns.add(new AbstractColumn<Step, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Step>> cellItem, String componentId, IModel<Step> rowModel) {
				int stepIndex = cellItem.findParent(Item.class).getIndex();
				Fragment fragment = new Fragment(componentId, "showDetailFrag", StepListViewPanel.this);
				fragment.add(new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

							@Override
							protected Component newTitle(String componentId) {
								String stepType = EditableUtils.getDisplayName(steps.get(stepIndex).getClass());
								return new Label(componentId, "Step (type: " + stepType + ")");
							}

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("class", "step"));
							}

							@Override
							protected Component newBody(String id) {
								return BeanContext.view(id, steps.get(stepIndex));
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
		
		IDataProvider<Step> dataProvider = new ListDataProvider<Step>() {

			@Override
			protected List<Step> getData() {
				return steps;
			}

		};
		
		add(new DataTable<Step, Void>("steps", columns, dataProvider, Integer.MAX_VALUE) {

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
		response.render(CssHeaderItem.forReference(new StepCssResourceReference()));
	}

}
