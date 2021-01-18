package io.onedev.server.code;

import java.util.List;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Build;

@ExtensionPoint
public interface CodeProblemContribution {

	List<CodeProblem> getCodeProblems(Build build, String blobPath); 
	
}
