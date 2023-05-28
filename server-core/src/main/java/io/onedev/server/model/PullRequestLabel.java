package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.rest.annotation.Api;

@Entity
@Table(
		indexes={@Index(columnList="o_request_id"), @Index(columnList="o_spec_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_request_id", "o_spec_id"})}
)
public class PullRequestLabel extends EntityLabel {

	private static final long serialVersionUID = 1L;

	public static String PROP_REQUEST = "request";
	
	public static String PROP_SPEC = "spec";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.PullRequestResource'>pull request</a>")
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.LabelSpecResource'>label spec</a>")
	private LabelSpec spec;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public LabelSpec getSpec() {
		return spec;
	}

	public void setSpec(LabelSpec spec) {
		this.spec = spec;
	}

	@Override
	public AbstractEntity getEntity() {
		return getRequest();
	}
	
}
