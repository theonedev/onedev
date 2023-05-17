package io.onedev.server.ee.xsearch.match;

import io.onedev.commons.utils.PlanarRange;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;

import java.io.Serializable;

public interface ContentMatch extends Serializable {

	Image renderIcon(String componentId);

	Component render(String componentId);
	
	PlanarRange getPosition();
	
}
