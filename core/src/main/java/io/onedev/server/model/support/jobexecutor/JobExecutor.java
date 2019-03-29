package io.onedev.server.model.support.jobexecutor;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.model.Build2;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface JobExecutor extends Serializable {

	@Nullable
	String run(Build2 build);

	boolean isRunning(Build2 build);
	
	void stop(Build2 build);
	
}
