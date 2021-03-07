package io.onedev.server.web.component.codecomment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.OrEntityCriteria;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.search.entity.codecomment.ContentCriteria;
import io.onedev.server.search.entity.codecomment.PathCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.CodeCommentQueryBehavior;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.codecomments.InvalidCodeCommentPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public abstract class CodeCommentListPanel extends Panel {

	private final IModel<String> queryStringModel;
	
	private final IModel<CodeCommentQuery> queryModel = new LoadableDetachableModel<CodeCommentQuery>() {

		@Override
		protected CodeCommentQuery load() {
			String queryString = queryStringModel.getObject();
			try {
				return CodeCommentQuery.parse(getProject(), queryString);
			} catch (ExplicitException e) {
				error(e.getMessage());
				return null;
			} catch (Exception e) {
				warn("Not a valid formal query, performing fuzzy query");
				List<EntityCriteria<CodeComment>> criterias = new ArrayList<>();
				criterias.add(new ContentCriteria(queryString));
				criterias.add(new PathCriteria("*" + queryString + "*"));
				return new CodeCommentQuery(new OrEntityCriteria<CodeComment>(criterias));
			}
		}
		
	};
	
	private DataTable<CodeComment, Void> commentsTable;
	
	private TextField<String> queryInput;
	
	private Component saveQueryLink;
	
	private WebMarkupContainer body;
	
	private boolean querySubmitted = true;
	
	public CodeCommentListPanel(String id, IModel<String> queryModel) {
		super(id);
		this.queryStringModel = queryModel;
	}

	private CodeCommentManager getCodeCommentManager() {
		return OneDev.getInstance(CodeCommentManager.class);
	}
	
	private void doQuery(AjaxRequestTarget target) {
		commentsTable.setCurrentPage(0);
		target.add(body);
		querySubmitted = true;
		if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
			target.add(saveQueryLink);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("showSavedQueries") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SavedQueriesClosed) {
					((SavedQueriesClosed) event.getPayload()).getHandler().add(this);
				}
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuerySaveSupport() != null && !getQuerySaveSupport().isSavedQueriesVisible());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				send(getPage(), Broadcast.BREADTH, new SavedQueriesOpened(target));
				target.add(this);
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(saveQueryLink = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(querySubmitted && queryModel.getObject() != null);
				setVisible(SecurityUtils.getUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) 
					tag.append("class", "disabled", " ");
				if (!querySubmitted)
					tag.put("title", "Query not submitted");
				else if (queryModel.getObject() == null)
					tag.put("title", "Can not save malformed query");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getQuerySaveSupport().onSaveQuery(target, queryModel.getObject().toString());
			}		
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				List<String> orderFields = new ArrayList<>(CodeComment.ORDER_FIELDS.keySet());
				
				return new OrderEditPanel(id, orderFields, new IModel<List<EntitySort>> () {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						CodeCommentQuery query = queryModel.getObject();
						CodeCommentListPanel.this.getFeedbackMessages().clear();
						if (query != null) 
							return query.getSorts();
						else
							return new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						CodeCommentQuery query = queryModel.getObject();
						CodeCommentListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new CodeCommentQuery();
						query.getSorts().clear();
						query.getSorts().addAll(object);
						queryModel.setObject(query);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
						target.add(queryInput);
						doQuery(target);
					}
					
				});
			}
			
		});	
		
		queryInput = new TextField<String>("input", queryStringModel);
		queryInput.add(new CodeCommentQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}) {
			
			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				CodeCommentListPanel.this.getFeedbackMessages().clear();
				querySubmitted = StringUtils.trimToEmpty(queryStringModel.getObject())
						.equals(StringUtils.trimToEmpty(inputContent));
				target.add(saveQueryLink);
			}
			
		});
		queryInput.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				doQuery(target);
			}
			
		});
		
		Form<?> queryForm = new Form<Void>("query");
		queryForm.add(queryInput);
		queryForm.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				CodeCommentListPanel.this.getFeedbackMessages().clear();
				doQuery(target);
			}
			
		});
		add(queryForm);
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		body.add(new FencedFeedbackPanel("feedback", this));

		SortableDataProvider<CodeComment, Void> dataProvider = new LoadableDetachableDataProvider<CodeComment, Void>() {

			@Override
			public Iterator<? extends CodeComment> iterator(long first, long count) {
				try {
					return getCodeCommentManager().query(getProject(), getPullRequest(), 
							queryModel.getObject(), (int)first, (int)count).iterator();
				} catch (ExplicitException e) {
					error(e.getMessage());
					return new ArrayList<CodeComment>().iterator();
				}
			}

			@Override
			public long calcSize() {
				CodeCommentQuery query = queryModel.getObject();
				if (query != null) {
					try {
						return getCodeCommentManager().count(getProject(), getPullRequest(), query.getCriteria());
					} catch (ExplicitException e) {
						error(e.getMessage());
					}
				} 
				return 0;
			}

			@Override
			public IModel<CodeComment> model(CodeComment object) {
				Long commentId = object.getId();
				return new LoadableDetachableModel<CodeComment>() {

					@Override
					protected CodeComment load() {
						return OneDev.getInstance(CodeCommentManager.class).load(commentId);
					}
					
				};
			}
			
		};
		
		List<IColumn<CodeComment, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}

			@Override
			public String getCssClass() {
				return "new-indicator";
			}
			
		});
		
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("File")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				Fragment fragment = new Fragment(componentId, "fileFrag", CodeCommentListPanel.this);
				CodeComment comment = rowModel.getObject();
				WebMarkupContainer link;
				if (!comment.isValid()) {
					link = new ActionablePageLink<Void>("link", InvalidCodeCommentPage.class, 
							InvalidCodeCommentPage.paramsOf(comment)) {

						@Override
						protected void doBeforeNav(AjaxRequestTarget target) {
							String redirectUrlAfterDelete = RequestCycle.get().urlFor(
									getPage().getClass(), getPage().getPageParameters()).toString();
							WebSession.get().setRedirectUrlAfterDelete(CodeComment.class, redirectUrlAfterDelete);
						}
						
					};
				} else {
					if (comment.getRequest() != null) {
						link = new BookmarkablePageLink<Void>("link", PullRequestChangesPage.class, 
								PullRequestChangesPage.paramsOf(comment.getRequest(), comment));
					} else {
						String compareCommitHash = comment.getCompareContext().getCompareCommitHash();
						if (!compareCommitHash.equals(comment.getMark().getCommitHash())) {
							RevCommit markCommit = getProject().getRevCommit(comment.getMark().getCommitHash(), true);
							RevCommit compareCommit = getProject().getRevCommit(compareCommitHash, true);
							if (isParent(markCommit, compareCommit) || isParent(compareCommit, markCommit)) {
								link = new BookmarkablePageLink<Void>("link", CommitDetailPage.class, 
										CommitDetailPage.paramsOf(comment));
							} else {
								link = new BookmarkablePageLink<Void>("link", RevisionComparePage.class, 
										RevisionComparePage.paramsOf(comment));
							}
						} else {
							link = new BookmarkablePageLink<Void>("link", ProjectBlobPage.class, 
									ProjectBlobPage.paramsOf(comment));
						}
					}				
				}
				link.add(new Label("label", comment.getMark().getPath()));
				fragment.add(link);
				cellItem.add(fragment);
			}
			
			private boolean isParent(RevCommit parent, RevCommit child) {
				for (RevCommit each: child.getParents()) {
					if (each.equals(parent))
						return true;
				}
				return false;
			}

			@Override
			public String getCssClass() {
				return "text-break";
			}
			
		});
		
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("Last Update")) {

			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem, String componentId, IModel<CodeComment> rowModel) {
				CodeComment comment = rowModel.getObject();
				
				Fragment fragment = new Fragment(componentId, "lastUpdateFrag", CodeCommentListPanel.this);
				
				LastUpdate lastUpdate = comment.getLastUpdate();
				if (lastUpdate.getUser() != null || lastUpdate.getUserName() != null) {
					User user = User.from(lastUpdate.getUser(), lastUpdate.getUserName());
					fragment.add(new UserIdentPanel("user", user, Mode.NAME));
				} else {
					fragment.add(new WebMarkupContainer("user").setVisible(false));
				}
				fragment.add(new Label("activity", lastUpdate.getActivity()));
				fragment.add(new Label("date", DateUtils.formatAge(lastUpdate.getDate()))
					.add(new AttributeAppender("title", DateUtils.formatDateTime(lastUpdate.getDate()))));
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "d-none d-md-table-cell";
			}

		});
		
		body.add(commentsTable = new OneDataTable<CodeComment, Void>("comments", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()) {

			@Override
			protected Item<CodeComment> newRowItem(String id, int index, IModel<CodeComment> model) {
				Item<CodeComment> item = super.newRowItem(id, index, model);
				CodeComment comment = model.getObject();
				item.add(AttributeAppender.append("class", 
						comment.isVisitedAfter(comment.getLastUpdate().getDate())?"comment":"comment new"));
				return item;
			}
		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CodeCommentCssResourceReference()));
	}

	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}

	protected abstract Project getProject();
	
	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}

	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
}
