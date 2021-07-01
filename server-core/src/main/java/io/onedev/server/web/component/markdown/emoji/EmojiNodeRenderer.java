package io.onedev.server.web.component.markdown.emoji;

import java.util.HashSet;
import java.util.Set;

import com.vladsch.flexmark.html.CustomNodeRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.options.DataHolder;

public class EmojiNodeRenderer implements NodeRenderer {

    public EmojiNodeRenderer(DataHolder options) {
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
    	String emojiAlias = node.getText().toString();
    	
    	String emojiUnicode = Emojis.getInstance().getUnicode(emojiAlias);
    	if (emojiUnicode != null) {
    		html.text(emojiUnicode);
    	} else {
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
