package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.onedev.server.model.support.EntityLabel;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.Immutable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
		indexes={@Index(columnList="project_id"), @Index(columnList="spec_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"project_id", "spec_id"})}
)
@Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
public class ProjectLabel extends EntityLabel {

	private static final long serialVersionUID = 1L;

	public static String PROP_PROJECT = "project";
	
	public static String PROP_SPEC = "spec";
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.ProjectResource'>project</a>")
	@Immutable
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Api(description = "id of <a href='/~help/api/io.onedev.server.rest.LabelSpecResource'>label spec</a>")
	private LabelSpec spec;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public LabelSpec getSpec() {
		return spec;
	}

	public void setSpec(LabelSpec spec) {
		this.spec = spec;
	}

	@Override
	public AbstractEntity getEntity() {
		return getProject();
	}
	
}
