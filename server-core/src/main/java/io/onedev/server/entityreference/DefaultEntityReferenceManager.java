package io.onedev.server.entityreference;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.codecomment.*;
import io.onedev.server.event.project.issue.IssueChanged;
import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.event.project.issue.IssueCommentEdited;
import io.onedev.server.event.project.issue.IssueOpened;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.event.project.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.project.pullrequest.PullRequestCommentEdited;
import io.onedev.server.event.project.pullrequest.PullRequestOpened;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.model.*;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromCommitData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromCodeCommentData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromCommitData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromIssueData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromPullRequestData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.ProjectScopedNumber;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class DefaultEntityReferenceManager implements EntityReferenceManager {

	private final IssueChangeManager issueChangeManager;
	
	private final PullRequestChangeManager pullRequestChangeManager;
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public DefaultEntityReferenceManager(IssueChangeManager issueChangeManager, 
										 PullRequestChangeManager pullRequestChangeManager, 
										 MarkdownManager markdownManager) {
		this.issueChangeManager = issueChangeManager;
		this.pullRequestChangeManager = pullRequestChangeManager;
		this.markdownManager = markdownManager;
	}
	
	@Override
	public void addReferenceChange(User user, Issue issue, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownManager.render(markdown));			
			for (ProjectScopedNumber referencedIssueFQN: new ReferenceParser(Issue.class).parseReferences(document, issue.getProject())) {
				Issue referencedIssue = OneDev.getInstance(IssueManager.class).find(referencedIssueFQN);
				if (referencedIssue != null && !referencedIssue.equals(issue)) {
					boolean found = false;
					for (IssueChange change: referencedIssue.getChanges()) {
						if (change.getData() instanceof IssueReferencedFromIssueData) {
							IssueReferencedFromIssueData referencedFromIssueData = (IssueReferencedFromIssueData) change.getData();
							if (referencedFromIssueData.getIssueId().equals(issue.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						IssueReferencedFromIssueData referencedFromIssueData = new IssueReferencedFromIssueData(issue);
						IssueChange change = new IssueChange();
						change.setData(referencedFromIssueData);
						change.setDate(new Date());
						change.setUser(user);
						change.setIssue(referencedIssue);
						referencedIssue.getChanges().add(change);
						issueChangeManager.create(change, null);
					}
				}
			}
			for (ProjectScopedNumber referencedRequestFQN: new ReferenceParser(PullRequest.class).parseReferences(document, issue.getProject())) {
				PullRequest referencedRequest  = OneDev.getInstance(PullRequestManager.class).find(referencedRequestFQN);
				if (referencedRequest != null) {
					boolean found = false;
					for (PullRequestChange change: referencedRequest.getChanges()) {
						if (change.getData() instanceof PullRequestReferencedFromIssueData) {
							PullRequestReferencedFromIssueData referencedFromIssueData = (PullRequestReferencedFromIssueData) change.getData();
							if (referencedFromIssueData.getIssueId().equals(issue.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						PullRequestReferencedFromIssueData referencedFromIssueData = new PullRequestReferencedFromIssueData(issue);
						PullRequestChange change = new PullRequestChange();
						change.setData(referencedFromIssueData);
						change.setDate(new Date());
						change.setUser(user);
						change.setRequest(referencedRequest);
						referencedRequest.getChanges().add(change);
						pullRequestChangeManager.create(change, null);
					}
				}
			}
		}
	}

	public void addReferenceChange(ProjectScopedCommit commit) {
		String commitMessage = commit.getRevCommit().getFullMessage();
		for (ProjectScopedNumber referencedIssueFQN: new ReferenceParser(Issue.class).parseReferences(commitMessage, commit.getProject())) {
			Issue referencedIssue = OneDev.getInstance(IssueManager.class).find(referencedIssueFQN);
			if (referencedIssue != null) {
				boolean found = false;
				for (IssueChange change: referencedIssue.getChanges()) {
					if (change.getData() instanceof IssueReferencedFromCommitData) {
						IssueReferencedFromCommitData referencedFromCommitData = (IssueReferencedFromCommitData) change.getData();
						if (referencedFromCommitData.getCommit().getCommitId().equals(commit.getCommitId())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					IssueReferencedFromCommitData referencedFromCommitData = new IssueReferencedFromCommitData(commit);
					IssueChange change = new IssueChange();
					change.setData(referencedFromCommitData);
					change.setDate(new Date());
					change.setIssue(referencedIssue);
					referencedIssue.getChanges().add(change);
					issueChangeManager.create(change, null);
				}
			}
		}
		for (ProjectScopedNumber referencedRequestFQN: new ReferenceParser(PullRequest.class).parseReferences(commitMessage, commit.getProject())) {
			PullRequest referencedRequest  = OneDev.getInstance(PullRequestManager.class).find(referencedRequestFQN);
			if (referencedRequest != null) {
				boolean found = false;
				for (PullRequestChange change: referencedRequest.getChanges()) {
					if (change.getData() instanceof PullRequestReferencedFromCommitData) {
						PullRequestReferencedFromCommitData referencedFromCommitData = (PullRequestReferencedFromCommitData) change.getData();
						if (referencedFromCommitData.getCommit().getCommitId().equals(commit.getCommitId())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					PullRequestReferencedFromCommitData referencedFromCommitData = new PullRequestReferencedFromCommitData(commit);
					PullRequestChange change = new PullRequestChange();
					change.setData(referencedFromCommitData);
					change.setDate(new Date());
					change.setRequest(referencedRequest);
					referencedRequest.getChanges().add(change);
					pullRequestChangeManager.create(change, null);
				}
			}
		}
	}
	
	@Override 
	public void addReferenceChange(User user, PullRequest request, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownManager.render(markdown));			
			for (ProjectScopedNumber referencedIssueFQN: new ReferenceParser(Issue.class).parseReferences(document, request.getTargetProject())) {
				Issue referencedIssue = OneDev.getInstance(IssueManager.class).find(referencedIssueFQN);
				if (referencedIssue != null) {
					boolean found = false;
					for (IssueChange change: referencedIssue.getChanges()) {
						if (change.getData() instanceof IssueReferencedFromPullRequestData) {
							IssueReferencedFromPullRequestData referencedFromPullRequestData = (IssueReferencedFromPullRequestData) change.getData();
							if (referencedFromPullRequestData.getRequestId().equals(request.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						IssueReferencedFromPullRequestData referencedFromPullRequestData = new IssueReferencedFromPullRequestData(request);
						IssueChange change = new IssueChange();
						change.setData(referencedFromPullRequestData);
						change.setDate(new Date());
						change.setUser(user);
						change.setIssue(referencedIssue);
						referencedIssue.getChanges().add(change);
						issueChangeManager.create(change, null);
					}
				}
			}
			for (ProjectScopedNumber referencedRequestFQN: new ReferenceParser(PullRequest.class).parseReferences(document, request.getTargetProject())) {
				PullRequest referencedRequest = OneDev.getInstance(PullRequestManager.class).find(referencedRequestFQN);
				if (referencedRequest != null && !referencedRequest.equals(request)) {
					boolean found = false;
					for (PullRequestChange change: referencedRequest.getChanges()) {
						if (change.getData() instanceof PullRequestReferencedFromPullRequestData) {
							PullRequestReferencedFromPullRequestData referencedFromPullRequestData = (PullRequestReferencedFromPullRequestData) change.getData();
							if (referencedFromPullRequestData.getRequestId().equals(request.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						PullRequestReferencedFromPullRequestData referencedFromPullRequestData = new PullRequestReferencedFromPullRequestData(request);
						PullRequestChange change = new PullRequestChange();
						change.setData(referencedFromPullRequestData);
						change.setDate(new Date());
						change.setUser(user);
						change.setRequest(referencedRequest);
						referencedRequest.getChanges().add(change);
						pullRequestChangeManager.create(change, null);
					}
				}
			}
		}
	}
	
	@Override
	public void addReferenceChange(User user, CodeComment comment, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownManager.render(markdown));			
			for (ProjectScopedNumber referencedIssueFQN: new ReferenceParser(Issue.class).parseReferences(document, comment.getProject())) {
				Issue referencedIssue = OneDev.getInstance(IssueManager.class).find(referencedIssueFQN);
				if (referencedIssue != null) {
					boolean found = false;
					for (IssueChange change: referencedIssue.getChanges()) {
						if (change.getData() instanceof IssueReferencedFromCodeCommentData) {
							IssueReferencedFromCodeCommentData referencedFromCodeCommentData = (IssueReferencedFromCodeCommentData) change.getData();
							if (referencedFromCodeCommentData.getCommentId().equals(comment.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						IssueReferencedFromCodeCommentData referencedFromCodeCommentData = new IssueReferencedFromCodeCommentData(comment);
						IssueChange change = new IssueChange();
						change.setData(referencedFromCodeCommentData);
						change.setDate(new Date());
						change.setUser(user);
						change.setIssue(referencedIssue);
						referencedIssue.getChanges().add(change);
						issueChangeManager.create(change, null);
					}
				}
			}
			for (ProjectScopedNumber referencedRequestFQN: new ReferenceParser(PullRequest.class).parseReferences(document, comment.getProject())) {
				PullRequest referencedRequest = OneDev.getInstance(PullRequestManager.class).find(referencedRequestFQN);
				if (referencedRequest != null) {
					boolean found = false;
					for (PullRequestChange change: referencedRequest.getChanges()) {
						if (change.getData() instanceof PullRequestReferencedFromCodeCommentData) {
							PullRequestReferencedFromCodeCommentData referencedFromCodeCommentData = (PullRequestReferencedFromCodeCommentData) change.getData();
							if (referencedFromCodeCommentData.getCommentId().equals(comment.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						PullRequestReferencedFromCodeCommentData referencedFromCodeCommentData = new PullRequestReferencedFromCodeCommentData(comment);
						PullRequestChange change = new PullRequestChange();
						change.setData(referencedFromCodeCommentData);
						change.setDate(new Date());
						change.setUser(user);
						change.setRequest(referencedRequest);
						referencedRequest.getChanges().add(change);
						pullRequestChangeManager.create(change, null);
					}
				}
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(IssueCommentCreated event) {
		addReferenceChange(event.getUser(), event.getIssue(), event.getComment().getContent());
	}

	@Transactional
	@Listen
	public void on(IssueCommentEdited event) {
		addReferenceChange(event.getUser(), event.getIssue(), event.getComment().getContent());
	}
	
	@Transactional
	@Listen
	public void on(IssueChanged event) {
		addReferenceChange(event.getUser(), event.getIssue(), event.getComment());
	}
	
	@Transactional
	@Listen
	public void on(PullRequestCommentCreated event) {
		addReferenceChange(event.getUser(), event.getRequest(), event.getComment().getContent());
	}

	@Transactional
	@Listen
	public void on(PullRequestCommentEdited event) {
		addReferenceChange(event.getUser(), event.getRequest(), event.getComment().getContent());
	}
	
	@Transactional
	@Listen
	public void on(PullRequestChanged event) {
		addReferenceChange(event.getUser(), event.getRequest(), event.getComment());
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentReplyCreated event) {
		addReferenceChange(event.getUser(), event.getComment(), event.getReply().getContent());
	}

	@Transactional
	@Listen
	public void on(CodeCommentReplyEdited event) {
		addReferenceChange(event.getUser(), event.getComment(), event.getReply().getContent());
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentStatusChanged event) {
		addReferenceChange(event.getUser(), event.getComment(), event.getNote());
	}
	
	@Transactional
	@Listen
	public void on(PullRequestOpened event) {
		addReferenceChange(event.getUser(), event.getRequest(), event.getRequest().getTitle());
		addReferenceChange(event.getUser(), event.getRequest(), event.getRequest().getDescription());
	}
	
	@Transactional
	@Listen
	public void on(IssueOpened event) {
		addReferenceChange(event.getUser(), event.getIssue(), event.getIssue().getTitle());
		addReferenceChange(event.getUser(), event.getIssue(), event.getIssue().getDescription());
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentCreated event) {
		addReferenceChange(event.getUser(), event.getComment(), event.getComment().getContent());
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentEdited event) {
		addReferenceChange(event.getUser(), event.getComment(), event.getComment().getContent());
	}
	
}
