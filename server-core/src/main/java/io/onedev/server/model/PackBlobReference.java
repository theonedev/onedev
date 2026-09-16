package io.onedev.server.model;

import jakarta.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="pack_id"), @Index(columnList = "packBlob_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"pack_id", "packBlob_id"})})
public class PackBlobReference extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_PACK = "pack";
	
	public static final String PROP_PACK_BLOB = "packBlob";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Pack pack;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PackBlob packBlob;

	public Pack getPack() {
		return pack;
	}

	public void setPack(Pack pack) {
		this.pack = pack;
	}

	public PackBlob getPackBlob() {
		return packBlob;
	}

	public void setPackBlob(PackBlob packBlob) {
		this.packBlob = packBlob;
	}

}
