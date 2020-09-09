package io.onedev.server.web.editable.job.postbuildaction;

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

import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;

@SuppressWarnings("serial")
class PostBuildActionListViewPanel extends Panel {

	private final List<PostBuildAction> actions = new ArrayList<>();
	
	public PostBuildActionListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			actions.add((PostBuildAction) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<PostBuildAction, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<PostBuildAction, Void>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<PostBuildAction>> cellItem, String componentId, IModel<PostBuildAction> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getDescription());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<PostBuildAction, Void>(Model.of("Condition")) {

			@Override
			public void populateItem(Item<ICellPopulator<PostBuildAction>> cellItem, String componentId, IModel<PostBuildAction> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getCondition());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<PostBuildAction, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<PostBuildAction>> cellItem, String componentId, IModel<PostBuildAction> rowModel) {
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
		
		IDataProvider<PostBuildAction> dataProvider = new ListDataProvider<PostBuildAction>() {

			@Override
			protected List<PostBuildAction> getData() {
				return actions;
			}

		};
		
		add(new DataTable<PostBuildAction, Void>("actions", columns, dataProvider, Integer.MAX_VALUE) {

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
			super(id, "columnFrag", PostBuildActionListViewPanel.this);
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
							return new Label(componentId, "Popst Build Action (type: " + EditableUtils.getDisplayName(actions.get(index).getClass()) + ")");
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "post-build-action"));
						}

						@Override
						protected Component newBody(String id) {
							return BeanContext.view(id, actions.get(index));
						}

					};
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
		
	}
}
