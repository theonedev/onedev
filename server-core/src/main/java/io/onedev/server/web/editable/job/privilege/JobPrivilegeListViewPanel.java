package io.onedev.server.web.editable.job.privilege;

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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel.Placement;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
class JobPrivilegeListViewPanel extends Panel {

	private final List<JobPrivilege> privileges = new ArrayList<>();
	
	public JobPrivilegeListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			privileges.add((JobPrivilege) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<JobPrivilege, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<JobPrivilege, Void>(Model.of("Job Names")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobPrivilege>> cellItem, String componentId, IModel<JobPrivilege> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getJobNames());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<JobPrivilege, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobPrivilege>> cellItem, String componentId, IModel<JobPrivilege> rowModel) {
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
				return "ellipsis text-right";
			}
			
		});		
		
		IDataProvider<JobPrivilege> dataProvider = new ListDataProvider<JobPrivilege>() {

			@Override
			protected List<JobPrivilege> getData() {
				return privileges;
			}

		};
		
		add(new DataTable<JobPrivilege, Void>("privileges", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this, Model.of("Not defined")));
				add(new NoRecordsBehavior());
			}
			
		});
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, int index) {
			super(id, "columnFrag", JobPrivilegeListViewPanel.this);
			this.index = index;
		}
		
		protected abstract Component newLabel(String componentId);
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new OffCanvasCardPanel(target, Placement.RIGHT, null) {

						@Override
						protected Component newTitle(String componentId) {
							return new Label(componentId, "Job Privilege");
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "job-privilege"));
						}

						@Override
						protected Component newBody(String id) {
							return BeanContext.view(id, privileges.get(index));
						}
							
					};
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
	}
	
}
