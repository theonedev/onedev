package io.onedev.server.workspace;

import java.io.Serializable;

import io.onedev.commons.utils.TaskLogger;

public interface WorkspaceRunnable extends Serializable {
	
	String run(TaskLogger workspaceLogger);
	
}
