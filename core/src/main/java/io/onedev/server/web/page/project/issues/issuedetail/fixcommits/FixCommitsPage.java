package io.onedev.server.web.page.project.issues.issuedetail.fixcommits;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.web.component.commitlist.CommitListPanel;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;

@SuppressWarnings("serial")
public class FixCommitsPage extends IssueDetailPage {

	public FixCommitsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new CommitListPanel("fixCommits", projectModel, new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				List<RevCommit> commits = new ArrayList<>();
				for (ObjectId commitId: getFixCommits()) {
					RevCommit commit = getProject().getRevCommit(commitId, false);
					if (commit != null)
						commits.add(commit);
				}
				Collections.sort(commits, new Comparator<RevCommit>() {

					@Override
					public int compare(RevCommit o1, RevCommit o2) {
						return o2.getCommitTime() - o1.getCommitTime();
					}
					
				});
				return commits;
			}
			
		}));
	}

}
