package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.search.entity.SortField;

import org.jspecify.annotations.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static io.onedev.server.model.Pack.*;
import static io.onedev.server.search.entity.EntitySort.Direction.DESCENDING;

@Entity
@Table(
		indexes={
				@Index(columnList="o_project_id"), @Index(columnList= PROP_TYPE), 
				@Index(columnList= PROP_NAME), @Index(columnList= PROP_VERSION)
		}, uniqueConstraints = {
				@UniqueConstraint(columnNames={"o_project_id", PROP_TYPE, PROP_NAME, PROP_VERSION})
		}
)
public class Pack extends AbstractEntity implements LabelSupport<PackLabel> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME_PROJECT = "Project";
	
	public static final String PROP_PROJECT = "project";

	public static final String NAME_LABEL = "Label";
	
	public static final String NAME_TYPE = "Type";
	
	public static final String PROP_TYPE = "type";
	
	public static final String PROP_PRERELEASE = "prerelease";
	
	public static final String NAME_NAME = "Name";

	public static final String PROP_NAME = "name";
	
	public static final String NAME_VERSION = "Version";

	public static final String PROP_VERSION = "version";
	
	public static final String NAME_PUBLISH_DATE = "Publish Date";
	
	public static final String PROP_PUBLISH_DATE = "publishDate";
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_BUILD = "build";
	
	public static final List<String> QUERY_FIELDS = Lists.newArrayList(
			NAME_PROJECT, NAME_TYPE, NAME_NAME, NAME_VERSION, NAME_LABEL, NAME_PUBLISH_DATE);

	public static final Map<String, SortField<Pack>> SORT_FIELDS = new LinkedHashMap<>();
	static {
		SORT_FIELDS.put(NAME_TYPE, new SortField<>(PROP_TYPE));
		SORT_FIELDS.put(NAME_NAME, new SortField<>(PROP_NAME));
		SORT_FIELDS.put(NAME_VERSION, new SortField<>(PROP_VERSION));
		SORT_FIELDS.put(NAME_PUBLISH_DATE, new SortField<>(PROP_PUBLISH_DATE, DESCENDING));
		SORT_FIELDS.put(NAME_PROJECT, new SortField<>(PROP_PROJECT));
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String type;

	@Column(nullable=false)
	private String name;

	@Column(nullable=false)
	private String version;
	
	private boolean prerelease;

	@JsonIgnore
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
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isPrerelease() {
		return prerelease;
	}

	public void setPrerelease(boolean prerelease) {
		this.prerelease = prerelease;
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
	
	public String getReference(boolean withProject) {
		return getSupport().getReference(this, withProject);
	}
	
	public PackSupport getSupport() {
		if (support == null) {
			support = OneDev.getExtensions(PackSupport.class).stream()
					.filter(it -> it.getPackType().equals(type)).findFirst().get();
		}
		return support;
	}
	
	public long getSize() {
		return getBlobReferences().stream().map(it -> it.getPackBlob().getSize()).mapToLong(it -> it).sum();
	}
	
}
