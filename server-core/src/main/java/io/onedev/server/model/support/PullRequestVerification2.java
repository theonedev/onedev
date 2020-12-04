package io.onedev.server.model.support;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.onedev.server.model.PullRequest;

@Embeddable
public class PullRequestVerification2 implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String COLUMN_PULL_REQUEST = "PULL_REQUEST";
	
	public static final String COLUMN_REQUIRED = "REQUIRED_BY_PULL_REQUEST";
	
	public static final String PROP_REQUEST = "request";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name=COLUMN_PULL_REQUEST)
	private PullRequest request;
	
	@Column(name=COLUMN_REQUIRED)
	private Boolean required;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

}
