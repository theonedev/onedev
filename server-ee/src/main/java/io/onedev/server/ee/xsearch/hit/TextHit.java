package io.onedev.server.ee.xsearch.hit;

import io.onedev.commons.jsymbol.util.HighlightableLabel;
import io.onedev.commons.jsymbol.util.NoAntiCacheImage;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

public class TextHit extends QueryHit {

	private static final long serialVersionUID = 1L;
	
	private final String line;
	
	public TextHit(Long projectId, String blobPath, PlanarRange hitPos, String line) {
		super(projectId, blobPath, hitPos);
		
		this.line = line;
	}

	public String getLine() {
		return line;
	}
	
	@Override
	public Component render(String componentId) {
		if (getTokenPos() != null) {
			return new HighlightableLabel(componentId, line, 
					new LinearRange(getTokenPos().getFromColumn(), getTokenPos().getToColumn()));
		} else {
			return new HighlightableLabel(componentId, line, null);
		}
	}

	@Override
	public Image renderIcon(String componentId) {
		return new NoAntiCacheImage(componentId, new PackageResourceReference(FileHit.class, "bullet.png"));
	}

	@Override
	public String getNamespace() {
		String fileName = getBlobPath();
		if (fileName.contains("/")) 
			fileName = StringUtils.substringAfterLast(fileName, "/");
		return fileName;
	}

}
