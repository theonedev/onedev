package io.onedev.server.model.support.jobexecutor;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface JobExecutor extends Serializable {

	@Nullable
	String run(String image, List<String> commands);

	boolean isRunning(String runningInstance);
	
	void stop(String runningInstance);
	
}
