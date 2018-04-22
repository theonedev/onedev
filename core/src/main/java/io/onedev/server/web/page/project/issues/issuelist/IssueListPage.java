package io.onedev.server.web.page.project.issues.issuelist;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueListCustomization;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.choice.MultiChoiceEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.WorkflowReconcilePanel;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.util.DateUtils;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class IssueListPage extends ProjectPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final MetaDataKey<IssueListCustomization> CUSTOMIZATION_KEY = 
			new MetaDataKey<IssueListCustomization>() {};
			
	private WebMarkupContainer body;
	
	public IssueListPage(PageParameters params) {
		super(params);
	}

	private IssueListCustomization getCustomization() {
		IssueListCustomization customization = WebSession.get().getMetaData(CUSTOMIZATION_KEY);
		if (customization == null)
			customization = getProject().getIssueListCustomization();
		return customization;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())));
		
		add(new ModalLink("fields") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldsFrag", IssueListPage.this);

				IssueListCustomization customization = getCustomization();
				Form<?> form = new Form<Void>("form") {

					@Override
					protected void onError() {
						super.onError();
						RequestCycle.get().find(AjaxRequestTarget.class).add(this);
					}

				};
				
				PropertyDescriptor propertyDescriptor = new PropertyDescriptor(IssueListCustomization.class, "displayFields"); 
				IModel<List<String>> propertyModel = new IModel<List<String>>() {

					@Override
					public void detach() {
					}

					@SuppressWarnings("unchecked")
					@Override
					public List<String> getObject() {
						return (List<String>) propertyDescriptor.getPropertyValue(customization);
					}

					@Override
					public void setObject(List<String> object) {
						propertyDescriptor.setPropertyValue(customization, object);
					}
					
				};
				form.add(new MultiChoiceEditor("editor", propertyDescriptor, propertyModel));

				form.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						WebSession.get().setMetaData(CUSTOMIZATION_KEY, customization);
						setResponsePage(IssueListPage.this);
					}
					
				});
				
				form.add(new AjaxButton("saveAsDefault") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(getProject()));
					}

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						WebSession.get().setMetaData(CUSTOMIZATION_KEY, customization);
						getProject().setIssueListCustomization(customization);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						setResponsePage(IssueListPage.this);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				form.setOutputMarkupId(true);
				fragment.add(form);
				return fragment;
			}
			
		});
		
		IDataProvider<Issue> dataProvider = new IDataProvider<Issue>() {

			private IssueManager getIssueManager() {
				return OneDev.getInstance(IssueManager.class);
			}
			
			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends Issue> iterator(long first, long count) {
				return getIssueManager().query(getCustomization().getQuery(), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return getIssueManager().count(getCustomization().getQuery().getCriteria());
			}

			@Override
			public IModel<Issue> model(Issue object) {
				Long issueId = object.getId();
				return new LoadableDetachableModel<Issue>() {

					@Override
					protected Issue load() {
						return OneDev.getInstance(IssueManager.class).load(issueId);
					}
					
				};
			}
			
		};
		
		body = new WebMarkupContainer("body");
		
		body.add(new ModalLink("reconcile") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new WorkflowReconcilePanel(id) {
					
					@Override
					protected Project getProject() {
						return IssueListPage.this.getProject();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onCompleted(AjaxRequestTarget target) {
						setResponsePage(IssueListPage.this);
					}
					
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getProject().getIssueWorkflow().isReconciled());
			}

			@Override
			public IModel<?> getBody() {
				if (SecurityUtils.canManage(getProject()))
					return Model.of("reconcile");
				else
					return Model.of("contact project administrator to reconcile");
			}
			
		});
		
		body.setOutputMarkupId(true);
		
		DataView<Issue> issuesView = new DataView<Issue>("issues", dataProvider) {

			@Override
			protected void populateItem(Item<Issue> item) {
				Issue issue = item.getModelObject();
				item.add(new Label("id", "#" + issue.getId()));
				Fragment titleFrag = new Fragment("title", "titleFrag", IssueListPage.this);
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueDetailPage.class, IssueDetailPage.paramsOf(issue));
				link.add(new Label("label", issue.getTitle()));
				titleFrag.add(link);
				item.add(titleFrag);
				
				item.add(new UserLink("reporterName", User.getForDisplay(issue.getSubmitter(), issue.getSubmitterName())));
				item.add(new Label("reportDate", DateUtils.formatAge(issue.getSubmitDate())));
				
				item.add(new IssueStateLabel("state", item.getModel()));
				
				RepeatingView fieldsView = new RepeatingView("fields");
				for (String fieldName: getCustomization().getDisplayFields()) {
					fieldsView.add(new FieldValuesPanel(fieldsView.newChildId(), new AbstractReadOnlyModel<MultiValueIssueField>() {

						@Override
						public MultiValueIssueField getObject() {
							return item.getModelObject().getMultiValueFields().get(fieldName);
						}
						
					}).add(AttributeAppender.append("title", fieldName)));
				}
				
				item.add(fieldsView);
				item.add(new Label("votes", issue.getVotes()));
			}
			
		};
		issuesView.setItemsPerPage(WebConstants.PAGE_SIZE);
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject());
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		issuesView.setCurrentPage(pagingHistorySupport.getCurrentPage());
		
		body.add(issuesView);
		
		body.add(new HistoryAwarePagingNavigator("issuesPageNav", issuesView, pagingHistorySupport) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(issuesView.getPageCount() > 1);
			}
			
		});
		body.add(new WebMarkupContainer("noIssues") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(dataProvider.size() == 0);
			}
			
		});
		add(body);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListResourceReference()));
	}
	
}
