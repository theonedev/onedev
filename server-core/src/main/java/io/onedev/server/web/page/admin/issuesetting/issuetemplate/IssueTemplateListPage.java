package io.onedev.server.web.page.admin.issuesetting.issuetemplate;

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
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.unbescape.html.HtmlEscape;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.IssueTemplate;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

@SuppressWarnings("serial")
public class IssueTemplateListPage extends IssueSettingPage {

	private static final int MAX_DESCRIPTION_LEN = 200;
	
	public IssueTemplateListPage(PageParameters params) {
		super(params);
	}

	private DataTable<IssueTemplate, Void> templatesTable;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new IssueTemplateEditPanel(id, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(templatesTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected GlobalIssueSetting getSetting() {
						return IssueTemplateListPage.this.getSetting();
					}

				};
			}
			
		});
		
		List<IColumn<IssueTemplate, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<IssueTemplate, Void>(Model.of("Applicable Issues")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueTemplate>> cellItem, String componentId, IModel<IssueTemplate> rowModel) {
				String label;
				IssueTemplate template = rowModel.getObject();
				if (template.getIssueQuery() != null)
					label = HtmlEscape.escapeHtml5(template.getIssueQuery());
				else
					label = "<i>All</i>";
				cellItem.add(new Label(componentId, label).setEscapeModelStrings(false));
			}
		});		
		
		columns.add(new AbstractColumn<IssueTemplate, Void>(Model.of("Description Template")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueTemplate>> cellItem, String componentId, IModel<IssueTemplate> rowModel) {
				cellItem.add(new Label(componentId, StringUtils.abbreviate(rowModel.getObject().getIssueDescription(), MAX_DESCRIPTION_LEN)));
			}
			
		});		
		
		columns.add(new AbstractColumn<IssueTemplate, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueTemplate>> cellItem, String componentId, IModel<IssueTemplate> rowModel) {
				Fragment fragment = new Fragment(componentId, "showDetailFrag", IssueTemplateListPage.this);
				fragment.add(new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

							@Override
							protected Component newTitle(String componentId) {
								return new Label(componentId, "Description Template");
							}

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("class", "issue-template"));
							}

							@Override
							protected Component newBody(String componentId) {
								return BeanContext.view(componentId, rowModel.getObject());
							}

							@Override
							protected Component newFooter(String componentId) {
								int templateIndex = cellItem.findParent(LoopItem.class).getIndex();
								Fragment fragment = new Fragment(componentId, "templateActionsFrag", IssueTemplateListPage.this);
								fragment.add(new ModalLink("edit") {

									@Override
									protected String getModalCssClass() {
										return "modal-lg";
									}

									@Override
									protected Component newContent(String id, ModalPanel modal) {
										close();
										return new IssueTemplateEditPanel(id, templateIndex) {

											@Override
											protected void onSave(AjaxRequestTarget target) {
												target.add(templatesTable);
												modal.close();
											}

											@Override
											protected void onCancel(AjaxRequestTarget target) {
												modal.close();
											}

											@Override
											protected GlobalIssueSetting getSetting() {
												return IssueTemplateListPage.this.getSetting();
											}

										};
									}
									
								});
								fragment.add(new AjaxLink<Void>("delete") {

									@Override
									protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
										super.updateAjaxAttributes(attributes);
										attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this template?"));
									}

									@Override
									public void onClick(AjaxRequestTarget target) {
										getSetting().getIssueTemplates().remove(templateIndex);
										OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
										target.add(templatesTable);
										close();
									}
									
								});
								
								return fragment;								
							}

						};		
					}
					
				});				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "text-right";
			}
		});		
		
		
		IDataProvider<IssueTemplate> dataProvider = new ListDataProvider<IssueTemplate>() {

			@Override
			protected List<IssueTemplate> getData() {
				return getSetting().getIssueTemplates();
			}

		};
		
		add(templatesTable = new DataTable<IssueTemplate, Void>("issueTemplates", columns, dataProvider, Integer.MAX_VALUE));
		templatesTable.addTopToolbar(new HeadersToolbar<Void>(templatesTable, null));
		templatesTable.addBottomToolbar(new NoRecordsToolbar(templatesTable));
		templatesTable.add(new NoRecordsBehavior());
		templatesTable.setOutputMarkupId(true);
		
		templatesTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(getSetting().getIssueTemplates(), fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(getSetting().getIssueTemplates(), fromIndex-i, fromIndex-i-1);
				}
				
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
				target.add(templatesTable);
			}
			
		}.sortable("tbody"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueTemplateCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Issue Description Templates</span>").setEscapeModelStrings(false);
	}
	
}
