package io.onedev.server.codesearch.hit;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import io.onedev.jsymbol.util.HighlightableLabel;
import io.onedev.jsymbol.util.NoAntiCacheImage;
import io.onedev.utils.Range;

public class PathHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final Range match;
	
	public PathHit(String blobPath, @Nullable Range match) {
		super(blobPath, null);
		this.match = match;
	}

	@Override
	public String toString() {
		return getBlobPath();
	}

	@Override
	public Component render(String componentId) {
		return new HighlightableLabel(componentId, getBlobPath(), match);
	}

	@Override
	public Image renderIcon(String componentId) {
		return new NoAntiCacheImage(componentId, new PackageResourceReference(PathHit.class, "file.png"));
	}

	@Override
	public String getNamespace() {
		return null;
	}

	public Range getMatch() {
		return match;
	}

}
