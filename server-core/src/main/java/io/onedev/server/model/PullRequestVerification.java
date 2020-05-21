package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="o_request_id"), @Index(columnList="o_build_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_request_id", "o_build_id"})})
public class PullRequestVerification extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_BUILD = "build";
		
	public static final String PROP_REQUEST = "request";
	
	public static final String PROP_REQUIRED = "required";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
	private boolean required;
	
	public Build getBuild() {
		return build;
	}

	public void setBuild(Build build) {
		this.build = build;
	}

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

}
