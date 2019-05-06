package io.onedev.server.search.code.hit;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import io.onedev.commons.jsymbol.util.HighlightableLabel;
import io.onedev.commons.jsymbol.util.NoAntiCacheImage;
import io.onedev.commons.utils.LinearRange;

public class PathHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final LinearRange match;
	
	public PathHit(String blobPath, @Nullable LinearRange match) {
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

	public LinearRange getMatch() {
		return match;
	}

}
