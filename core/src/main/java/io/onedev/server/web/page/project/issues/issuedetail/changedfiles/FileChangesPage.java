package io.onedev.server.web.page.project.issues.issuedetail.changedfiles;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CodeCommentManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.MarkPos;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.component.diff.revision.CommentSupport;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;

@SuppressWarnings("serial")
public class FileChangesPage extends IssueDetailPage implements CommentSupport {

	private Long commentId;
	
	private MarkPos mark;
	
	private String blameFile;
	
	private String pathFilter;
	
	private WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
	
	private IModel<RevCommit> commitModel = new LoadableDetachableModel<RevCommit>() {

		@Override
		protected RevCommit load() {
			return getProject().getRevCommit(ObjectId.fromString(getIssue().getCommit()));
		}
		
	};
	
	public FileChangesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		commitModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<String> blameModel = new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return blameFile;
			}

			@Override
			public void setObject(String object) {
				blameFile = object;
			}
			
		};
		IModel<String> pathFilterModel = new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return pathFilter;
			}

			@Override
			public void setObject(String object) {
				pathFilter = object;
			}
			
		};
		IModel<WhitespaceOption> whitespaceOptionModel = new IModel<WhitespaceOption>() {

			@Override
			public void detach() {
			}

			@Override
			public WhitespaceOption getObject() {
				return whitespaceOption;
			}

			@Override
			public void setObject(WhitespaceOption object) {
				whitespaceOption = object;
			}
			
		};

		if (getCommit().getParentCount() != 0) {
			add(new RevisionDiffPanel("fileChanges", projectModel, 
					new Model<PullRequest>(null), getCommit().getParent(0).name(), 
					getCommit().name(), pathFilterModel, whitespaceOptionModel, blameModel, this));
		} else {
			add(new Label("fileChanges", "<i>No file changes</i>").setEscapeModelStrings(false));
		}
	}
	
	private RevCommit getCommit() {
		return commitModel.getObject();
	}

	@Override
	public MarkPos getMark() {
		return mark;
	}

	@Override
	public String getMarkUrl(MarkPos mark) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		state.mark = mark;
		state.leftSide = new ProjectAndBranch(getProject(), getCommit().getParent(0).name());
		state.rightSide = new ProjectAndBranch(getProject(), getCommit().name());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(getProject(), state)).toString();
	}

	@Override
	public void onMark(AjaxRequestTarget target, MarkPos mark) {
		this.mark = mark;
	}

	@Override
	public CodeComment getOpenComment() {
		if (commentId != null)
			return OneDev.getInstance(CodeCommentManager.class).load(commentId);
		else
			return null;
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, MarkPos mark) {
		this.commentId = null;
		this.mark = mark;
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		if (comment != null) {
			commentId = comment.getId();
			mark = comment.getMarkPos();
		} else {
			commentId = null;
		}
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		mark = comment.getMarkPos();
		state.commentId = comment.getId();
		state.leftSide = new ProjectAndBranch(getProject(), getCommit().getParent(0).name());
		state.rightSide = new ProjectAndBranch(getProject(), getCommit().name());
		state.pathFilter = pathFilter;
		state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
		state.whitespaceOption = whitespaceOption;
		state.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, RevisionComparePage.paramsOf(getProject(), state)).toString();
	}
	
}
