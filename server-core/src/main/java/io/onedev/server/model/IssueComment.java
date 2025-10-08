package io.onedev.server.model;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import java.util.ArrayList;
import java.util.Collection;

import org.jspecify.annotations.Nullable;
import javax.mail.internet.InternetAddress;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.onedev.server.model.support.EntityComment;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.util.facade.IssueCommentFacade;

@Entity
@Table(indexes={
		@Index(columnList="o_issue_id"), @Index(columnList="o_user_id")})
public class IssueComment extends EntityComment {

	private static final long serialVersionUID = 1L;

	public static final String PROP_ISSUE = "issue";
	
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
