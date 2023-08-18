package io.onedev.server.ee.xsearch.match;

import io.onedev.commons.jsymbol.util.HighlightableLabel;
import io.onedev.commons.jsymbol.util.NoAntiCacheImage;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

public class TextMatch implements ContentMatch {

	private static final long serialVersionUID = 1L;

	private final PlanarRange position;
	
	private final String line;
	
	public TextMatch(PlanarRange position, String line) {
		this.position = position;
		this.line = line;
	}

	@Override
	public Component render(String componentId) {
		return new HighlightableLabel(componentId, line,
				new LinearRange(position.getFromColumn(), position.getToColumn()));
	}

	@Override
	public Image renderIcon(String componentId) {
		return new NoAntiCacheImage(componentId, new PackageResourceReference(TextMatch.class, "bullet.png"));
	}

	public PlanarRange getPosition() {
		return position;
	}

}
