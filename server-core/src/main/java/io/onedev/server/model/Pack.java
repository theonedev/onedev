package io.onedev.server.model;

import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.util.CollectionUtils;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static io.onedev.server.model.Pack.*;

@Entity
@Table(indexes={
		@Index(columnList="o_project_id"), @Index(columnList= PROP_TYPE),
		@Index(columnList= PROP_TAG), @Index(columnList= PROP_GROUP_ID),
		@Index(columnList= PROP_ARTIFACT_ID), @Index(columnList= PROP_VERSION)}
)
public class Pack extends AbstractEntity implements LabelSupport<PackLabel> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME_PROJECT = "Project";
	
	public static final String PROP_PROJECT = "project";

	public static final String NAME_LABEL = "Label";
	
	public static final String NAME_TYPE = "Type";
	
	public static final String PROP_TYPE = "type";
	
	public static final String NAME_TAG = "Tag";
	
	public static final String PROP_TAG = "tag";

	public static final String NAME_GROUP_ID = "Group Id";

	public static final String PROP_GROUP_ID = "groupId";

	public static final String NAME_ARTIFACT_ID = "Artifact Id";

	public static final String PROP_ARTIFACT_ID = "artifactId";

	public static final String NAME_VERSION = "Version";

	public static final String PROP_VERSION = "version";
	
	public static final String NAME_PUBLISH_DATE = "Publish Date";
	
	public static final String PROP_PUBLISH_DATE = "publishDate";
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_BUILD = "build";
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_PROJECT, NAME_TYPE, NAME_TAG, NAME_GROUP_ID, NAME_ARTIFACT_ID, 
			NAME_VERSION, NAME_LABEL, NAME_PUBLISH_DATE);

	public static final Map<String, String> ORDER_FIELDS = CollectionUtils.newLinkedHashMap(
			NAME_TYPE, PROP_TYPE,
			NAME_TAG, PROP_TAG,
			NAME_GROUP_ID, PROP_GROUP_ID, 
			NAME_ARTIFACT_ID, PROP_ARTIFACT_ID, 
			NAME_VERSION, PROP_VERSION,
			NAME_PUBLISH_DATE, PROP_PUBLISH_DATE,
			NAME_PROJECT, PROP_PROJECT);

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String type;
	
	private String tag; 
	
	private String groupId;
	
	private String artifactId;
	
	private String version;

	@Lob
	@Column(length=65535)
	private Serializable data;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Build build;
	
	private Date publishDate;
	
	private transient PackSupport support;

	@OneToMany(mappedBy="pack", cascade=CascadeType.REMOVE)
	private Collection<PackBlobReference> blobReferences = new ArrayList<>();

	@OneToMany(mappedBy="pack", cascade=CascadeType.REMOVE)
	private Collection<PackLabel> labels = new ArrayList<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Nullable
	public String getTag() {
		return tag;
	}

	public void setTag(@Nullable String tag) {
		this.tag = tag;
	}

	@Nullable
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(@Nullable String groupId) {
		this.groupId = groupId;
	}

	@Nullable
	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(@Nullable String artifactId) {
		this.artifactId = artifactId;
	}

	@Nullable
	public String getVersion() {
		return version;
	}

	public void setVersion(@Nullable String version) {
		this.version = version;
	}

	public String getGAV() {
		return groupId + ":" + artifactId + ":" + version;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Nullable
	public Serializable getData() {
		return data;
	}

	public void setData(@Nullable Serializable data) {
		this.data = data;
	}

	@Nullable
	public Build getBuild() {
		return build;
	}

	public void setBuild(@Nullable Build build) {
		this.build = build;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Collection<PackBlobReference> getBlobReferences() {
		return blobReferences;
	}

	public void setBlobReferences(Collection<PackBlobReference> blobReferences) {
		this.blobReferences = blobReferences;
	}

	@Override
	public Collection<PackLabel> getLabels() {
		return labels;
	}

	public void setLabels(Collection<PackLabel> labels) {
		this.labels = labels;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	
	public PackSupport getSupport() {
		if (support == null) {
			support = OneDev.getExtensions(PackSupport.class).stream()
					.filter(it -> it.getPackType().equals(type)).findFirst().get();
		}
		return support;
	}
	
}
