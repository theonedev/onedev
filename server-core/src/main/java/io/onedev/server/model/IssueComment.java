package io.onedev.server.model;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static io.onedev.server.model.IssueComment.PROP_MESSAGE_ID;

import java.util.ArrayList;
import java.util.Collection;

import org.jspecify.annotations.Nullable;
import jakarta.mail.internet.InternetAddress;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.onedev.server.model.support.EntityComment;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.util.facade.IssueCommentFacade;

@Entity
@Table(indexes={
		@Index(columnList="issue_id"), @Index(columnList="user_id"),
		@Index(columnList=PROP_MESSAGE_ID)})
public class IssueComment extends EntityComment {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ISSUE = "issue";

	public static final String PROP_MESSAGE_ID = "messageId";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	@Immutable
	private Issue issue;
	
	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<IssueCommentReaction> reactions = new ArrayList<>();

	@OneToMany(mappedBy="comment", cascade=CascadeType.REMOVE)
	private Collection<IssueCommentRevision> revisions = new ArrayList<>();

	@JsonProperty(access = READ_ONLY)
	private int revisionCount;

	@Lob
	private InternetAddress onBehalfOf;

	private String messageId;

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	@Nullable
	public InternetAddress getOnBehalfOf() {
		return onBehalfOf;
	}

	public void setOnBehalfOf(@Nullable InternetAddress onBehalfOf) {
		this.onBehalfOf = onBehalfOf;
	}

	@Nullable
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@Override
	public AbstractEntity getEntity() {
		return getIssue();
	}

	public Collection<IssueCommentReaction> getReactions() {
		return reactions;
	}

	public void setReactions(Collection<IssueCommentReaction> reactions) {
		this.reactions = reactions;
	}

	public Collection<IssueCommentRevision> getRevisions() {
		return revisions;
	}

	public void setRevisions(Collection<IssueCommentRevision> revisions) {
		this.revisions = revisions;
	}

	public IssueCommentFacade getFacade() {
		return new IssueCommentFacade(getId(), getIssue().getId(), getContent());
	}

	public int getRevisionCount() {
		return revisionCount;
	}

	public void setRevisionCount(int revisionCount) {
		this.revisionCount = revisionCount;
	}
	
}
