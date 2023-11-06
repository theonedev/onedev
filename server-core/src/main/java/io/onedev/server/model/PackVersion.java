package io.onedev.server.model;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

import static io.onedev.server.model.PackVersion.PROP_NAME;
import static io.onedev.server.model.PackVersion.PROP_TYPE;
import static io.onedev.server.model.PackVersion.PROP_DATA_HASH;

@Entity
@Table(
		indexes={
				@Index(columnList="o_project_id"), @Index(columnList= PROP_TYPE), 
				@Index(columnList= PROP_NAME), @Index(columnList= PROP_DATA_HASH)},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", PROP_TYPE, PROP_NAME})}
)
public class PackVersion extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final int MAX_DATA_LEN = 10000000;

	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_TYPE = "type";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_DATA_HASH = "dataHash";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String type;
	
	@Column(nullable=false)
	private String name;
	
	@Lob
	@Column(nullable=false, length = MAX_DATA_LEN)
	private byte[] dataBytes;
	
	@Column(nullable=false)
	private String dataHash;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Build build;

	@OneToMany(mappedBy="packVersion", cascade=CascadeType.REMOVE)
	private Collection<PackBlobReference> blobReferences = new ArrayList<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getDataBytes() {
		return dataBytes;
	}

	public void setDataBytes(byte[] dataBytes) {
		this.dataBytes = dataBytes;
	}

	public String getDataHash() {
		return dataHash;
	}

	public void setDataHash(String dataHash) {
		this.dataHash = dataHash;
	}

	@Nullable
	public Build getBuild() {
		return build;
	}

	public void setBuild(@Nullable Build build) {
		this.build = build;
	}

	public Collection<PackBlobReference> getBlobReferences() {
		return blobReferences;
	}

	public void setBlobReferences(Collection<PackBlobReference> blobReferences) {
		this.blobReferences = blobReferences;
	}
	
}
