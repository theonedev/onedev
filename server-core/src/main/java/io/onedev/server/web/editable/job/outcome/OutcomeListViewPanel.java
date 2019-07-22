package io.onedev.server.web.editable.job.outcome;

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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.layout.SideFloating;

@SuppressWarnings("serial")
class OutcomeListViewPanel extends Panel {

	private final List<JobOutcome> outcomes = new ArrayList<>();
	
	public OutcomeListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			outcomes.add((JobOutcome) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<JobOutcome, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<JobOutcome, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobOutcome>> cellItem, String componentId, IModel<JobOutcome> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, EditableUtils.getDisplayName(rowModel.getObject().getClass()));
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<JobOutcome, Void>(Model.of("File Patterns")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobOutcome>> cellItem, String componentId, IModel<JobOutcome> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getFilePatterns());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<JobOutcome, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobOutcome>> cellItem, String componentId, IModel<JobOutcome> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, "<i class='fa fa-ellipsis-h'></i>").setEscapeModelStrings(false);
					}
					
				});
				
			}

			@Override
			public String getCssClass() {
				return "ellipsis";
			}
			
		});		
		
		IDataProvider<JobOutcome> dataProvider = new ListDataProvider<JobOutcome>() {

			@Override
			protected List<JobOutcome> getData() {
				return outcomes;
			}

		};
		
		add(new DataTable<JobOutcome, Void>("outcomes", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this));
			}
			
		});
	}

	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, int index) {
			super(id, "columnFrag", OutcomeListViewPanel.this);
			this.index = index;
		}
		
		protected abstract Component newLabel(String componentId);
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new SideFloating(target, SideFloating.Placement.RIGHT) {

						@Override
						protected String getTitle() {
							return EditableUtils.getDisplayName(outcomes.get(index).getClass());
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "job-outcome def-detail"));
						}

						@Override
						protected Component newBody(String id) {
							return BeanContext.view(id, outcomes.get(index));
						}

					};
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
		
	}
}
