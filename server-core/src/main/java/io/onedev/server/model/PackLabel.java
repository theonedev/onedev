package io.onedev.server.model;

import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.Immutable;

import javax.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="o_pack_id"), @Index(columnList="o_spec_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_pack_id", "o_spec_id"})}
)
public class PackLabel extends EntityLabel {

	private static final long serialVersionUID = 1L;

	public static String PROP_PACK = "pack";
	
	public static String PROP_SPEC = "spec";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.PackResource'>package</a>")
	@Immutable
	private Pack pack;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.LabelSpecResource'>label spec</a>")
	private LabelSpec spec;

	public Pack getPack() {
		return pack;
	}

	public void setPack(Pack pack) {
		this.pack = pack;
	}

	public LabelSpec getSpec() {
		return spec;
	}

	public void setSpec(LabelSpec spec) {
		this.spec = spec;
	}

	@Override
	public AbstractEntity getEntity() {
		return getPack();
	}
	
}
