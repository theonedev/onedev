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
public class PullRequestBuild extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String ATTR_BUILD = "build";
		
	public static final String ATTR_REQUEST = "request";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build build;
	
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

}
