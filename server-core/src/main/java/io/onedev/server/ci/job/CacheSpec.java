package io.onedev.server.ci.job;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.Path;
import io.onedev.server.util.validation.annotation.PathSegment;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class CacheSpec implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String key;
	
	private String path;

	@Editable(order=100, description="Specify key of the cache. Caches with same key can be reused by different builds")
	@PathSegment
	@NotEmpty
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Editable(order=200, description="Specify path to cache. Non-absolute path is considered to be relative to job workspace. "
			+ "Specify \".\" (without quote) to cache workspace itself")
	@Path
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}
