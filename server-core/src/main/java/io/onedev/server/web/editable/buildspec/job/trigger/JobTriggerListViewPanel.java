package io.onedev.server.web.editable.buildspec.job.trigger;

import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class JobTriggerListViewPanel extends Panel {

	private final List<JobTrigger> triggers = new ArrayList<>();
	
	public JobTriggerListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			triggers.add((JobTrigger) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<JobTrigger, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getDescription()));
			}

		});		
		
		columns.add(new AbstractColumn<>(Model.of("#Params")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getParamMatrix().size()));
			}

		});		
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
				int triggerIndex = cellItem.findParent(Item.class).getIndex();
				Fragment fragment = new Fragment(componentId, "showDetailFrag", JobTriggerListViewPanel.this);
				fragment.add(new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

							@Override
							protected Component newTitle(String componentId) {
								String triggerType = EditableUtils.getDisplayName(triggers.get(triggerIndex).getClass());
								return new Label(componentId, "Trigger (type: " + triggerType + ")");
							}

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("class", "job-trigger"));
							}

							@Override
							protected Component newBody(String id) {
								return BeanContext.view(id, triggers.get(triggerIndex));
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
		
		IDataProvider<JobTrigger> dataProvider = new ListDataProvider<JobTrigger>() {

			@Override
			protected List<JobTrigger> getData() {
				return triggers;
			}

		};
		
		add(new DataTable<>("triggers", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this, Model.of(_T("Unspecified"))));
				add(new NoRecordsBehavior());
			}

		});
	}

}
