package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

public interface DiffSupport extends Serializable {

	List<String> getOldLines();
	
	List<String> getNewLines();
	
	@Nullable
	String getOldFileName();
	
	@Nullable
	String getNewFileName();
}
