package com.gitplex.server.search.hit;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.symbolextractor.Range;
import com.gitplex.symbolextractor.TokenPosition;
import com.gitplex.symbolextractor.util.HighlightableLabel;
import com.gitplex.symbolextractor.util.NoAntiCacheImage;

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
		return new HighlightableLabel(componentId, lineContent, 
				new Range(getTokenPos().getFromCh(), getTokenPos().getToCh()));
	}

	@Override
	public Image renderIcon(String componentId) {
		return new NoAntiCacheImage(componentId, new PackageResourceReference(FileHit.class, "bullet.gif"));
	}

	@Override
	public String getScope() {
		String fileName = getBlobPath();
		if (fileName.contains("/")) 
			fileName = StringUtils.substringAfterLast(fileName, "/");
		return fileName;
	}

	@Override
	protected int score() {
		return lineContent.length();
	}

}
