package io.onedev.server.web.component.issuelist;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.FieldsEditBean;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.query.AndCriteria;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public abstract class IssueListPanel extends GenericPanel<String> {

	private static final Logger logger = LoggerFactory.getLogger(IssueListPanel.class);
	
	private IModel<IssueQuery> parsedQueryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			IssueQuery parsedQuery = null;
			try {
				parsedQuery = IssueQuery.parse(getProject(), getQuery(), true);
				if (SecurityUtils.getUser() == null && parsedQuery.needsLogin()) {
					error("Please login to perform this query");
					parsedQuery = null;
				} else if (getBaseCriteria() != null) {
					if (parsedQuery.getCriteria() != null) {
						List<IssueCriteria> criterias = Lists.newArrayList(getBaseCriteria(), parsedQuery.getCriteria());
						parsedQuery = new IssueQuery(new AndCriteria(criterias), parsedQuery.getSorts());
					} else {
						parsedQuery = new IssueQuery(getBaseCriteria(), parsedQuery.getSorts());
					}
				}
			} catch (Exception e) {
				logger.error("Error parsing issue query: " + getQuery(), e);
				if (StringUtils.isNotBlank(e.getMessage()))
					error(e.getMessage());
				else
					error("Malformed issue query");
			}
			return parsedQuery;
		}
		
	};
	
	private IModel<Integer> countModel = new LoadableDetachableModel<Integer>() {

		@Override
		protected Integer load() {
			IssueQuery parsedQuery = parsedQueryModel.getObject();
			if (parsedQuery != null)
				return getIssueManager().count(getProject(), parsedQuery.getCriteria());
			else
				return 0;
		}
		
	};
	
	public IssueListPanel(String id, IModel<String> queryModel) {
		super(id, queryModel);
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	private String getQuery() {
		return getModelObject();
	}
	
	@Override
	protected void onDetach() {
		parsedQueryModel.detach();
		countModel.detach();
		super.onDetach();
	}
	
	protected abstract Project getProject();

	@Nullable
	protected abstract IssueCriteria getBaseCriteria();

	protected abstract PagingHistorySupport getPagingHistorySupport();
	
	protected abstract void onQueryUpdated(AjaxRequestTarget target);
	
	protected abstract void onQueryUpdating(AjaxRequestTarget target);
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		TextField<String> input = new TextField<String>("input", getModel());
		input.add(new IssueQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}));
		input.add(new AjaxFormComponentUpdatingBehavior("input"){
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onQueryUpdating(target);
			}
			
		});
		
		Form<?> form = new Form<Void>("query");
		form.add(input);
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(IssueListPanel.this);
				onQueryUpdated(target);
			}
			
		});
		add(form);
		
		add(new ModalLink("displayFields") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldsFrag", IssueListPanel.this);

				FieldsEditBean bean = new FieldsEditBean();
				bean.setFields(getProject().getIssueListFields());
				Form<?> form = new Form<Void>("form") {

					@Override
					protected void onError() {
						super.onError();
						RequestCycle.get().find(AjaxRequestTarget.class).add(this);
					}

				};
				form.add(BeanContext.editBean("editor", bean));

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
						getProject().setIssueListFields((ArrayList<String>) bean.getFields());
						OneDev.getInstance(ProjectManager.class).save(getProject());
						target.add(IssueListPanel.this);
						modal.close();
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

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getProject()));
			}
			
		});

		String query;
		if (parsedQueryModel.getObject() != null)
			query = parsedQueryModel.getObject().toString();
		else
			query = null;
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject(), query)) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canRead(getProject()));
			}
			
		});
		
		IDataProvider<Issue> dataProvider = new IDataProvider<Issue>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends Issue> iterator(long first, long count) {
				return getIssueManager().query(getProject(), parsedQueryModel.getObject(), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return countModel.getObject();
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
		
		add(new NotificationPanel("feedback", this));
		
		DataView<Issue> issuesView = new DataView<Issue>("issues", dataProvider) {

			@Override
			protected void populateItem(Item<Issue> item) {
				Issue issue = item.getModelObject();
				item.add(new Label("number", "#" + issue.getNumber()));
				Fragment titleFrag = new Fragment("title", "titleFrag", IssueListPanel.this);
				QueryPosition position = new QueryPosition(parsedQueryModel.getObject().toString(), countModel.getObject(), 
						(int)getCurrentPage() * WebConstants.PAGE_SIZE + item.getIndex());
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueActivitiesPage.class, 
						IssueActivitiesPage.paramsOf(issue, position));
				link.add(new Label("label", issue.getTitle()));
				titleFrag.add(link);
				item.add(titleFrag);

				item.add(new UserLink("user", 
						User.getForDisplay(issue.getLastActivity().getUser(), issue.getLastActivity().getUserName())));
				item.add(new Label("action", issue.getLastActivity().getAction()));
				item.add(new Label("date", DateUtils.formatAge(issue.getLastActivity().getDate())));
				
				item.add(new IssueStateLabel("state", item.getModel()));
				
				RepeatingView fieldsView = new RepeatingView("fields");
				for (String fieldName: getProject().getIssueListFields()) {
					fieldsView.add(new FieldValuesPanel(fieldsView.newChildId()) {

						@Override
						protected Issue getIssue() {
							return item.getModelObject();
						}

						@Override
						protected IssueField getField() {
							return item.getModelObject().getEffectiveFields().get(fieldName);
						}
						
					}.add(AttributeAppender.append("title", fieldName)));
				}
				
				item.add(fieldsView);
				if (issue.getMilestone() != null) {
					Link<Void> milestoneLink = new BookmarkablePageLink<Void>("milestone", MilestoneDetailPage.class, 
							MilestoneDetailPage.paramsOf(issue.getMilestone(), null));
					milestoneLink.add(new Label("name", issue.getMilestoneName()));
					item.add(milestoneLink);
				} else {
					WebMarkupContainer milestoneLink = new WebMarkupContainer("milestone") {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("span");
						}
						
					};
					milestoneLink.add(new Label("name", "<i>Unspecified</i>").setEscapeModelStrings(false));
					item.add(milestoneLink);
				}
				item.add(new Label("votes", issue.getNumOfVotes()));
				item.add(new Label("comments", issue.getNumOfComments()));
				
				Date lastActivityDate;
				if (issue.getLastActivity() != null)
					lastActivityDate = issue.getLastActivity().getDate();
				else
					lastActivityDate = issue.getSubmitDate();
				item.add(AttributeAppender.append("class", 
						issue.isVisitedAfter(lastActivityDate)?"issue":"issue new"));
			}
			
		};
		issuesView.setItemsPerPage(WebConstants.PAGE_SIZE);
		
		issuesView.setCurrentPage(getPagingHistorySupport().getCurrentPage());
		
		add(issuesView);
		
		add(new HistoryAwarePagingNavigator("issuesPageNav", issuesView, getPagingHistorySupport()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(issuesView.getPageCount() > 1);
			}
			
		});
		add(new WebMarkupContainer("noIssues") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(parsedQueryModel.getObject() != null && countModel.getObject() == 0);
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListResourceReference()));
	}
	
}
