package io.onedev.server.web.component.svg;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;

public class SpriteImageResolver implements IComponentResolver {

	private static final long serialVersionUID = 1L;

	private static final String TAG_NAME = "svg";
	
	private static final String ATTR_HREF = "href";
	
	static {
	    WicketTagIdentifier.registerWellKnownTagName(TAG_NAME);
	}
	
	@Override
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag) {
		if (tag instanceof WicketTag && TAG_NAME.equalsIgnoreCase(tag.getName())) {
			WicketTag wtag = (WicketTag) tag;
			
			String id = "_" + TAG_NAME + "_" + container.getPage().getAutoIndex();
			String href = wtag.getAttribute(ATTR_HREF);
			SpriteImageWrapper wrapper = new SpriteImageWrapper(id, href);
			wrapper.setRenderBodyOnly(container.getApplication().getMarkupSettings().getStripWicketTags());
			container.autoAdd(wrapper, markupStream);
			
			for (String key: wtag.getAttributes().keySet()) {
				if (!key.equals(ATTR_HREF)) 
					wrapper.getImage().add(AttributeAppender.append(key, wtag.getAttribute(key)));
			}
			
			return wrapper;
		} else {
			return null;
		}
	}

}
