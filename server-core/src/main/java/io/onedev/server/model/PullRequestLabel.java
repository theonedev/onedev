package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.Immutable;

@Entity
@Table(
		indexes={@Index(columnList="request_id"), @Index(columnList="spec_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"request_id", "spec_id"})}
)
public class PullRequestLabel extends EntityLabel {

	private static final long serialVersionUID = 1L;

	public static String PROP_REQUEST = "request";
	
	public static String PROP_SPEC = "spec";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.PullRequestResource'>pull request</a>")
	@Immutable
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
