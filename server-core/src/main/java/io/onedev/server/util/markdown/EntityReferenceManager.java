package io.onedev.server.util.markdown;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.event.codecomment.CodeCommentCreated;
import io.onedev.server.event.codecomment.CodeCommentUpdated;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.issue.changedata.IssueDescriptionChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTitleChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDescriptionChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestTitleChangeData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.ProjectScopedNumber;

@Singleton
public class EntityReferenceManager {

	private final IssueChangeManager issueChangeManager;
	
	private final PullRequestChangeManager pullRequestChangeManager;
	
	private final MarkdownManager markdownManager;
	
	@Inject
	public EntityReferenceManager(IssueChangeManager issueChangeManager, 
			PullRequestChangeManager pullRequestChangeManager, MarkdownManager markdownManager) {
		this.issueChangeManager = issueChangeManager;
		this.pullRequestChangeManager = pullRequestChangeManager;
		this.markdownManager = markdownManager;
	}
	
	private void addReferenceChange(Issue issue, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownManager.render(markdown));			
			for (ProjectScopedNumber referencedIssueFQN: new ReferenceParser(Issue.class).parseReferences(document, issue.getProject())) {
				Issue referencedIssue = OneDev.getInstance(IssueManager.class).find(referencedIssueFQN);
				if (referencedIssue != null && !referencedIssue.equals(issue)) {
					boolean found = false;
					for (IssueChange change: referencedIssue.getChanges()) {
						if (change.getData() instanceof io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData) {
							io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData referencedFromIssueData = 
									(io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData) change.getData();
							if (referencedFromIssueData.getIssueId().equals(issue.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData referencedFromIssueData = 
								new io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData(issue);
						IssueChange change = new IssueChange();
						change.setData(referencedFromIssueData);
						change.setDate(new Date());
						change.setIssue(referencedIssue);
						referencedIssue.getChanges().add(change);
						issueChangeManager.save(change);
					}
				}
			}
			for (ProjectScopedNumber referencedRequestFQN: new ReferenceParser(PullRequest.class).parseReferences(document, issue.getProject())) {
				PullRequest referencedRequest  = OneDev.getInstance(PullRequestManager.class).find(referencedRequestFQN);
				if (referencedRequest != null) {
					boolean found = false;
					for (PullRequestChange change: referencedRequest.getChanges()) {
						if (change.getData() instanceof io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromIssueData) {
							io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromIssueData referencedFromIssueData = 
									(io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromIssueData) change.getData();
							if (referencedFromIssueData.getIssueId().equals(issue.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromIssueData referencedFromIssueData = 
								new io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromIssueData(issue);
						PullRequestChange change = new PullRequestChange();
						change.setData(referencedFromIssueData);
						change.setDate(new Date());
						change.setRequest(referencedRequest);
						referencedRequest.getChanges().add(change);
						pullRequestChangeManager.save(change);
					}
				}
			}
		}
	}
	
	private void addReferenceChange(PullRequest request, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownManager.render(markdown));			
			for (ProjectScopedNumber referencedIssueFQN: new ReferenceParser(Issue.class).parseReferences(document, request.getTargetProject())) {
				Issue referencedIssue = OneDev.getInstance(IssueManager.class).find(referencedIssueFQN);
				if (referencedIssue != null) {
					boolean found = false;
					for (IssueChange change: referencedIssue.getChanges()) {
						if (change.getData() instanceof io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData) {
							io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData referencedFromPullRequestData = 
									(io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData) change.getData();
							if (referencedFromPullRequestData.getRequestId().equals(request.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData referencedFromPullRequestData = 
								new io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData(request);
						IssueChange change = new IssueChange();
						change.setData(referencedFromPullRequestData);
						change.setDate(new Date());
						change.setIssue(referencedIssue);
						referencedIssue.getChanges().add(change);
						issueChangeManager.save(change);
					}
				}
			}
			for (ProjectScopedNumber referencedRequestFQN: new ReferenceParser(PullRequest.class).parseReferences(document, request.getTargetProject())) {
				PullRequest referencedRequest = OneDev.getInstance(PullRequestManager.class).find(referencedRequestFQN);
				if (referencedRequest != null && !referencedRequest.equals(request)) {
					boolean found = false;
					for (PullRequestChange change: referencedRequest.getChanges()) {
						if (change.getData() instanceof io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromPullRequestData) {
							io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromPullRequestData referencedFromPullRequestData = 
									(io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromPullRequestData) change.getData();
							if (referencedFromPullRequestData.getRequestId().equals(request.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromPullRequestData referencedFromPullRequestData = 
								new io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromPullRequestData(request);
						PullRequestChange change = new PullRequestChange();
						change.setData(referencedFromPullRequestData);
						change.setDate(new Date());
						change.setRequest(referencedRequest);
						referencedRequest.getChanges().add(change);
						pullRequestChangeManager.save(change);
					}
				}
			}
		}
	}
	
	private void addReferenceChange(CodeComment comment, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownManager.render(markdown));			
			for (ProjectScopedNumber referencedIssueFQN: new ReferenceParser(Issue.class).parseReferences(document, comment.getProject())) {
				Issue referencedIssue = OneDev.getInstance(IssueManager.class).find(referencedIssueFQN);
				if (referencedIssue != null) {
					boolean found = false;
					for (IssueChange change: referencedIssue.getChanges()) {
						if (change.getData() instanceof io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData) {
							io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData referencedFromCodeCommentData = 
									(io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData) change.getData();
							if (referencedFromCodeCommentData.getCommentId().equals(comment.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData referencedFromCodeCommentData = 
								new io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData(comment);
						IssueChange change = new IssueChange();
						change.setData(referencedFromCodeCommentData);
						change.setDate(new Date());
						change.setIssue(referencedIssue);
						referencedIssue.getChanges().add(change);
						issueChangeManager.save(change);
					}
				}
			}
			for (ProjectScopedNumber referencedRequestFQN: new ReferenceParser(PullRequest.class).parseReferences(document, comment.getProject())) {
				PullRequest referencedRequest = OneDev.getInstance(PullRequestManager.class).find(referencedRequestFQN);
				if (referencedRequest != null) {
					boolean found = false;
					for (PullRequestChange change: referencedRequest.getChanges()) {
						if (change.getData() instanceof io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromCodeCommentData) {
							io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromCodeCommentData referencedFromCodeCommentData = 
									(io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromCodeCommentData) change.getData();
							if (referencedFromCodeCommentData.getCommentId().equals(comment.getId())) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromCodeCommentData referencedFromCodeCommentData = 
								new io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromCodeCommentData(comment);
						PullRequestChange change = new PullRequestChange();
						change.setData(referencedFromCodeCommentData);
						change.setDate(new Date());
						change.setRequest(referencedRequest);
						referencedRequest.getChanges().add(change);
						pullRequestChangeManager.save(change);
					}
				}
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof IssueComment) {
			IssueComment comment = (IssueComment) event.getEntity();
			addReferenceChange(comment.getIssue(), comment.getContent());
		} else if (event.getEntity() instanceof IssueChange) {
			IssueChange change = (IssueChange) event.getEntity();
			if (change.getData().getCommentAware() != null)
				addReferenceChange(change.getIssue(), change.getData().getCommentAware().getComment());
		} else if (event.getEntity() instanceof PullRequestComment) {
			PullRequestComment comment = (PullRequestComment) event.getEntity();
			addReferenceChange(comment.getRequest(), comment.getContent());
		} else if (event.getEntity() instanceof PullRequestChange) {
			PullRequestChange change = (PullRequestChange) event.getEntity();
			if (change.getData().getCommentAware() != null)
				addReferenceChange(change.getRequest(), change.getData().getCommentAware().getComment());
		} else if (event.getEntity() instanceof CodeCommentReply) {
			CodeCommentReply reply = (CodeCommentReply) event.getEntity();
			addReferenceChange(reply.getComment(), reply.getContent());
		}
	}

	@Transactional
	@Listen
	public void on(PullRequestOpened event) {
		addReferenceChange(event.getRequest(), event.getRequest().getTitle());
		addReferenceChange(event.getRequest(), event.getRequest().getDescription());
	}
	
	@Transactional
	@Listen
	public void on(PullRequestChangeEvent event) {
		if (event.getChange().getData() instanceof PullRequestTitleChangeData)
			addReferenceChange(event.getRequest(), event.getRequest().getTitle());
		if (event.getChange().getData() instanceof PullRequestDescriptionChangeData)
			addReferenceChange(event.getRequest(), event.getRequest().getDescription());
	}
	
	@Transactional
	@Listen
	public void on(IssueOpened event) {
		addReferenceChange(event.getIssue(), event.getIssue().getTitle());
		addReferenceChange(event.getIssue(), event.getIssue().getDescription());
	}
	
	@Transactional
	@Listen
	public void on(IssueChangeEvent event) {
		if (event.getChange().getData() instanceof IssueTitleChangeData)
			addReferenceChange(event.getIssue(), event.getIssue().getTitle());
		if (event.getChange().getData() instanceof IssueDescriptionChangeData)
			addReferenceChange(event.getIssue(), event.getIssue().getDescription());
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentCreated event) {
		addReferenceChange(event.getComment(), event.getComment().getContent());
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentUpdated event) {
		addReferenceChange(event.getComment(), event.getComment().getContent());
	}
	
}
