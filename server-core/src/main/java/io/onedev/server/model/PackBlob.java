package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
	indexes={
		@Index(columnList="o_project_id"), @Index(columnList= PackBlob.PROP_SHA256_HASH),
		@Index(columnList= PackBlob.PROP_CREATE_DATE)}, 
	uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", PackBlob.PROP_SHA256_HASH})})
public class PackBlob extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PACKS_DIR = "packages";

	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_SHA256_HASH = "sha256Hash";

	public static final String PROP_SHA512_HASH = "sha512Hash";
	
	public static final String PROP_CREATE_DATE = "createDate";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String sha256Hash;
	
	private String sha512Hash;

	private String md5Hash;

	private String sha1Hash;
	
	private long size;
	
	@Column(nullable=false)
	private Date createDate;
	
	@OneToMany(mappedBy="packBlob", cascade=CascadeType.REMOVE)
	private Collection<PackBlobReference> references = new ArrayList<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getSha256Hash() {
		return sha256Hash;
	}

	public void setSha256Hash(String hash) {
		this.sha256Hash = hash;
	}

	@Nullable
	public String getSha512Hash() {
		return sha512Hash;
	}

	public void setSha512Hash(@Nullable String sha512Hash) {
		this.sha512Hash = sha512Hash;
	}

	@Nullable
	public String getMd5Hash() {
		return md5Hash;
	}

	public void setMd5Hash(@Nullable String md5Hash) {
		this.md5Hash = md5Hash;
	}

	@Nullable
	public String getSha1Hash() {
		return sha1Hash;
	}

	public void setSha1Hash(@Nullable String sha1Hash) {
		this.sha1Hash = sha1Hash;
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

	public static String getFileLockName(Long projectId, String sha256Hash) {
		return "pack-blob:" + projectId + ":" + sha256Hash;
	}

	public static String getPacksRelativeDirPath(String sha256Hash) {
		return sha256Hash.substring(0, 2) + "/" 
				+ sha256Hash.substring(2, 4) + "/" + sha256Hash;	
	}
	
}
