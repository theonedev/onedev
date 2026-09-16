package io.onedev.server.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={
				@Index(columnList="user_id"), @Index(columnList="request_id"),
				@Index(columnList="comment_id")},
		uniqueConstraints={
				@UniqueConstraint(columnNames={"user_id", "request_id", "comment_id"})}
)
public class PendingSuggestionApply extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_REQUEST = "request";
	
	public static final String PROP_COMMENT = "comment";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private CodeComment comment;
	
	@Column(nullable=false)
	@Lob
	private ArrayList<String> suggestion;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public CodeComment getComment() {
		return comment;
	}

	public void setComment(CodeComment comment) {
		this.comment = comment;
	}

	public List<String> getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(ArrayList<String> suggestion) {
		this.suggestion = suggestion;
	}

}
