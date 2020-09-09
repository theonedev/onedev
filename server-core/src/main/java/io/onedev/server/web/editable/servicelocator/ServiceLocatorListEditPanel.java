package io.onedev.server.web.editable.servicelocator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.model.support.administration.jobexecutor.ServiceLocator;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.EmptyValueLabel;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

@SuppressWarnings("serial")
class ServiceLocatorListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<ServiceLocator> locators;
	
	public ServiceLocatorListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		locators = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			locators.add((ServiceLocator) each);
		}
	}
	
	private BuildSpec getBuildSpec() {
		BuildSpecAware buildSpecAware = findParent(BuildSpecAware.class);
		if (buildSpecAware != null)
			return buildSpecAware.getBuildSpec();
		else
			return null;
	}

	private Job getJob() {
		JobAware jobAware = findParent(JobAware.class);
		if (jobAware != null)
			return jobAware.getJob();
		else
			return null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new ServiceLocatorEditPanel(id, locators, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						markFormDirty(target);
						modal.close();
						onPropertyUpdating(target);
						target.add(ServiceLocatorListEditPanel.this);
					}

					@Override
					public BuildSpec getBuildSpec() {
						return ServiceLocatorListEditPanel.this.getBuildSpec();
					}

					@Override
					public Job getJob() {
						return ServiceLocatorListEditPanel.this.getJob();
					}

				};
			}
			
		});
		
		List<IColumn<ServiceLocator, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				cellItem.add(new SpriteImage(componentId, "grip") {

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
			
		});		
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("Applicable Names")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				if (rowModel.getObject().getServiceNames() != null) {
					cellItem.add(new Label(componentId, rowModel.getObject().getServiceNames()));
				} else {
					try {
						cellItem.add(new EmptyValueLabel(componentId, ServiceLocator.class.getDeclaredMethod("getServiceNames")));
					} catch (NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});		
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("Applicable Images")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				if (rowModel.getObject().getServiceImages() != null) {
					cellItem.add(new Label(componentId, rowModel.getObject().getServiceImages()));
				} else {
					try {
						cellItem.add(new EmptyValueLabel(componentId, ServiceLocator.class.getDeclaredMethod("getServiceImages")));
					} catch (NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});	
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("#Node Selector Entries")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getNodeSelector().size()));
			}
			
		});		
		
		columns.add(new AbstractColumn<ServiceLocator, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ServiceLocator>> cellItem, String componentId, IModel<ServiceLocator> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", ServiceLocatorListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new ServiceLocatorEditPanel(id, locators, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								markFormDirty(target);
								modal.close();
								onPropertyUpdating(target);
								target.add(ServiceLocatorListEditPanel.this);
							}

							@Override
							public BuildSpec getBuildSpec() {
								return ServiceLocatorListEditPanel.this.getBuildSpec();
							}

							@Override
							public Job getJob() {
								return ServiceLocatorListEditPanel.this.getJob();
							}

						};
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						locators.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(ServiceLocatorListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});		
		
		IDataProvider<ServiceLocator> dataProvider = new ListDataProvider<ServiceLocator>() {

			@Override
			protected List<ServiceLocator> getData() {
				return locators;			
			}

		};
		
		DataTable<ServiceLocator, Void> dataTable;
		add(dataTable = new DataTable<ServiceLocator, Void>("locators", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.add(new NoRecordsBehavior());
		
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(locators, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(locators, fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
				target.add(ServiceLocatorListEditPanel.this);
			}
			
		}.sortable("tbody"));
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof PropertyUpdating) {
			event.stop();
			onPropertyUpdating(((PropertyUpdating)event.getPayload()).getHandler());
		}		
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> value = new ArrayList<>();
		for (ServiceLocator each: locators)
			value.add(each);
		return value;
	}

}
