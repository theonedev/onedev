package com.turbodev.server.search.hit;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.turbodev.utils.Range;
import com.turbodev.jsymbol.TokenPosition;
import com.turbodev.jsymbol.util.HighlightableLabel;
import com.turbodev.jsymbol.util.NoAntiCacheImage;

public class TextHit extends QueryHit {

	private static final long serialVersionUID = 1L;
	
	private final String lineContent;
	
	public TextHit(String blobPath, String lineContent, TokenPosition hitPos) {
		super(blobPath, hitPos);
		
		this.lineContent = lineContent;
	}

	public String getLineContent() {
		return lineContent;
	}
	
	@Override
	public Component render(String componentId) {
		if (getTokenPos() != null) {
			return new HighlightableLabel(componentId, lineContent, 
					new Range(getTokenPos().getFromCh(), getTokenPos().getToCh()));
		} else {
			return new HighlightableLabel(componentId, lineContent, null);
		}
	}

	@Override
	public Image renderIcon(String componentId) {
		return new NoAntiCacheImage(componentId, new PackageResourceReference(FileHit.class, "bullet.gif"));
	}

	@Override
	public String getNamespace() {
		String fileName = getBlobPath();
		if (fileName.contains("/")) 
			fileName = StringUtils.substringAfterLast(fileName, "/");
		return fileName;
	}

}
