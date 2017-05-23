package com.gitplex.server.web.page.depot.pullrequest.requestdetail.codecomments;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.util.editable.annotation.AccountChoice;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.FileChoice;
import com.gitplex.server.util.stringmatch.WildcardUtils;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.link.AccountLink;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.gitplex.server.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class RequestCodeCommentsPage extends RequestDetailPage {

	private static final String PARAM_USER = "user";
	
	private static final String PARAM_UNRESOLVED = "unresolved";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_BEFORE = "before";
	
	private static final String PARAM_AFTER = "after";
	
	private static final int TITLE_LEN = 100;
	
	private final State state;
	
	private List<String> commentedFiles;
	
	public RequestCodeCommentsPage(PageParameters params) {
		super(params);
		
		state = new State();
		state.userName = params.get(PARAM_USER).toString();
		state.unresolved = "yes".equals(params.get(PARAM_UNRESOLVED).toString());
		state.path = params.get(PARAM_PATH).toString();
		
		String value = params.get(PARAM_BEFORE).toString();
		if (value != null)
			state.before = new Date(Long.valueOf(value));
		
		value = params.get(PARAM_AFTER).toString();
		if (value != null)
			state.after = new Date(Long.valueOf(value));
	}

	public static PageParameters paramsOf(PullRequest request, State state) {
		PageParameters params = RequestDetailPage.paramsOf(request);
		
		if (state.userName != null)
			params.add(PARAM_USER, state.userName);
		if (state.unresolved)
			params.add(PARAM_UNRESOLVED, "yes");
		if (state.path != null)
			params.add(PARAM_PATH, state.path);
		if (state.before != null)
			params.add(PARAM_BEFORE, state.before.getTime());
		if (state.after != null)
			params.add(PARAM_AFTER, state.after.getTime());
		
		return params;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("myComments") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getAccount() != null);
			}

			@Override
			public void onClick() {
				State state = new State();
				state.userName = SecurityUtils.getAccount().getName();
				setResponsePage(RequestCodeCommentsPage.class, paramsOf(getPullRequest(), state));
			}
			
		});
		add(new Link<Void>("myUnresolvedComments") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getAccount() != null);
			}

			@Override
			public void onClick() {
				State state = new State();
				state.userName = SecurityUtils.getAccount().getName();
				state.unresolved = true;
				setResponsePage(RequestCodeCommentsPage.class, paramsOf(getPullRequest(), state));
			}
			
		});
		add(new Link<Void>("unresolvedComments") {

			@Override
			public void onClick() {
				State state = new State();
				state.unresolved = true;
				setResponsePage(RequestCodeCommentsPage.class, paramsOf(getPullRequest(), state));
			}
			
		});
		add(new Link<Void>("allComments") {

			@Override
			public void onClick() {
				PageParameters params = paramsOf(getPullRequest());
				setResponsePage(RequestCodeCommentsPage.class, params);
			}
			
		});
		Form<?> filterForm = new Form<Void>("filterForm") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
			}
			
		};
		filterForm.add(BeanContext.editBean("editor", state));
		add(filterForm);
		
		List<IColumn<CodeComment, Void>> columns = new ArrayList<>();
		columns.add(new AbstractColumn<CodeComment, Void>(Model.of("Code Comment")) {

			private void openComment(CodeComment comment) {
				setResponsePage(RequestChangesPage.class, RequestChangesPage.paramsOf(comment));
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<CodeComment>> cellItem,
					String componentId, IModel<CodeComment> rowModel) {
				CodeComment comment = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "commentFrag", RequestCodeCommentsPage.this);
				Link<Void> titleLink = new Link<Void>("title") {

					@Override
					public void onClick() {
						openComment(rowModel.getObject());
					}
					
				};
				titleLink.add(new Label("label", StringUtils.abbreviate(comment.getContent(), TITLE_LEN)));
				fragment.add(titleLink);
				fragment.add(new WebMarkupContainer("resolved").setVisible(comment.isResolved()));
				fragment.add(new AccountLink("user", Account.getForDisplay(comment.getUser(), comment.getUserName())));
				
				Link<Void> fileLink = new Link<Void>("file") {

					@Override
					public void onClick() {
						openComment(rowModel.getObject());
					}
					
				};
				fileLink.add(new Label("label", comment.getCommentPos().getPath()));
				fragment.add(fileLink);
				
				fragment.add(new Label("date", DateUtils.formatAge(comment.getDate())));
				
				WebMarkupContainer lastEventContainer = new WebMarkupContainer("lastEvent");
				if (comment.getLastEvent() != null) {
					String description = comment.getLastEvent().getType();
					lastEventContainer.add(new Label("description", description));
					
					Account userForDisplay = Account.getForDisplay(comment.getLastEvent().getUser(), 
							comment.getLastEvent().getUserName());
					lastEventContainer.add(new AccountLink("user", userForDisplay));
					lastEventContainer.add(new Label("date", DateUtils.formatAge(comment.getLastEvent().getDate())));
				} else {
					lastEventContainer.add(new WebMarkupContainer("description"));
					lastEventContainer.add(new WebMarkupContainer("user"));
					lastEventContainer.add(new WebMarkupContainer("date"));
					lastEventContainer.setVisible(false);
				}
				fragment.add(lastEventContainer);
				
				cellItem.add(fragment);
				
				Date lastUpdateDate;
				if (comment.getLastEvent() != null)
					lastUpdateDate = comment.getLastEvent().getDate();
				else
					lastUpdateDate = comment.getDate();
				cellItem.add(AttributeAppender.append("class", 
						comment.isVisitedAfter(lastUpdateDate)?"comment":"comment new"));
			}
			
		});

		IDataProvider<CodeComment> dataProvider = new IDataProvider<CodeComment>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends CodeComment> iterator(long first, long count) {
				List<CodeComment> comments = new ArrayList<>(getPullRequest().getCodeComments());
				state.filter(comments);
				return comments.iterator();
			}

			@Override
			public long size() {
				return getPullRequest().getCodeComments().size();
			}

			@Override
			public IModel<CodeComment> model(CodeComment object) {
				Long commentId = object.getId();
				return new LoadableDetachableModel<CodeComment>() {

					@Override
					protected CodeComment load() {
						return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
					}
					
				};
			}
			
		};
		DataTable<CodeComment, Void> dataTable = new DataTable<>("comments", columns, 
				dataProvider, getPullRequest()!=null?Integer.MAX_VALUE:WebConstants.DEFAULT_PAGE_SIZE);
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new BootstrapPagingNavigator(navigatorId, dataTable);
			}
			
		});
		add(dataTable);		
		
		if (getPullRequest() != null && dataTable.getItemCount() > PullRequest.MAX_CODE_COMMENTS) {
			add(new Label("alert", "Too many code comments, only displaying " + PullRequest.MAX_CODE_COMMENTS));
		} else {
			add(new WebMarkupContainer("alert").setVisible(false));
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new RequestCodeCommentsCssResourceReference()));
	}

	public List<String> getCommentedFiles() {
		if (commentedFiles == null) {
			/*
			 * Lazy initializing the commentedFiles as otherwise it may result in recursive initialization as
			 * getPullRequest().getCodeComments() can save CodeCommentRelation which again triggering 
			 * instantiation of RequestCodeComments in PullRequestChangeBroadcaster when calling WicketUtils.getPage()
			 */
			commentedFiles = new ArrayList<>();
			for (CodeComment comment: getPullRequest().getCodeComments()) {
				commentedFiles.add(comment.getCommentPos().getPath());
			}
			commentedFiles.sort((file1, file2)->Paths.get(file1).compareTo(Paths.get(file2)));
		}
		return commentedFiles;
	}

	@Editable
	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;

		private String userName;
		
		private boolean unresolved;
		
		private String path;
		
		private Date before;
		
		private Date after;
		
		@Editable(order=100, name="Created by", description="Choose the user who created the comment")
		@AccountChoice
		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		@Editable(order=200, name="Show unresolved only", description="Check this if you only want to show unresolved comments")
		public boolean isUnresolved() {
			return unresolved;
		}

		public void setUnresolved(boolean unresolved) {
			this.unresolved = unresolved;
		}

		@Editable(order=500, name="Commented path", description="Show comments on specified path")
		@FileChoice("getCommentedFiles")
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		@Editable(order=600, name="After date", description="Show comments after specified date")
		public Date getAfter() {
			return after;
		}

		public void setAfter(Date after) {
			this.after = after;
		}

		@Editable(order=700, name="Before date", description="Show comments before specified date")
		public Date getBefore() {
			return before;
		}

		public void setBefore(Date before) {
			this.before = before;
		}

		public void fill(EntityCriteria<CodeComment> criteria) {
			if (userName != null)
				criteria.createCriteria("user").add(Restrictions.eq("name", userName));
			if (unresolved)
				criteria.add(Restrictions.eq("resolved", false));
			if (path != null) {
				String pathQuery = path.replace('*', '%');
				if (pathQuery.endsWith("/"))
					pathQuery += "%";
				criteria.add(Restrictions.ilike("markPos.path", pathQuery));
			}
				
			if (before != null)
				criteria.add(Restrictions.le("date", before));
			if (after != null)
				criteria.add(Restrictions.ge("date", after));
		}
		
		public void filter(Collection<CodeComment> comments) {
			for (Iterator<CodeComment> it = comments.iterator(); it.hasNext();) {
				CodeComment comment = it.next();
				if (userName != null) {
					if (comment.getUser() == null || !comment.getUser().getName().equals(userName)) {
						it.remove();
						continue;
					}
				}
				if (unresolved && comment.isResolved()) {
					it.remove();
					continue;
				}
				if (path != null) {
					if (comment.getCommentPos().getPath() == null) {
						it.remove();
						continue;
					} else {
						String matchWith = path;
						if (matchWith.endsWith("/"))
							matchWith += "*";
						if (!WildcardUtils.matchString(matchWith, comment.getCommentPos().getPath())) {
							it.remove();
							continue;
						}
					}
				}
				if (before != null && comment.getDate().getTime()>before.getTime()) {
					it.remove();
					continue;
				}
				if (after != null && comment.getDate().getTime()<after.getTime()) {
					it.remove();
					continue;
				}
			}
		}
		
		@SuppressWarnings("unused")
		private static List<String> getCommentedFiles() {
			IPageRequestHandler handler = (IPageRequestHandler) RequestCycle.get().getActiveRequestHandler();
			return ((RequestCodeCommentsPage)handler.getPage()).getCommentedFiles();
		}
		
	}	
}
