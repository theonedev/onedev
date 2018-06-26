package io.onedev.server.web.page.project.setting.issueworkflow.fields;

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

import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.layout.SideFloating;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;
import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class IssueFieldsPage extends IssueWorkflowPage {

	private DataTable<InputSpec, Void> fieldsTable;
	
	public IssueFieldsPage(PageParameters params) {
		super(params);
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
					protected IssueWorkflow getWorkflow() {
						return IssueFieldsPage.this.getWorkflow();
					}

				};
			}
			
		});
		
		List<IColumn<InputSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel, rowModel.getObject().getName(), true));
			}
		});		
		
		columns.add(new AbstractColumn<InputSpec, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<InputSpec>> cellItem, String componentId, IModel<InputSpec> rowModel) {
				InputSpec field = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, rowModel, EditableUtils.getDisplayName(field.getClass()), false));
			}
		});		
		
		IDataProvider<InputSpec> dataProvider = new ListDataProvider<InputSpec>() {

			@Override
			protected List<InputSpec> getData() {
				return getWorkflow().getFieldSpecs();
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
						Collections.swap(getWorkflow().getFieldSpecs(), fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(getWorkflow().getFieldSpecs(), fromIndex-i, fromIndex-i-1);
				}
				
				getProject().setIssueWorkflow(getWorkflow());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				target.add(fieldsTable);
			}
			
		}.sortable("tbody").handle(".drag-handle").helperClass("sort-helper"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueFieldsResourceReference()));
	}

	private class ColumnFragment extends Fragment {

		private final int index;
		
		private final String label;
		
		public ColumnFragment(String id, IModel<InputSpec> model, String label, boolean nameColumn) {
			super(id, nameColumn?"nameColumnFrag":"otherColumnFrag", IssueFieldsPage.this, model);
			this.index = getWorkflow().getFieldSpecIndex(getField().getName());
			Preconditions.checkState(this.index != -1);
			this.label = label;
		}
		
		private InputSpec getField() {
			return (InputSpec) getDefaultModelObject();
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
							return getField().getName() + " (type: " + EditableUtils.getDisplayName(getField().getClass()) + ")";
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "field-view"));
						}

						@Override
						protected Component newBody(String id) {
							SideFloating sideFloating = this;
							Fragment fragment = new Fragment(id, "viewFieldFrag", IssueFieldsPage.this);
							getField().setupShowConditionsForDisplay();
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
										protected IssueWorkflow getWorkflow() {
											return IssueFieldsPage.this.getWorkflow();
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
									getWorkflow().getFieldSpecs().remove(index);
									getWorkflow().onDeleteField(getField().getName());
									getWorkflow().setReconciled(false);
									getProject().setIssueWorkflow(getWorkflow());
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
