package io.onedev.server.model.support.jobexecutor;

import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300)
public class KubernetesExecutor extends JobExecutor {

	private static final long serialVersionUID = 1L;

	public void execute(String environment, List<String> commands, 
			@Nullable SourceSnapshot snapshot, Logger logger) {
		throw new UnsupportedOperationException();
	}	
	
}