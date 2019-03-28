package io.onedev.server.ci.detect;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.ci.CISpec;
import io.onedev.server.model.Project;

@ExtensionPoint
public interface CISpecDetector {
	
	int DEFAULT_PRIORITY = 100;
	
	@Nullable
	CISpec detect(Project project, ObjectId commitId);
	
	int getPriority();
	
}
