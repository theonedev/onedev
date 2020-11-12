package io.onedev.server.web.page.project.pullrequests.detail.operationconfirm;

import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Splitter;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.util.CommitMessageBean;

@SuppressWarnings("serial")
public abstract class MergeConfirmPanel extends OperationConfirmPanel {

	private CommitMessageBean bean = new CommitMessageBean();
	
	public MergeConfirmPanel(String componentId, ModalPanel modal, Long latestUpdateId) {
		super(componentId, modal, latestUpdateId);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getPullRequest();
		if (request.getMergeStrategy() == MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS) {
			StringBuilder builder = new StringBuilder();
			
			for (PullRequestUpdate update: request.getSortedUpdates()) {
				for (RevCommit commit: update.getCommits()) {
					List<String> lines = Splitter.on('\n').splitToList(commit.getFullMessage().trim());
					for (int i=0; i<lines.size(); i++) {
						builder.append(i==0?"- ": "  ");
						builder.append(lines.get(i)).append("\n");
					}
					builder.append("\n");
				}
			}

			bean.setBody(builder.toString().trim());
		}

		String description = null;
		MergeStrategy mergeStrategy = getPullRequest().getMergeStrategy();
		MergePreview mergePreview = getPullRequest().getMergePreview();
		if (mergeStrategy == CREATE_MERGE_COMMIT) 
			bean.setSummary("Merge pull request " + request.getNumberAndTitle());
		else if (mergeStrategy == SQUASH_SOURCE_BRANCH_COMMITS) 
			bean.setSummary("Pull request " + request.getNumberAndTitle());
		else if (mergeStrategy == REBASE_SOURCE_BRANCH_COMMITS) 
			description = "Source branch commits will be rebased onto target branch";
		else if (mergePreview.getMergeCommitHash().equals(mergePreview.getHeadCommitHash())) 
			description = "Source branch commits will be fast-forwarded to target branch";
		else 
			bean.setSummary("Merge pull request " + request.getNumberAndTitle());
		
		getForm().add(new Label("description", description).setVisible(description != null));
		
		getForm().add(BeanContext.edit("commitMessage", bean).setVisible(description == null));
	}

	private PullRequest getPullRequest() {
		return getLatestUpdate().getRequest();
	}
	
	public String getCommitMessage() {
		if (bean.getSummary() != null) {
			StringBuilder builder = new StringBuilder(bean.getSummary());
			if (bean.getBody() != null) {
				builder.append("\n\n");
				builder.append(bean.getBody());
			}
			return builder.toString();
		} else {
			return null;
		}
	}

	@Override
	protected String getTitle() {
		return getPullRequest().getMergeStrategy().toString();
	}

}
