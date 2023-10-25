package io.onedev.server.model;

import javax.persistence.*;

import static io.onedev.server.model.PackVersion.PROP_NAME;

@Entity
@Table(
		indexes={@Index(columnList="o_pack_id"), @Index(columnList= PROP_NAME)},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_pack_id", PROP_NAME})}
)
public class PackVersion extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PACK = "pack";
	
	public static final String PROP_BUILD = "build";
	
	public static final String PROP_NAME = "name";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Pack pack;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Build build;
	
	@Column(nullable=false)
	private String name;

	public Pack getPack() {
		return pack;
	}

	public void setPack(Pack pack) {
		this.pack = pack;
	}

	public Build getBuild() {
		return build;
	}

	public void setBuild(Build build) {
		this.build = build;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String reportName) {
		this.name = reportName;
	}

}
