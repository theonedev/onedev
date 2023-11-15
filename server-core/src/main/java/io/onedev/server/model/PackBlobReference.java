package io.onedev.server.model;

import javax.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="o_packVersion_id"), @Index(columnList = "o_packBlob_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_packVersion_id", "o_packBlob_id"})})
public class PackBlobReference extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_PACK_VERSION = "packVersion";
	
	public static final String PROP_PACK_BLOB = "packBlob";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PackVersion packVersion;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PackBlob packBlob;

	public PackVersion getPackVersion() {
		return packVersion;
	}

	public void setPackVersion(PackVersion packVersion) {
		this.packVersion = packVersion;
	}

	public PackBlob getPackBlob() {
		return packBlob;
	}

	public void setPackBlob(PackBlob packBlob) {
		this.packBlob = packBlob;
	}
	
}
