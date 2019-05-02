package io.onedev.server.web.page.project.blob.render.renderers.cispec.joboutcome;

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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.ci.job.JobOutcome;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

@SuppressWarnings("serial")
public class OutcomeListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<JobOutcome> outcomes;
	
	public OutcomeListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		outcomes = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			outcomes.add((JobOutcome) each);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new OutcomeEditPanel(id, outcomes, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						modal.close();
						target.add(OutcomeListEditPanel.this);
					}

				};
			}
			
		});
		
		List<IColumn<JobOutcome, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<JobOutcome, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobOutcome>> cellItem, String componentId, IModel<JobOutcome> rowModel) {
				Fragment fragment = new Fragment(componentId, "typeColumnFrag", OutcomeListEditPanel.this);
				ModalLink link = new ModalLink("link") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new OutcomeEditPanel(id, outcomes, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								modal.close();
								target.add(OutcomeListEditPanel.this);
							}

						};
					}
					
				};
				link.add(new Label("label", EditableUtils.getDisplayName(rowModel.getObject().getClass())));
				fragment.add(link);
				
				cellItem.add(fragment);
			}
		});		
		
		columns.add(new AbstractColumn<JobOutcome, Void>(Model.of("File Patterns")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobOutcome>> cellItem, String componentId, IModel<JobOutcome> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getFilePatterns()));
			}
		});		
		
		columns.add(new AbstractColumn<JobOutcome, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobOutcome>> cellItem, String componentId, IModel<JobOutcome> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", OutcomeListEditPanel.this);
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						outcomes.remove(rowModel.getObject());
						target.add(OutcomeListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});		
		
		IDataProvider<JobOutcome> dataProvider = new ListDataProvider<JobOutcome>() {

			@Override
			protected List<JobOutcome> getData() {
				return outcomes;			
			}

		};
		
		DataTable<JobOutcome, Void> dataTable;
		add(dataTable = new DataTable<JobOutcome, Void>("outcomes", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(outcomes, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(outcomes, fromIndex-i, fromIndex-i-1);
				}
				target.add(OutcomeListEditPanel.this);
			}
			
		}.sortable("tbody"));
		
		setOutputMarkupId(true);		
	}

	@Override
	protected String getErrorClass() {
		return null;
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
		for (JobOutcome each: outcomes)
			value.add(each);
		return value;
	}

	@Override
	public ErrorContext getErrorContext(PathElement element) {
		return null;
	}
	
}
