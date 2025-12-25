package io.onedev.server.search.code.hit;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.PlanarRange;

public abstract class QueryHit implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String filePath;
	
	private final PlanarRange hitPos;
	
	public QueryHit(String filePath, @Nullable PlanarRange hitPos) {
		this.filePath = filePath;
		this.hitPos = hitPos;
	}

	public String getFilePath() {
		return filePath;
	}
	
	@Nullable
	public PlanarRange getHitPos() {
		return hitPos;
	}

	public abstract Component render(String componentId);
	
	@Nullable
	public abstract String getNamespace();
	
	public abstract Image renderIcon(String componentId);
	
}
