package io.onedev.server.web.opengraph;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;

public class OpenGraphHeaderMeta {
	private OpenGraphHeaderMetaType type;
	private String content;
	
	public OpenGraphHeaderMeta(OpenGraphHeaderMetaType type, String content) {
		this.type = type;
		this.content = content;
	}

	public void render(IHeaderResponse response) {
		response.render(MetaDataHeaderItem.forMetaTag(type.toString(), content).addTagAttribute("property", type.toString()));
	}
}
