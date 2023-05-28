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
		indexes={@Index(columnList="o_build_id"), @Index(columnList="o_spec_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_build_id", "o_spec_id"})}
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
