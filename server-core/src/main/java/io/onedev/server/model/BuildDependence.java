package io.onedev.server.model;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.onedev.server.rest.annotation.Api;

@Entity
@Table(
		indexes={@Index(columnList="dependent_id"), @Index(columnList="dependency_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"dependent_id", "dependency_id"})
})
public class BuildDependence extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_DEPENDENT = "dependent";
	
	public static final String PROP_DEPENDENCY = "dependency";
	
	@Api(exampleProvider="getDependentExample")
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build dependent;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build dependency;

	private boolean requireSuccessful;
	
	private String artifacts;
	
	private String destinationPath;
	
	public Build getDependent() {
		return dependent;
	}

	public void setDependent(Build dependent) {
		this.dependent = dependent;
	}

	public Build getDependency() {
		return dependency;
	}

	public void setDependency(Build dependency) {
		this.dependency = dependency;
	}

	public boolean isRequireSuccessful() {
		return requireSuccessful;
	}

	public void setRequireSuccessful(boolean requireSuccessful) {
		this.requireSuccessful = requireSuccessful;
	}

	@Nullable
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}

	@Nullable
	public String getDestinationPath() {
		return destinationPath;
	}

	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}
	
	@SuppressWarnings("unused")
	private static Build getDependentExample() {
		Build build = new Build();
		build.setId(2L);
		return build;
	}

}
