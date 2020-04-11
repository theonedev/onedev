package io.onedev.server.web.page.project.pullrequests.detail.codecomments;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.codecomment.CodeCommentListPanel;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.Cursor;

@SuppressWarnings("serial")
public class PullRequestCodeCommentsPage extends PullRequestDetailPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	public PullRequestCodeCommentsPage(PageParameters params) {
		super(params);
		
		query = params.get(PARAM_QUERY).toString();
	}

	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getPullRequest(), getCursor(), query);
				params.add(PARAM_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
		add(new CodeCommentListPanel("codeComments", query) {

			@Override
			protected Project getProject() {
				return PullRequestCodeCommentsPage.this.getProject();
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target, String query) {
				PageParameters params = paramsOf(getPullRequest(), getCursor(), query);
				setResponsePage(PullRequestCodeCommentsPage.class, params);
			}

			@Override
			protected PullRequest getPullRequest() {
				return PullRequestCodeCommentsPage.this.getPullRequest();
			}

		});
		
		RequestCycle.get().getListeners().add(new IRequestCycleListener() {
			
			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}
			
			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
			}
			
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}
			
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getUser() != null) { 
					OneDev.getInstance(UserInfoManager.class).visitPullRequestCodeComments(SecurityUtils.getUser(), getPullRequest());
				}
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});		
	}

	public static PageParameters paramsOf(PullRequest request, @Nullable Cursor cursor, @Nullable String query) {
		PageParameters params = paramsOf(request, cursor);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
