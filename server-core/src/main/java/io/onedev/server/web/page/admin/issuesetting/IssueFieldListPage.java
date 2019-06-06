package io.onedev.server.web.page.admin.issuesetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.ajaxlistener.ConfirmListener;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.layout.SideFloating;
import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class IssueFieldListPage extends GlobalIssueSettingPage {

	public IssueFieldListPage(PageParameters params) {
		super(params);
	}

	private DataTable<InputSpec, Void> fieldsTable;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new FieldEditPanel(id, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(fieldsTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected GlobalIssueSetting getSetting() {
						return IssueFieldListPage.this.getSetting();
					}

				};
			}
			
		});
		
		List<IColumn<InputSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel, true) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getName());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				InputSpec field = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, rowModel, false) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, EditableUtils.getDisplayName(field.getClass()));
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Prompt upon Issue Open")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel, false) {

					@Override
					protected Component newLabel(String componentId) {
						InputSpec field = rowModel.getObject();
						return new Label(componentId, StringUtils.describe(getSetting().getDefaultPromptFieldsUponIssueOpen().contains(field.getName())));
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Display in Issue List")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel, false) {

					@Override
					protected Component newLabel(String componentId) {
						InputSpec field = rowModel.getObject();
						return new Label(componentId, StringUtils.describe(getSetting().getDefaultListFields().contains(field.getName())));
					}
					
				});
			}

			@Override
			public String getCssClass() {
				return "display-in-issue-list";
			}
			
		});		
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel, false) {

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
		
		IDataProvider<InputSpec> dataProvider = new ListDataProvider<InputSpec>() {

			@Override
			protected List<InputSpec> getData() {
				return getSetting().getFieldSpecs();
			}

		};
		
		add(fieldsTable = new DataTable<InputSpec, Void>("issueFields", columns, dataProvider, Integer.MAX_VALUE));
		fieldsTable.addTopToolbar(new HeadersToolbar<Void>(fieldsTable, null));
		fieldsTable.addBottomToolbar(new NoRecordsToolbar(fieldsTable));
		fieldsTable.setOutputMarkupId(true);
		
		fieldsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(getSetting().getFieldSpecs(), fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(getSetting().getFieldSpecs(), fromIndex-i, fromIndex-i-1);
				}
				
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
				target.add(fieldsTable);
			}
			
		}.sortable("tbody"));
	}
	
	private int getFieldSpecIndex(String fieldName) {
		for (int i=0; i<getSetting().getFieldSpecs().size(); i++) {
			if (getSetting().getFieldSpecs().get(i).getName().equals(fieldName))
				return i;
		}
		return -1;
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, IModel<InputSpec> model, boolean nameColumn) {
			super(id, nameColumn?"nameColumnFrag":"otherColumnFrag", IssueFieldListPage.this, model);
			this.index = getFieldSpecIndex(getField().getName());
			Preconditions.checkState(this.index != -1);
		}
		
		private InputSpec getField() {
			return (InputSpec) getDefaultModelObject();
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
							return getField().getName() + " (type: " + EditableUtils.getDisplayName(getField().getClass()) + ")";
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "field-spec def-detail"));
						}

						@Override
						protected Component newBody(String id) {
							SideFloating sideFloating = this;
							Fragment fragment = new Fragment(id, "viewFieldFrag", IssueFieldListPage.this);
							fragment.add(BeanContext.view("viewer1", getField(), Sets.newHashSet("name"), true));
							FieldBean bean = new FieldBean();
							bean.setPromptUponIssueOpen(getSetting().getDefaultPromptFieldsUponIssueOpen().contains(getField().getName()));
							bean.setDisplayInIssueList(getSetting().getDefaultListFields().contains(getField().getName()));
							fragment.add(BeanContext.view("viewer2", bean, Sets.newHashSet("field"), true));
							fragment.add(new ModalLink("edit") {

								@Override
								protected Component newContent(String id, ModalPanel modal) {
									sideFloating.close();
									return new FieldEditPanel(id, index) {

										@Override
										protected void onSave(AjaxRequestTarget target) {
											target.add(fieldsTable);
											modal.close();
										}

										@Override
										protected void onCancel(AjaxRequestTarget target) {
											modal.close();
										}

										@Override
										protected GlobalIssueSetting getSetting() {
											return IssueFieldListPage.this.getSetting();
										}

									};
								}
								
							});
							fragment.add(new AjaxLink<Void>("delete") {

								@Override
								protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
									super.updateAjaxAttributes(attributes);
									attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this field?"));
								}

								@Override
								public void onClick(AjaxRequestTarget target) {
									getSetting().getFieldSpecs().remove(index);
									getSetting().getDefaultPromptFieldsUponIssueOpen().remove(getField().getName());
									getSetting().getDefaultListFields().remove(getField().getName());
									getSetting().onDeleteField(getField().getName());
									getSetting().setReconciled(false);
									OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
									target.add(fieldsTable);
									close();
								}
								
							});
							
							fragment.setOutputMarkupId(true);
							
							return fragment;
						}

					};
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
		
	}
	
}
