package io.onedev.server.web.page.project.setting.issuecustomfields;

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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.input.Input;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.SideFloating;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;
import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class IssueCustomFieldsPage extends ProjectSettingPage {

	private final List<Input> fields;
	
	private DataTable<Input, Void> fieldsTable;
	
	public IssueCustomFieldsPage(PageParameters params) {
		super(params);
		
		fields = getProject().getIssueWorkflow().getFieldSpecs();
	}

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
					protected List<Input> getFields() {
						return fields;
					}

				};
			}
			
		});
		
		List<IColumn<Input, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Input, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Input>> cellItem, String componentId, IModel<Input> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel, rowModel.getObject().getName(), true));
			}
		});		
		
		columns.add(new AbstractColumn<Input, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<Input>> cellItem, String componentId, IModel<Input> rowModel) {
				Input field = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, rowModel, EditableUtils.getName(field.getClass()), false));
			}
		});		
		
		columns.add(new AbstractColumn<Input, Void>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<Input>> cellItem, String componentId, IModel<Input> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel, rowModel.getObject().getDescription(), false));
			}
			
		});		
		
		IDataProvider<Input> dataProvider = new ListDataProvider<Input>() {

			@Override
			protected List<Input> getData() {
				return fields;
			}

		};
		
		add(fieldsTable = new DataTable<Input, Void>("issueCustomFields", columns, dataProvider, Integer.MAX_VALUE));
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
						Collections.swap(fields, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(fields, fromIndex-i, fromIndex-i-1);
				}
				
				getProject().getIssueWorkflow().setFieldSpecs(fields);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				target.add(fieldsTable);
			}
			
		}.sortable("tbody").handle(".handle").helperClass("sort-helper"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueCustomFieldsResourceReference()));
	}

	public int getFieldIndex(String fieldName) {
		int index = 0;
		for (Input field: fields) {
			if (field.getName().equals(fieldName))
				break;
			index++;
		}
		return index;
	}
	
	private class ColumnFragment extends Fragment {

		private final int index;
		
		private final String label;
		
		public ColumnFragment(String id, IModel<Input> model, String label, boolean nameColumn) {
			super(id, nameColumn?"nameColumnFrag":"otherColumnFrag", IssueCustomFieldsPage.this, model);
			this.index = getFieldIndex(getField().getName());
			this.label = label;
		}
		
		private Input getField() {
			return (Input) getDefaultModelObject();
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new SideFloating(target, SideFloating.Placement.RIGHT) {

						@Override
						protected String getTitle() {
							return getField().getName() + " (type: " + EditableUtils.getName(getField().getClass()) + ")";
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "issue-custom-field"));
						}

						@Override
						protected Component newBody(String id) {
							SideFloating sideFloating = this;
							Fragment fragment = new Fragment(id, "viewFieldFrag", IssueCustomFieldsPage.this);
							fragment.add(BeanContext.viewBean("viewer", getField(), Sets.newHashSet("name")));
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
										protected List<Input> getFields() {
											return fields;
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
									for (Input field: fields) {
										if (field.isUsingInput(getField().getName())) {
											fragment.error("This field is still being used by field \"" + field.getName() + "\"");
											target.add(fragment);
											return;
										}
									}
									
									fields.remove(index);
									getProject().getIssueWorkflow().setFieldSpecs(fields);
									OneDev.getInstance(ProjectManager.class).save(getProject());
									target.add(fieldsTable);
									close();
								}
								
							});
							
							fragment.add(new NotificationPanel("feedback", fragment));
							fragment.setOutputMarkupId(true);
							
							return fragment;
						}

					};
				}
				
			};
			if (label != null)
				link.add(new Label("label", label));
			else
				link.add(new Label("label", "&nbsp;").setEscapeModelStrings(false));
			add(link);
		}
		
	}
	
}
