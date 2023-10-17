package io.onedev.server.web.page.admin.issuesetting.issuetemplate;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.IssueTemplate;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
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
import org.unbescape.html.HtmlEscape;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class IssueTemplateListPage extends IssueSettingPage {

	public IssueTemplateListPage(PageParameters params) {
		super(params);
	}

	private DataTable<IssueTemplate, Void> templatesTable;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

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
		
		columns.add(new AbstractColumn<IssueTemplate, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueTemplate>> cellItem, String componentId, IModel<IssueTemplate> rowModel) {
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
		
		columns.add(new AbstractColumn<IssueTemplate, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueTemplate>> cellItem, String componentId, IModel<IssueTemplate> rowModel) {
				int templateIndex = cellItem.findParent(LoopItem.class).getIndex();
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", IssueTemplateListPage.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
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
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions text-nowrap";
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
				CollectionUtils.move(getSetting().getIssueTemplates(), from.getItemIndex(), to.getItemIndex());
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
