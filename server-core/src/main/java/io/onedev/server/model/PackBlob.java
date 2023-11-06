package io.onedev.server.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(indexes={
		@Index(columnList="o_project_id"), @Index(columnList= PackBlob.PROP_HASH), 
		@Index(columnList= PackBlob.PROP_CREATE_DATE)})
public class PackBlob extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PACKS_DIR = "packages";

	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_HASH = "hash";
	
	public static final String PROP_CREATE_DATE = "createDate";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false, unique = true)
	private String hash;
	
	private long size;
	
	@Column(nullable=false)
	private Date createDate;
	
	@OneToMany(mappedBy="packBlob", cascade=CascadeType.REMOVE)
	private Collection<PackBlobReference> references = new ArrayList<>();

	@OneToMany(mappedBy="packBlob", cascade=CascadeType.REMOVE)
	private Collection<PackBlobAuthorization> authorizations = new ArrayList<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Collection<PackBlobAuthorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<PackBlobAuthorization> authorizations) {
		this.authorizations = authorizations;
	}
	
	public static String getFileLockName(Long projectId, String hash) {
		return "pack-blob:" + projectId + ":" + hash;
	}

	public static String getPacksRelativeDirPath(String hash) {
		return hash.substring(0, 2) + "/" 
				+ hash.substring(2, 4) + "/" + hash;	
	}
	
}
