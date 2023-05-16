package io.onedev.server.ee.xsearch.hit;

import io.onedev.commons.jsymbol.util.HighlightableLabel;
import io.onedev.commons.jsymbol.util.NoAntiCacheImage;
import io.onedev.commons.utils.LinearRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import javax.annotation.Nullable;

public class FileHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final LinearRange match;
	
	public FileHit(Long projectId, String blobPath, @Nullable LinearRange match) {
		super(projectId, blobPath, null);
		this.match = match;
	}

	@Override
	public String toString() {
		return getBlobPath();
	}

	@Override
	public Component render(String componentId) {
		String fileName = getBlobPath();
		if (fileName.contains("/")) 
			fileName = StringUtils.substringAfterLast(fileName, "/");
		
		return new HighlightableLabel(componentId, fileName, match);
	}

	@Override
	public Image renderIcon(String componentId) {
		return new NoAntiCacheImage(componentId, new PackageResourceReference(FileHit.class, "file.png"));
	}

	@Override
	public String getNamespace() {
		if (getBlobPath().contains("/")) 
			return StringUtils.substringBeforeLast(getBlobPath(), "/");
		else 
			return null;
	}

	public LinearRange getMatch() {
		return match;
	}

}
