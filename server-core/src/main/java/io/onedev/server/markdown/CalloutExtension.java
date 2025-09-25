package io.onedev.server.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.misc.Extension;

/**
 * Flexmark extension to support GitHub Flavored Markdown (GFM) callouts.
 * 
 * Supports callout syntax like:
 * > [!NOTE]
 * > This is a note
 * 
 * > [!WARNING]
 * > This is a warning
 */
public class CalloutExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    
    private CalloutExtension() {
    }

    public static Extension create() {
        return new CalloutExtension();
    }

    @Override
    public void rendererOptions(MutableDataHolder options) {
        // No renderer options needed
    }

    @Override
    public void parserOptions(MutableDataHolder options) {
        // No parser options needed
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new CalloutBlockParser.Factory());
    }

    @Override
    public void extend(HtmlRenderer.Builder htmlRendererBuilder, String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new CalloutHtmlRenderer.Factory());
        }
    }
}
