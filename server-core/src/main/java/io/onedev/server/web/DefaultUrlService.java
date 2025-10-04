package io.onedev.server.web;

import com.google.common.base.Splitter;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.*;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.util.ProjectAndRevision;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Singleton
public class DefaultUrlService implements UrlService {

	private final ProjectService projectService;
	
	private final SettingService settingService;
	
	@Inject
	public DefaultUrlService(ProjectService projectService, SettingService settingService) {
		this.projectService = projectService;
		this.settingService = settingService;
	}
	
	@Override
	public String urlForProject(Long projectId, boolean withRootUrl) {
		return urlForProject(projectService.findFacadeById(projectId).getPath(), withRootUrl);
	}
	
	@Override
	public String urlForProject(String projectPath, boolean withRootUrl) {
		return (withRootUrl ? settingService.getSystemSetting().getServerUrl() : "") + "/" + projectPath;
	}
	
	@Override
	public String urlFor(Project project, boolean withRootUrl) {
		return urlForProject(project.getId(), withRootUrl);
	}

	@Override
	public String cloneUrlFor(Project project, boolean ssh) {
		if (ssh)
			return settingService.getSystemSetting().getEffectiveSshRootUrl() + "/" + project.getPath();
		else
			return settingService.getSystemSetting().getServerUrl() + "/" + project.getPath();
	}
	
	@Override
	public String urlFor(CodeComment comment, boolean withRootUrl) {
		return urlFor(comment, comment.getCompareContext(), withRootUrl);
	}

	@Override
	public String urlFor(CodeCommentReply reply, boolean withRootUrl) {
		return urlFor(reply.getComment(), reply.getCompareContext(), withRootUrl) + "#" + reply.getAnchor();
	}

	@Override
	public String urlFor(CodeCommentStatusChange change, boolean withRootUrl) {
		return urlFor(change.getComment(), change.getCompareContext(), withRootUrl) + "#" + change.getAnchor();
	}

	@Override
	public String urlFor(ProjectAndRevision projectAndRevision, boolean withRootUrl) {
		return urlFor(projectAndRevision.getProject(), withRootUrl) + "/~commits/" + projectAndRevision.getRevision();
	}

	@Override
	public String urlFor(Project project, ObjectId commitId, boolean withRootUrl) {
		return urlFor(new ProjectAndRevision(project, commitId.name()), withRootUrl);
	}

	private String urlFor(CodeComment comment, CompareContext compareContext, boolean withRootUrl) {
		Project project = comment.getProject();
		if (comment.isValid()) {
			PullRequest request = compareContext.getPullRequest();
			PageParametersEncoder paramsEncoder = new PageParametersEncoder();
			if (!compareContext.getOldCommitHash().equals(compareContext.getNewCommitHash())) {
				if (request != null) {
					PageParameters params = new PageParameters();
					PullRequestChangesPage.fillParams(params, PullRequestChangesPage.getState(comment, compareContext));
					return urlFor(request, withRootUrl) + "/changes" + paramsEncoder.encodePageParameters(params);
				} else {
					RevCommit oldCommit;
					if (compareContext.getOldCommitHash().equals(ObjectId.zeroId().name()))
						oldCommit = null;
					else
						oldCommit = project.getRevCommit(compareContext.getOldCommitHash(), true);
					RevCommit newCommit = project.getRevCommit(compareContext.getNewCommitHash(), true);
					if (oldCommit == null || isParent(oldCommit, newCommit)) {
						String url = urlFor(comment.getProject(), withRootUrl);
						PageParameters params = new PageParameters();
						CommitDetailPage.State state = CommitDetailPage.getState(comment, compareContext);

						// encode path param separately, otherwise it will be encoded as query param
						state.revision = null;
						params.set(0, newCommit.name());

						CommitDetailPage.fillParams(params, state);
						return url + "/~commits/" + paramsEncoder.encodePageParameters(params);
					} else {
						String url = urlFor(comment.getProject(), withRootUrl);
						PageParameters params = new PageParameters();
						RevisionComparePage.fillParams(params, RevisionComparePage.getState(comment, compareContext));
						return url + "/~compare" + paramsEncoder.encodePageParameters(params);
					}
				}
			} else {
				String url = urlFor(comment.getProject(), withRootUrl);
				PageParameters params = new PageParameters();
				ProjectBlobPage.State state = ProjectBlobPage.getState(comment);

				// encode path param separately, otherwise it will be encoded as query param
				state.blobIdent.path = null;
				state.blobIdent.revision = null;
				if (request != null)
					state.requestId = request.getId();
				params.set(0, comment.getMark().getCommitHash());
				List<String> pathSegments = Splitter.on("/").splitToList(comment.getMark().getPath());
				for (int i = 0; i < pathSegments.size(); i++)
					params.set(i + 1, pathSegments.get(i));

				ProjectBlobPage.fillParams(params, state);

				return url + "/~files/" + paramsEncoder.encodePageParameters(params);
			}
		} else {
			return urlFor(project, withRootUrl) + "/~code-comments/" + comment.getId() + "/invalid";
		}
	}
	
	private static boolean isParent(RevCommit parent, RevCommit child) {
		for (RevCommit each: child.getParents()) {
			if (each.equals(parent))
				return true;
		}
		return false;
	}
	
	@Override
	public String urlFor(PullRequest request, boolean withRootUrl) {
		return urlForPullRequest(request.getProject(), request.getNumber(), withRootUrl);
	}

	@Override
	public String urlForPullRequest(Project project, Long pullRequestNumber, boolean withRootUrl) {
		return urlFor(project, withRootUrl) + "/~pulls/" + pullRequestNumber;
	}

	@Override
	public String urlForAttachment(Project project, String attachmentGroup, String attachmentName, boolean withRootUrl) {
		var encodedAttachmentGroup = UrlEncoder.PATH_INSTANCE.encode(attachmentGroup, StandardCharsets.UTF_8);
		var encodedAttachmentName = UrlEncoder.PATH_INSTANCE.encode(attachmentName, StandardCharsets.UTF_8);
		return (withRootUrl ? settingService.getSystemSetting().getServerUrl() : "") + "/~downloads/projects/" + project.getId() + "/attachments/" + encodedAttachmentGroup + "/" + encodedAttachmentName;
	}

	@Override
	public String urlFor(PullRequestComment comment, boolean withRootUrl) {
		return urlFor(comment.getRequest(), withRootUrl) + "#" + comment.getAnchor();
	}

	@Override
	public String urlFor(PullRequestChange change, boolean withRootUrl) {
		return urlFor(change.getRequest(), withRootUrl) + "#" + change.getAnchor();
	}

	@Override
	public String urlFor(Issue issue, boolean withRootUrl) {
		return urlForIssue(issue.getProject(), issue.getNumber(), withRootUrl);
	}

	@Override
	public String urlForIssue(Project project, Long issueNumber, boolean withRootUrl) {
		return urlFor(project, withRootUrl) + "/~issues/" + issueNumber;
	}

	@Override
	public String urlFor(Build build, boolean withRootUrl) {
		return urlForBuild(build.getProject(), build.getNumber(), withRootUrl);
	}

	@Override
	public String urlForBuild(Project project, Long buildNumber, boolean withRootUrl) {
		return urlFor(project, withRootUrl) + "/~builds/" + buildNumber;
	}

	@Override
	public String urlFor(Pack pack, boolean withRootUrl) {
		return urlFor(pack.getProject(), withRootUrl) + "/~packages/" + pack.getId();
	}
	
	@Override
	public String urlFor(IssueComment comment, boolean withRootUrl) {
		return urlFor(comment.getIssue(), withRootUrl) + "#" + comment.getAnchor();
	}

	@Override
	public String urlFor(IssueChange change, boolean withRootUrl) {
		return urlFor(change.getIssue(), withRootUrl) + "#" + change.getAnchor();
	}
	
}
