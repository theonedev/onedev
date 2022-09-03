package io.onedev.server.web.editable.buildspec.step;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.ParamSpecAware;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.typeselect.TypeSelectPanel;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

@SuppressWarnings("serial")
class StepListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<Step> steps;
	
	public StepListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		steps = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			steps.add((Step) each);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DropdownLink("addNew", false, AlignPlacement.bottom(0), true) {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new TypeSelectPanel<Step>(id) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends Step> type) {
						dropdown.close();
						
						Step step;
						try {
							step = type.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
						
						new StepEditPanel(target, step) {

							@Override
							protected void onSave(AjaxRequestTarget target, Step bean) {
								steps.add(bean);
								markFormDirty(target);
								close();
								onPropertyUpdating(target);
								target.add(StepListEditPanel.this);
							}

							@Override
							public BuildSpec getBuildSpec() {
								return StepListEditPanel.this.getBuildSpec();
							}

							@Override
							public List<ParamSpec> getParamSpecs() {
								return StepListEditPanel.this.getParamSpecs();
							}
							
						};
					}
					
				};
			}
			
		});
		
		List<IColumn<Step, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Step, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Step>> cellItem, String componentId, IModel<Step> rowModel) {
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
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", StepListEditPanel.this);
				fragment.add(new AjaxLink<Void>("edit") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Step step = steps.get(cellItem.findParent(Item.class).getIndex());
						new StepEditPanel(target, step) {

							@Override
							protected void onSave(AjaxRequestTarget target, Step bean) {
								markFormDirty(target);
								close();
								onPropertyUpdating(target);
								target.add(StepListEditPanel.this);
							}

							@Override
							public BuildSpec getBuildSpec() {
								return StepListEditPanel.this.getBuildSpec();
							}

							@Override
							public List<ParamSpec> getParamSpecs() {
								return StepListEditPanel.this.getParamSpecs();
							}
							
						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this step?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						steps.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(StepListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "minimum actions";
			}
			
		});		
		
		IDataProvider<Step> dataProvider = new ListDataProvider<Step>() {

			@Override
			protected List<Step> getData() {
				return steps;			
			}

		};
		
		DataTable<Step, Void> dataTable;
		add(dataTable = new DataTable<Step, Void>("steps", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable, Model.of("Not defined")));
		dataTable.add(new NoRecordsBehavior());
		
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(steps, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(steps, fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
				target.add(StepListEditPanel.this);
			}
			
		}.sortable("tbody"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new StepCssResourceReference()));
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
		for (Step each: steps)
			value.add(each);
		return value;
	}

	private BuildSpec getBuildSpec() {
		BuildSpecAware buildSpecAware = findParent(BuildSpecAware.class);
		if (buildSpecAware != null)
			return buildSpecAware.getBuildSpec();
		else
			return null;
	}

	private List<ParamSpec> getParamSpecs() {
		ParamSpecAware paramSpecAware = findParent(ParamSpecAware.class);
		if (paramSpecAware != null)
			return paramSpecAware.getParamSpecs();
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}
	
}
