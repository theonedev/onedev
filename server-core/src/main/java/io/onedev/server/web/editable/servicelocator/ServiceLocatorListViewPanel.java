package io.onedev.server.web.editable.servicelocator;

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

import com.google.common.collect.Sets;

import io.onedev.server.model.support.administration.jobexecutor.ServiceLocator;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel.Placement;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EmptyValueLabel;

@SuppressWarnings("serial")
class ServiceLocatorListViewPanel extends Panel {

	private final List<ServiceLocator> locators = new ArrayList<>();
	
	public ServiceLocatorListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			locators.add((ServiceLocator) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<ServiceLocator, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("Applicable Services")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						if (rowModel.getObject().getServiceNames() != null) {
							return new Label("label", rowModel.getObject().getServiceNames());
						} else {
							try {
								return new EmptyValueLabel("label", ServiceLocator.class.getDeclaredMethod("getServiceNames"));
							} catch (NoSuchMethodException | SecurityException e) {
								throw new RuntimeException(e);
							}
						}
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("Applicable Images")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						if (rowModel.getObject().getServiceImages() != null) {
							return new Label("label", rowModel.getObject().getServiceImages());
						} else {
							try {
								return new EmptyValueLabel("label", ServiceLocator.class.getDeclaredMethod("getServiceImages"));
							} catch (NoSuchMethodException | SecurityException e) {
								throw new RuntimeException(e);
							}
						}
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("#Node Selector Entries")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getNodeSelector().size());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				if (!rowModel.getObject().getNodeSelector().isEmpty()) {
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
				} else {
					cellItem.add(new Label(componentId));
				}
			}

			@Override
			public String getCssClass() {
				return "ellipsis text-right";
			}
			
		});		
		
		IDataProvider<ServiceLocator> dataProvider = new ListDataProvider<ServiceLocator>() {

			@Override
			protected List<ServiceLocator> getData() {
				return locators;
			}

		};
		
		add(new DataTable<ServiceLocator, Void>("locators", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this));
				add(new NoRecordsBehavior());
			}
			
		});
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, int index) {
			super(id, "columnFrag", ServiceLocatorListViewPanel.this);
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
							return new Label(componentId, "Service Locator");
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "service-locator"));
						}

						@Override
						protected Component newBody(String id) {
							return BeanContext.view(id, locators.get(index), Sets.newHashSet("job"), true);
						}
							
					};
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
	}
	
}
