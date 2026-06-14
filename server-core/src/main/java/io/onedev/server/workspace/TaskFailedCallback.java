package io.onedev.server.workspace;

import java.io.Serializable;

public interface TaskFailedCallback extends Serializable {

    /**
     * This method will be called in a Hibernate session
     * @param workspaceReference
     */
    void onTaskFailed(String workspaceReference);
	
}