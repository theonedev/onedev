package io.onedev.server.model;

import javax.persistence.*;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList = "o_packBlob_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "o_packBlob_id"})})
public class PackBlobAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_PACK_BLOB = "packBlob";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PackBlob packBlob;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public PackBlob getPackBlob() {
		return packBlob;
	}

	public void setPackBlob(PackBlob packBlob) {
		this.packBlob = packBlob;
	}
	
}
