package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.StepFacade;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.ServerSideFacade;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.EditableStringVisitor;
import io.onedev.server.annotation.Interpolative;

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
	
	public boolean requireCommitIndex() {
		return false;
	}
	
	@Nullable
	public abstract Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger);
	
	public Collection<String> getPlaceholders() {
		Collection<String> placeholders = new HashSet<>();
		
		new EditableStringVisitor(t -> placeholders.addAll(KubernetesHelper.parsePlaceholders(t))).visitProperties(this, Interpolative.class);
		
		return placeholders;
	}
	
}
