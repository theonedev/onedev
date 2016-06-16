package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.codecomments;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.web.component.comment.CodeCommentAware;
import com.pmease.gitplex.web.component.comment.CodeCommentFilterOption;
import com.pmease.gitplex.web.component.comment.CodeCommentListPanel;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;

@SuppressWarnings("serial")
public class CodeCommentsPage extends RequestDetailPage implements CodeCommentAware {

	private final CodeCommentFilterOption filterOption;
	
	private final List<String> commentedFiles = new ArrayList<>();
	
	public CodeCommentsPage(PageParameters params) {
		super(params);
		
		filterOption = new CodeCommentFilterOption(params);
		
		for (CodeComment comment: getPullRequest().getCodeComments()) {
			if (comment.getPath() != null) 
				commentedFiles.add(comment.getPath());
		}
		commentedFiles.sort((file1, file2)->Paths.get(file1).compareTo(Paths.get(file2)));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new CodeCommentListPanel("codeComments", new IModel<CodeCommentFilterOption>() {

			@Override
			public void detach() {
			}

			@Override
			public CodeCommentFilterOption getObject() {
				return filterOption;
			}

			@Override
			public void setObject(CodeCommentFilterOption object) {
				PageParameters params = paramsOf(getPullRequest());
				object.fillPageParams(params);
				setResponsePage(CodeCommentsPage.class, params);
			}
			
		}) {
			
			@Override
			protected PullRequest getPullRequest() {
				return CodeCommentsPage.this.getPullRequest();
			}

		});
	}

	@Override
	public List<String> getCommentedFiles() {
		return commentedFiles;
	}

}
