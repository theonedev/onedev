package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.ServerSideFacade;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.k8shelper.StepFacade;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.EditableStringVisitor;

public abstract class ServerSideStep extends Step {

	private static final long serialVersionUID = 1L;

	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		return new ServerSideFacade(this, getSourcePath(), 
				getFiles().getIncludes(), getFiles().getExcludes(), getPlaceholders());
	}
	
	@Nullable
	protected String getSourcePath() {
		return null;
	}

	protected PatternSet getFiles() {
		return new PatternSet(new HashSet<>(), new HashSet<>());
	}
		
	public abstract ServerStepResult run(Long buildId, File inputDir, TaskLogger logger);

	@Override
	public boolean isApplicable(Build build, JobExecutor executor) {
		return true;
	}
	
	public Collection<String> getPlaceholders() {
		Collection<String> placeholders = new HashSet<>();
		
		new EditableStringVisitor(t -> placeholders.addAll(KubernetesHelper.parsePlaceholders(t))).visitProperties(this, Interpolative.class);
		
		return placeholders;
	}
	
}
