package io.onedev.server.web.page.admin.issuesetting.fieldspec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
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
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.asset.inputspec.InputSpecCssResourceReference;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChanged;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

@SuppressWarnings("serial")
public class IssueFieldListPage extends IssueSettingPage {

	public IssueFieldListPage(PageParameters params) {
		super(params);
	}

	private DataTable<FieldSpec, Void> fieldsTable;
	
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
		
		List<IColumn<FieldSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<FieldSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<FieldSpec>> cellItem, String componentId, IModel<FieldSpec> rowModel) {
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
		
		columns.add(new AbstractColumn<FieldSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<FieldSpec>> cellItem, String componentId, IModel<FieldSpec> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}
		});		
		
		columns.add(new AbstractColumn<FieldSpec, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<FieldSpec>> cellItem, String componentId, IModel<FieldSpec> rowModel) {
				FieldSpec field = rowModel.getObject();
				cellItem.add(new Label(componentId, EditableUtils.getDisplayName(field.getClass())));
			}
		});		
		
		columns.add(new AbstractColumn<FieldSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<FieldSpec>> cellItem, String componentId, IModel<FieldSpec> rowModel) {
				int fieldIndex = cellItem.findParent(LoopItem.class).getIndex();
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", IssueFieldListPage.this);
				fragment.add(new ModalLink("edit") {
	
					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new FieldEditPanel(id, fieldIndex) {
	
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
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this field?"));
					}
	
					@Override
					public void onClick(AjaxRequestTarget target) {
						getSetting().getFieldSpecs().remove(fieldIndex);
						getSetting().setReconciled(false);
						send(getPage(), Broadcast.BREADTH, new WorkflowChanged(target));
						OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
						target.add(fieldsTable);
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions text-nowrap";
			}

		});		
		
		IDataProvider<FieldSpec> dataProvider = new ListDataProvider<FieldSpec>() {

			@Override
			protected List<FieldSpec> getData() {
				return getSetting().getFieldSpecs();
			}

		};
		
		add(fieldsTable = new DataTable<FieldSpec, Void>("issueFields", columns, dataProvider, Integer.MAX_VALUE));
		fieldsTable.addTopToolbar(new HeadersToolbar<Void>(fieldsTable, null));
		fieldsTable.addBottomToolbar(new NoRecordsToolbar(fieldsTable));
		fieldsTable.add(new NoRecordsBehavior());
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
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new InputSpecCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Issue Custom Fields");
	}

}
