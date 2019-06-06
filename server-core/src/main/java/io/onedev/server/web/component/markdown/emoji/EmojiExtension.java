package io.onedev.server.web.component.markdown.emoji;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.DataKey;
import com.vladsch.flexmark.util.options.MutableDataHolder;

public class EmojiExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
	
    public static final DataKey<String> ATTR_ALIGN = new DataKey<String>("ATTR_ALIGN", "absmiddle");
    
    public static final DataKey<String> ATTR_IMAGE_SIZE = new DataKey<String>("ATTR_IMAGE_SIZE", "20");

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
