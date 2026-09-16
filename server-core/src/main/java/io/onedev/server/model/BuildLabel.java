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

@Entity
@Table(
		indexes={@Index(columnList="build_id"), @Index(columnList="spec_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"build_id", "spec_id"})}
)
public class BuildLabel extends EntityLabel {

	private static final long serialVersionUID = 1L;

	public static String PROP_BUILD = "build";
	
	public static String PROP_SPEC = "spec";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.BuildResource'>build</a>")
	private Build build;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.LabelSpecResource'>label spec</a>")
	private LabelSpec spec;

	public Build getBuild() {
		return build;
	}

	public void setBuild(Build build) {
		this.build = build;
	}

	public LabelSpec getSpec() {
		return spec;
	}

	public void setSpec(LabelSpec spec) {
		this.spec = spec;
	}

	@Override
	public AbstractEntity getEntity() {
		return getBuild();
	}
	
}
