package io.onedev.server.web.editable.buildspec.step;

import static io.onedev.server.web.component.floating.AlignPlacement.bottom;
import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.SerializationUtils;
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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.ParamSpecAware;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.typeselect.TypeSelectPanel;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import io.onedev.server.web.util.TextUtils;

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
	protected void onBeforeRender() {
		super.onBeforeRender();
		replace(new AddStepLink("addNew", null, steps.size()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AddStepLink("addNew", null, steps.size()));
		
		List<IColumn<Step, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of("")) {

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
		
		columns.add(new AbstractColumn<>(Model.of(_T("Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Step>> cellItem, String componentId, IModel<Step> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}

		});		
		
		columns.add(new AbstractColumn<>(Model.of(_T("Condition"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Step>> cellItem, String componentId, IModel<Step> rowModel) {
				var condition = rowModel.getObject().getCondition();
				if (condition != null) 
					cellItem.add(new Label(componentId, _T(TextUtils.getDisplayValue(condition))));
				else
					cellItem.add(new Label(componentId, "<i>" + HtmlEscape.escapeHtml5(_T("Unspecified")) + "</i>").setEscapeModelStrings(false));
			}

		});		
		
		columns.add(new AbstractColumn<>(Model.of("")) {
			
			@Override
			public void populateItem(Item<ICellPopulator<Step>> cellItem, String componentId, IModel<Step> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", StepListEditPanel.this);
				Item<?> row = cellItem.findParent(Item.class);
				var index = row.getIndex();
				row.setOutputMarkupId(true);
				fragment.add(new AjaxLink<Void>("edit") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Step step = steps.get(index);
						new StepEditModalPanel(target, step) {

							@Override
							protected void onSave(AjaxRequestTarget target, Step step) {
								steps.set(index, step);
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
				fragment.add(new MenuLink("actions") {

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						var menuItems = new ArrayList<MenuItem>();
						menuItems.add(new MenuItem() {
							@Override
							public String getLabel() {
								return _T("Add before");
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AddStepLink(id, row, index) {
									@Override
									public void onClick(AjaxRequestTarget target) {
										super.onClick(target);
										dropdown.close();
									}
								};
							}
						});
						menuItems.add(new MenuItem() {
							@Override
							public String getLabel() {
								return _T("Add after");
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AddStepLink(id, row, index+1) {
									@Override
									public void onClick(AjaxRequestTarget target) {
										super.onClick(target);
										dropdown.close();
									}
								};
							}
						});
						menuItems.add(new MenuItem() {
							@Override
							public String getLabel() {
								return _T("Copy");
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									public void onClick(AjaxRequestTarget target) {
										dropdown.close();
										Step step = SerializationUtils.clone(steps.get(index));
										new StepEditModalPanel(target, step) {

											@Override
											protected void onSave(AjaxRequestTarget target, Step step) {
												steps.add(step);
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
						menuItems.add(new MenuItem() {
							@Override
							public String getLabel() {
								return _T("Delete");
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									public void onClick(AjaxRequestTarget target) {
										dropdown.close();
										markFormDirty(target);
										steps.remove(rowModel.getObject());
										onPropertyUpdating(target);
										target.add(StepListEditPanel.this);
									}

								};
							}
							
						});
						return menuItems;
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "minimum actions";
			}

		});		
		
		IDataProvider<Step> dataProvider = new ListDataProvider<>() {

			@Override
			protected List<Step> getData() {
				return steps;
			}

		};
		
		DataTable<Step, Void> dataTable;
		add(dataTable = new DataTable<>("steps", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable, Model.of(_T("Unspecified"))));
		dataTable.add(new NoRecordsBehavior());
		
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				CollectionUtils.move(steps, from.getItemIndex(), to.getItemIndex());
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
	
	private class AddStepLink extends DropdownLink {
		
		private final int index;
		
		AddStepLink(String componentId, @Nullable Component alignTarget, int index) {
			super(componentId, alignTarget, bottom(0), true, true);
			this.index = index;
		} 

		@Override
		protected Component newContent(String id, FloatingPanel dropdown) {
			return new TypeSelectPanel<Step>(id) {

				@Override
				protected void onSelect(AjaxRequestTarget target, Class<? extends Step> type) {
					dropdown.close();

					Step step;
					try {
						step = type.getDeclaredConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}

					new StepEditModalPanel(target, step) {

						@Override
						protected void onSave(AjaxRequestTarget target, Step step) {
							steps.add(index, step);
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

	}
	
}
