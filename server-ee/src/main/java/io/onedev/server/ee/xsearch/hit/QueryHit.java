package io.onedev.server.ee.xsearch.hit;

import io.onedev.commons.utils.PlanarRange;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;

import javax.annotation.Nullable;
import java.io.Serializable;

public abstract class QueryHit implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	private final String blobPath;
	
	private final PlanarRange tokenPos;
	
	public QueryHit(Long projectId, String blobPath, @Nullable PlanarRange tokenPos) {
		this.projectId = projectId;
		this.blobPath = blobPath;
		this.tokenPos = tokenPos;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getBlobPath() {
		return blobPath;
	}
	
	@Nullable
	public PlanarRange getTokenPos() {
		return tokenPos;
	}

	public abstract Component render(String componentId);
	
	@Nullable
	public abstract String getNamespace();
	
	public abstract Image renderIcon(String componentId);
	
}
