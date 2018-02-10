package com.turbodev.server.web.page.project.pullrequest.requestdetail.codecomments;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.VisitManager;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.component.comment.CodeCommentFilter;
import com.turbodev.server.web.component.comment.CodeCommentListPanel;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.RequestDetailPage;
import com.turbodev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class RequestCodeCommentsPage extends RequestDetailPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private final CodeCommentFilter filterOption;
	
	public RequestCodeCommentsPage(PageParameters params) {
		super(params);

		filterOption = new CodeCommentFilter(params);
	}

	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new CodeCommentListPanel("codeComments", new IModel<CodeCommentFilter>() {

			@Override
			public void detach() {
			}

			@Override
			public CodeCommentFilter getObject() {
				return filterOption;
			}

			@Override
			public void setObject(CodeCommentFilter object) {
				PageParameters params = paramsOf(getPullRequest());
				object.fillPageParams(params);
				setResponsePage(RequestCodeCommentsPage.class, params);
			}
			
		}, new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getPullRequest());
				filterOption.fillPageParams(params);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		}) {
			
			@Override
			protected PullRequest getPullRequest() {
				return RequestCodeCommentsPage.this.getPullRequest();
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
					TurboDev.getInstance(VisitManager.class).visitPullRequestCodeComments(SecurityUtils.getUser(), getPullRequest());
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

}
