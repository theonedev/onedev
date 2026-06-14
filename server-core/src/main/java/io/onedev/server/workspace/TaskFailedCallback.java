package io.onedev.server.workspace;

import java.io.Serializable;

public interface TaskFailedCallback extends Serializable {

    void onTaskFailed(String workspaceReference);
	
}