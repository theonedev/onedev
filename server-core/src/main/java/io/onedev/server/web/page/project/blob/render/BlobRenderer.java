package io.onedev.server.web.page.project.blob.render;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.commons.utils.PlanarRange;

@ExtensionPoint
public interface BlobRenderer extends Serializable {

	public static final String SOURCE_POSITION_PREFIX = "source-";
	
	@Nullable
	public static PlanarRange getSourceRange(@Nullable String position) {
		if (position != null && position.startsWith(SOURCE_POSITION_PREFIX))
			return PlanarRange.of(position.substring(SOURCE_POSITION_PREFIX.length()));
		else
			return null;
	}
	
	@Nullable
	public static String getSourcePosition(@Nullable PlanarRange range) {
		return range!=null? SOURCE_POSITION_PREFIX + range.toString(): null;
	}

	@Nullable Component render(String componentId, BlobRenderContext context);
	
}
