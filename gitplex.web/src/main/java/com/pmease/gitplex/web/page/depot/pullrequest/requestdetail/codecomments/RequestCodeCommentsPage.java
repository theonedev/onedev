package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.codecomments;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.web.component.comment.CodeCommentAware;
import com.pmease.gitplex.web.component.comment.CodeCommentFilter;
import com.pmease.gitplex.web.component.comment.CodeCommentListPanel;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;

@SuppressWarnings("serial")
public class RequestCodeCommentsPage extends RequestDetailPage implements CodeCommentAware {

	private final CodeCommentFilter filterOption;
	
	private List<String> commentedFiles;
	
	public RequestCodeCommentsPage(PageParameters params) {
		super(params);

		filterOption = new CodeCommentFilter(params);
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
			
		}) {
			
			@Override
			protected PullRequest getPullRequest() {
				return RequestCodeCommentsPage.this.getPullRequest();
			}

		});
	}

	@Override
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

}
