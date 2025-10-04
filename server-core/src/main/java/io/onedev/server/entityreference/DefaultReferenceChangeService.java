package io.onedev.server.entityreference;

import static io.onedev.server.entityreference.ReferenceUtils.extractReferences;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.codecomment.CodeCommentCreated;
import io.onedev.server.event.project.codecomment.CodeCommentEdited;
import io.onedev.server.event.project.codecomment.CodeCommentReplyCreated;
import io.onedev.server.event.project.codecomment.CodeCommentReplyEdited;
import io.onedev.server.event.project.codecomment.CodeCommentStatusChanged;
import io.onedev.server.event.project.issue.IssueChanged;
import io.onedev.server.event.project.issue.IssueCommentCreated;
import io.onedev.server.event.project.issue.IssueCommentEdited;
import io.onedev.server.event.project.issue.IssueOpened;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.event.project.pullrequest.PullRequestCommentCreated;
import io.onedev.server.event.project.pullrequest.PullRequestCommentEdited;
import io.onedev.server.event.project.pullrequest.PullRequestOpened;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromCodeCommentData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromIssueData;
import io.onedev.server.model.support.issue.changedata.IssueReferencedFromPullRequestData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromCodeCommentData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromIssueData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReferencedFromPullRequestData;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class DefaultReferenceChangeService implements ReferenceChangeService {

	@Inject
	private IssueService issueService;

	@Inject
	private PullRequestService pullRequestService;

	@Inject
	private IssueChangeService issueChangeService;

	@Inject
	private PullRequestChangeService pullRequestChangeService;

	@Inject
	private MarkdownService markdownService;

	@Override
	public void addReferenceChange(User user, Issue issue, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownService.render(markdown));			
			for (var reference: extractReferences(document, issue.getProject())) {
				if (reference instanceof IssueReference) {
					var referencedIssue = issueService.find(reference.getProject(), reference.getNumber());
					if (referencedIssue != null && !referencedIssue.equals(issue)) {
						boolean found = false;
						for (var change : referencedIssue.getChanges()) {
							if (change.getData() instanceof IssueReferencedFromIssueData) {
								var referencedFromIssueData = (IssueReferencedFromIssueData) change.getData();
								if (referencedFromIssueData.getIssueId().equals(issue.getId())) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							var referencedFromIssueData = new IssueReferencedFromIssueData(issue);
							var change = new IssueChange();
							change.setData(referencedFromIssueData);
							change.setDate(new Date());
							change.setUser(user);
							change.setIssue(referencedIssue);
							referencedIssue.getChanges().add(change);
							issueChangeService.create(change, null);
						}
					}
				} else if (reference instanceof PullRequestReference) {
					var referencedPullRequest  = pullRequestService.find(reference.getProject(), reference.getNumber());
					if (referencedPullRequest != null) {
						boolean found = false;
						for (var change: referencedPullRequest.getChanges()) {
							if (change.getData() instanceof PullRequestReferencedFromIssueData) {
								var referencedFromIssueData = (PullRequestReferencedFromIssueData) change.getData();
								if (referencedFromIssueData.getIssueId().equals(issue.getId())) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							var referencedFromIssueData = new PullRequestReferencedFromIssueData(issue);
							var change = new PullRequestChange();
							change.setData(referencedFromIssueData);
							change.setDate(new Date());
							change.setUser(user);
							change.setRequest(referencedPullRequest);
							referencedPullRequest.getChanges().add(change);
							pullRequestChangeService.create(change, null);
						}
					}
				}
			}
		}
	}

	
	@Override 
	public void addReferenceChange(User user, PullRequest request, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownService.render(markdown));			
			for (var reference: extractReferences(document, request.getTargetProject())) {
				if (reference instanceof IssueReference) {
					var referencedIssue = issueService.find(reference.getProject(), reference.getNumber());
					if (referencedIssue != null) {
						boolean found = false;
						for (var change : referencedIssue.getChanges()) {
							if (change.getData() instanceof IssueReferencedFromPullRequestData) {
								var referencedFromPullRequestData = (IssueReferencedFromPullRequestData) change.getData();
								if (referencedFromPullRequestData.getRequestId().equals(request.getId())) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							var referencedFromPullRequestData = new IssueReferencedFromPullRequestData(request);
							var change = new IssueChange();
							change.setData(referencedFromPullRequestData);
							change.setDate(new Date());
							change.setUser(user);
							change.setIssue(referencedIssue);
							referencedIssue.getChanges().add(change);
							issueChangeService.create(change, null);
						}
					}
				} else if (reference instanceof PullRequestReference) {
					var referencedPullRequest = pullRequestService.find(reference.getProject(), reference.getNumber());
					if (referencedPullRequest != null && !referencedPullRequest.equals(request)) {
						boolean found = false;
						for (var change: referencedPullRequest.getChanges()) {
							if (change.getData() instanceof PullRequestReferencedFromPullRequestData) {
								var referencedFromPullRequestData = (PullRequestReferencedFromPullRequestData) change.getData();
								if (referencedFromPullRequestData.getRequestId().equals(request.getId())) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							var referencedFromPullRequestData = new PullRequestReferencedFromPullRequestData(request);
							var change = new PullRequestChange();
							change.setData(referencedFromPullRequestData);
							change.setDate(new Date());
							change.setUser(user);
							change.setRequest(referencedPullRequest);
							referencedPullRequest.getChanges().add(change);
							pullRequestChangeService.create(change, null);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void addReferenceChange(User user, CodeComment comment, String markdown) {
		if (markdown != null) {
			Document document = Jsoup.parseBodyFragment(markdownService.render(markdown));			
			for (var reference: extractReferences(document, comment.getProject())) {
				if (reference instanceof IssueReference) {
					var referencedIssue = issueService.find(reference.getProject(), reference.getNumber());
					if (referencedIssue != null) {
						boolean found = false;
						for (var change : referencedIssue.getChanges()) {
							if (change.getData() instanceof IssueReferencedFromCodeCommentData) {
								var referencedFromCodeCommentData = (IssueReferencedFromCodeCommentData) change.getData();
								if (referencedFromCodeCommentData.getCommentId().equals(comment.getId())) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							var referencedFromCodeCommentData = new IssueReferencedFromCodeCommentData(comment);
							var change = new IssueChange();
							change.setData(referencedFromCodeCommentData);
							change.setDate(new Date());
							change.setUser(user);
							change.setIssue(referencedIssue);
							referencedIssue.getChanges().add(change);
							issueChangeService.create(change, null);
						}
					}					
				} else if (reference instanceof PullRequestReference) {
					var referencedPullRequest = pullRequestService.find(reference.getProject(), reference.getNumber());
					if (referencedPullRequest != null) {
						boolean found = false;
						for (var change: referencedPullRequest.getChanges()) {
							if (change.getData() instanceof PullRequestReferencedFromCodeCommentData) {
								var referencedFromCodeCommentData = (PullRequestReferencedFromCodeCommentData) change.getData();
								if (referencedFromCodeCommentData.getCommentId().equals(comment.getId())) {
									found = true;
									break;
								}
							}
						}
						if (!found) {
							var referencedFromCodeCommentData = new PullRequestReferencedFromCodeCommentData(comment);
							var change = new PullRequestChange();
							change.setData(referencedFromCodeCommentData);
							change.setDate(new Date());
							change.setUser(user);
							change.setRequest(referencedPullRequest);
							referencedPullRequest.getChanges().add(change);
							pullRequestChangeService.create(change, null);
						}
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
