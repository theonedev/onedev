package io.onedev.server.web.component.markdown.emoji;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class EmojiExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
	
    @Override
    public void rendererOptions(final MutableDataHolder options) {
    }

    @Override
    public void parserOptions(final MutableDataHolder options) {
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new EmojiDelimiterProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
    	rendererBuilder.nodeRendererFactory(new EmojiNodeRenderer.Factory());
    }
    
}
