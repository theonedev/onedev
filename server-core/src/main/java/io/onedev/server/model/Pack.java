package io.onedev.server.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.server.util.CollectionUtils;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.*;

import static io.onedev.server.model.Pack.*;

@Entity
@Table(
		indexes={
				@Index(columnList="o_project_id"), @Index(columnList= PROP_TYPE), 
				@Index(columnList= PROP_VERSION), @Index(columnList= PROP_DATA_HASH)},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", PROP_TYPE, PROP_VERSION})}
)
public class Pack extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final int MAX_DATA_LEN = 10000000;

	public static final String NAME_PROJECT = "Project";
	
	public static final String PROP_PROJECT = "project";
	
	public static final String NAME_TYPE = "Type";
	
	public static final String PROP_TYPE = "type";
	
	public static final String NAME_VERSION = "Version";
	
	public static final String PROP_VERSION = "version";
	
	public static final String PROP_DATA_HASH = "dataHash";
	
	public static final String NAME_CREATE_DATE = "Create Date";
	
	public static final String PROP_CREATE_DATE = "createDate";
	
	public static final Set<String> ALL_FIELDS = Sets.newHashSet(
			NAME_PROJECT, NAME_TYPE, NAME_VERSION, NAME_CREATE_DATE);

	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_PROJECT, NAME_TYPE, NAME_VERSION, NAME_CREATE_DATE);

	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			NAME_TYPE, PROP_TYPE,
			NAME_VERSION, PROP_VERSION,
			NAME_CREATE_DATE, PROP_CREATE_DATE,
			NAME_PROJECT, PROP_PROJECT);

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String type;
	
	@Column(nullable=false)
	private String version;
	
	@Lob
	@Column(nullable=false, length = MAX_DATA_LEN)
	private byte[] dataBytes;
	
	@Column(nullable=false)
	private String dataHash;
	
	private String extraInfo;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Build build;
	
	private Date createDate;

	@OneToMany(mappedBy="pack", cascade=CascadeType.REMOVE)
	private Collection<PackBlobReference> blobReferences = new ArrayList<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
}
