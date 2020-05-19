package io.onedev.server.web.component.codecomment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.OrEntityCriteria;
import io.onedev.server.search.entity.codecomment.CodeCommentQuery;
import io.onedev.server.search.entity.codecomment.ContentCriteria;
import io.onedev.server.search.entity.codecomment.PathCriteria;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.CodeCommentQueryBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.codecomments.InvalidCodeCommentPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public abstract class CodeCommentListPanel extends Panel {

	private final IModel<String> queryModel;
	
	private final IModel<CodeCommentQuery> parsedQueryModel = new LoadableDetachableModel<CodeCommentQuery>() {

		@Override
		protected CodeCommentQuery load() {
			String query = queryModel.getObject();
			try {
				return CodeCommentQuery.parse(getProject(), query);
			} catch (OneException e) {
				error(e.getMessage());
				return null;
			} catch (Exception e) {
				warn("Not a valid formal query, interpreted as fuzzy query");
				List<EntityCriteria<CodeComment>> criterias = new ArrayList<>();
				criterias.add(new ContentCriteria(query));
				criterias.add(new PathCriteria("*" + query + "*"));
				return new CodeCommentQuery(new OrEntityCriteria<CodeComment>(criterias));
			}
		}
		
	};
	
	private DataTable<CodeComment, Void> commentsTable;
	
	public CodeCommentListPanel(String id, IModel<String> queryModel) {
		super(id);
		this.queryModel = queryModel;
	}

	private CodeCommentManager getCodeCommentManager() {
		return OneDev.getInstance(CodeCommentManager.class);
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
		
		Component saveQueryLink;
		add(saveQueryLink = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(parsedQueryModel.getObject() != null);
				setVisible(SecurityUtils.getUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) 
					tag.put("disabled", "disabled");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getQuerySaveSupport().onSaveQuery(target, queryModel.getObject());
			}		
			
		}.setOutputMarkupId(true));
		
		TextField<String> input = new TextField<String>("input", queryModel);
		input.add(new CodeCommentQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}));
		
		WebMarkupContainer body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		Form<?> form = new Form<Void>("query");
		form.add(input);
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				commentsTable.setCurrentPage(0);
				target.add(body);
				if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
					target.add(saveQueryLink);
			}
			
		});
		add(form);
		
		body.add(new FencedFeedbackPanel("feedback", this));

		SortableDataProvider<CodeComment, Void> dataProvider = new LoadableDetachableDataProvider<CodeComment, Void>() {

			@Override
			public Iterator<? extends CodeComment> iterator(long first, long count) {
				try {
					return getCodeCommentManager().query(getProject(), getPullRequest(), 
							parsedQueryModel.getObject(), (int)first, (int)count).iterator();
				} catch (OneException e) {
					error(e.getMessage());
					return new ArrayList<CodeComment>().iterator();
				}
			}

			@Override
			public long calcSize() {
				CodeCommentQuery parsedQuery = parsedQueryModel.getObject();
				if (parsedQuery != null) {
					try {
						return getCodeCommentManager().count(getProject(), getPullRequest(), parsedQuery.getCriteria());
					} catch (OneException e) {
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
						try {
							String compareCommit = comment.getCompareContext().getCompareCommitHash();
							if (!compareCommit.equals(comment.getMark().getCommitHash())
									&& getProject().getRepository().getObjectDatabase().has(ObjectId.fromString(compareCommit))) {
								link = new BookmarkablePageLink<Void>("link", RevisionComparePage.class, 
										RevisionComparePage.paramsOf(comment));
							} else {
								link = new BookmarkablePageLink<Void>("link", ProjectBlobPage.class, 
										ProjectBlobPage.paramsOf(comment));
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}				
				}
				link.add(new Label("label", comment.getMark().getPath()));
				fragment.add(link);
				cellItem.add(fragment);
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
				fragment.add(new Label("date", DateUtils.formatAge(lastUpdate.getDate())));
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "last-update expanded";
			}

		});
		
		body.add(commentsTable = new DefaultDataTable<CodeComment, Void>("comments", columns, dataProvider, 
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
		queryModel.detach();
		parsedQueryModel.detach();
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
