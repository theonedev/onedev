package io.onedev.server.buildspec.job;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Project;

@ExtensionPoint
public interface JobSuggestion {
	
	Collection<Job> suggestJobs(Project project, ObjectId commitId);

}
