package io.onedev.server.web.component.markdown;

import com.vladsch.flexmark.ast.Block;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.util.html.Attributes;
import com.vladsch.flexmark.util.options.MutableDataHolder;

public class SourcePositionTrackExtension implements HtmlRenderer.HtmlRendererExtension {

	public static final String DATA_START_ATTRIBUTE = "sourcestart";

	public static final String DATA_END_ATTRIBUTE = "sourceend";
	
	@Override
	public void rendererOptions(MutableDataHolder options) {
	}

	@Override
	public void extend(Builder rendererBuilder, String rendererType) {
		rendererBuilder.attributeProviderFactory(new IndependentAttributeProviderFactory() {
			
			@Override
			public AttributeProvider create(NodeRendererContext context) {
				return new AttributeProvider() {

					@Override
					public void setAttributes(Node node, AttributablePart part, Attributes attributes) {
						if (node instanceof Block) {
							int startOffset = node.getStartOffset();
							int endOffset = node.getEndOffset();
							Node document = node.getDocument();
							if (document != null) {
								int leadingWhitespaces = 0;
								for (int i=startOffset; i<endOffset; i++) {
									if (Character.isWhitespace(document.getChars().charAt(i))) {
										leadingWhitespaces++;
									} else {
										break;
									}
								}
								int trailingWhitespaces = 0;
								for (int i=endOffset-1; i>=startOffset; i--) {
									if (Character.isWhitespace(document.getChars().charAt(i))) {
										trailingWhitespaces++;
									} else {
										break;
									}
								}
								attributes.addValue("data-" + DATA_START_ATTRIBUTE, String.valueOf(startOffset+leadingWhitespaces));
								attributes.addValue("data-" + DATA_END_ATTRIBUTE, String.valueOf(endOffset-trailingWhitespaces));
							}
						}
					}
					
				};
			}
		});
	}

}
