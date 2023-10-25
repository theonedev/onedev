package io.onedev.server.model;

import io.onedev.server.model.support.PackSupport;
import io.onedev.server.util.facade.PackFacade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

import static io.onedev.server.model.Pack.PROP_NAME;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList= PROP_NAME)},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", PROP_NAME})}
)
public class Pack extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_TYPE = "type";
	
	public static final String PROP_NAME = "name";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private String type;
	
	@Lob
	@Column(nullable=false, length = 65535)
	private PackSupport support;
	
	@OneToMany(mappedBy= PackVersion.PROP_PACK, cascade=CascadeType.REMOVE)
	private Collection<PackVersion> versions = new ArrayList<>();

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

	public PackSupport getSupport() {
		return support;
	}

	public void setSupport(PackSupport support) {
		this.support = support;
	}

	public Collection<PackVersion> getVersions() {
		return versions;
	}

	public void setVersions(Collection<PackVersion> versions) {
		this.versions = versions;
	}

	@Override
	public PackFacade getFacade() {
		return new PackFacade(getId(), getProject().getId(), getName());
	}
	
}
