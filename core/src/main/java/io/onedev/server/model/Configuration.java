package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.configuration.BuildCleanupRule;
import io.onedev.server.model.support.configuration.DoNotCleanup;
import io.onedev.server.util.facade.ConfigurationFacade;
import io.onedev.server.util.validation.annotation.CommitHash;
import io.onedev.server.util.validation.annotation.ConfigurationName;
import io.onedev.server.web.editable.annotation.Editable;

@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList="name")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", "name"})}
)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Configuration extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String ATTR_PROJECT = "project";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	private String name;
	
	private String baseCommit;
	
	private BuildCleanupRule buildCleanupRule = new DoNotCleanup();

	@OneToMany(mappedBy="configuration", cascade=CascadeType.REMOVE)
	private Collection<PullRequestBuild> pullRequestBuilds = new ArrayList<>();
	
	@OneToMany(mappedBy="configuration", cascade=CascadeType.REMOVE)
	private Collection<Build> builds = new ArrayList<>();
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Editable(order=100)
	@ConfigurationName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Optionally specify a base commit to calculate changes for the first build in "
			+ "the configuration")
	@CommitHash
	public String getBaseCommit() {
		return baseCommit;
	}

	public void setBaseCommit(String baseCommit) {
		this.baseCommit = baseCommit;
	}

	@Editable(order=300, description="Optionally specify build cleanup strategy to improvement OneDev performance. "
			+ "Note that this cleanup will not affect builds at build server side")
	@NotNull
	public BuildCleanupRule getBuildCleanupRule() {
		return buildCleanupRule;
	}

	public void setBuildCleanupRule(BuildCleanupRule buildCleanupRule) {
		this.buildCleanupRule = buildCleanupRule;
	}

	public Collection<PullRequestBuild> getPullRequestBuilds() {
		return pullRequestBuilds;
	}

	public void setPullRequestBuilds(Collection<PullRequestBuild> pullRequestBuilds) {
		this.pullRequestBuilds = pullRequestBuilds;
	}

	public Collection<Build> getBuilds() {
		return builds;
	}

	public void setBuilds(Collection<Build> builds) {
		this.builds = builds;
	}
	
	public ConfigurationFacade getFacade() {
		return new ConfigurationFacade(this);
	}
	
}
