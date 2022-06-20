package io.onedev.server.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={
				@Index(columnList="o_user_id"), @Index(columnList="o_request_id"), 
				@Index(columnList="o_comment_id")},
		uniqueConstraints={
				@UniqueConstraint(columnNames={"o_user_id", "o_request_id", "o_comment_id"})}
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
