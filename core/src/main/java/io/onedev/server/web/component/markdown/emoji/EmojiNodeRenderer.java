package io.onedev.server.web.component.markdown.emoji;

import com.vladsch.flexmark.html.CustomNodeRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.options.DataHolder;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

public class EmojiNodeRenderer implements NodeRenderer {
    private final String attrImageSize;
    private final String attrAlign;

    public EmojiNodeRenderer(DataHolder options) {
        this.attrImageSize = options.get(EmojiExtension.ATTR_IMAGE_SIZE);
        this.attrAlign = options.get(EmojiExtension.ATTR_ALIGN);
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<NodeRenderingHandler<?>>();
        set.add(new NodeRenderingHandler<EmojiNode>(EmojiNode.class, new CustomNodeRenderer<EmojiNode>() {
            @Override
            public void render(EmojiNode node, NodeRendererContext context, HtmlWriter html) {
                EmojiNodeRenderer.this.render(node, context, html);
            }
        }));
        return set;
    }

    private void render(EmojiNode node, NodeRendererContext context, HtmlWriter html) {
    	String emojiName = node.getText().toString();
    	String emojiUrl = null;
		if (RequestCycle.get() != null) {
			String emojiCode = EmojiOnes.getInstance().all().get(emojiName);
			if (emojiCode != null) {
				emojiUrl = RequestCycle.get().urlFor(new PackageResourceReference(
						EmojiOnes.class, "icon/" + emojiCode + ".png"), new PageParameters()).toString();
			} 
		}

		if (emojiUrl != null) {
            ResolvedLink resolvedLink = context.resolveLink(LinkType.IMAGE, emojiUrl, null);
            html.attr("src", emojiUrl);
            html.attr("alt", "emoji " + emojiName);
            if (!attrImageSize.isEmpty()) 
            	html.attr("height", attrImageSize).attr("width", attrImageSize);
            if (!attrAlign.isEmpty()) 
            	html.attr("align", attrAlign);
            html.withAttr(resolvedLink);
            html.tagVoid("img");
		} else {
            // output as text
            html.text(":");
            context.renderChildren(node);
            html.text(":");
		}
		
    }

    public static class Factory implements NodeRendererFactory {
        @Override
        public NodeRenderer create(final DataHolder options) {
            return new EmojiNodeRenderer(options);
        }
    }
}
