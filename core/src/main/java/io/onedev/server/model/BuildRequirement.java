package io.onedev.server.model;

import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(indexes={@Index(columnList="o_request_id")})
public class BuildRequirement extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String ATTR_BUILD = "build";
	
	@Column(nullable=false)
	private String jobName;
	
	@Column(nullable=false, length=65535)
	@Lob
	private LinkedHashMap<String, List<String>> buildParams = new LinkedHashMap<>();
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Build build;
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public LinkedHashMap<String, List<String>> getBuildParams() {
		return buildParams;
	}

	public void setBuildParams(LinkedHashMap<String, List<String>> buildParams) {
		this.buildParams = buildParams;
	}

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
