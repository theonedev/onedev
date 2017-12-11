package com.gitplex.server.util.markdown;

import java.util.HashSet;
import java.util.Set;

import com.gitplex.utils.PathUtils;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.util.TextCollectingVisitor;
import com.vladsch.flexmark.html.CustomNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.LinkType;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.html.Escaping;
import com.vladsch.flexmark.util.options.DataHolder;
import com.vladsch.flexmark.util.options.DataKey;
import com.vladsch.flexmark.util.options.MutableDataHolder;

public class UrlResolveExtension implements HtmlRenderer.HtmlRendererExtension {
	
    public static final DataKey<String> BASE_URL = new DataKey<>("BASE_URL", (String)null);
    
    public void rendererOptions(MutableDataHolder options) {
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
    	rendererBuilder.nodeRendererFactory(new NodeRendererFactory() {
			
			@Override
			public NodeRenderer create(DataHolder options) {
	            return new NodeRenderer() {

					@Override
					public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
				        Set<NodeRenderingHandler<?>> set = new HashSet<NodeRenderingHandler<?>>();
						String baseUrl = options.get(BASE_URL);
						if (baseUrl != null) {
					        set.add(new NodeRenderingHandler<Image>(Image.class, new CustomNodeRenderer<Image>() {
					        	
					            @Override
					            public void render(Image image, NodeRendererContext context, HtmlWriter html) {
					                if (!context.isDoNotRenderLinks()) {
					                    String altText = new TextCollectingVisitor().collectAndGetText(image);

					                    ResolvedLink resolvedLink = context.resolveLink(LinkType.IMAGE, image.getUrl().unescape(), null);
					                    String url = resolvedLink.getUrl();

					                    if (!image.getUrlContent().isEmpty()) {
					                        // reverse URL encoding of =, &
					                        String content = Escaping.percentEncodeUrl(image.getUrlContent()).replace("+", "%2B").replace("%3D", "=").replace("%26", "&amp;");
					                        url += content;
					                    }

					                    String resolvedUrl = resolveUrl(baseUrl, url); 
						                html.attr("src", resolvedUrl);
					                    html.attr("data-resolved", String.valueOf(!resolvedUrl.equals(url)));
					                    html.attr("alt", altText);
					                    if (image.getTitle().isNotNull()) {
					                        html.attr("title", image.getTitle().unescape());
					                    }
					                    html.srcPos(image.getChars()).withAttr(resolvedLink).tagVoid("img");
					                }
					            }
					            
					        }));
					        set.add(new NodeRenderingHandler<Link>(Link.class, new CustomNodeRenderer<Link>() {
					        	
					            @Override
					            public void render(Link link, NodeRendererContext context, HtmlWriter html) {
					                if (context.isDoNotRenderLinks()) {
					                    context.renderChildren(link);
					                } else {
					                    ResolvedLink resolvedLink = 
					                    		context.resolveLink(LinkType.LINK, link.getUrl().unescape(), null);

					                    String resolvedUrl = resolveUrl(baseUrl, resolvedLink.getUrl());
					                    html.attr("href", resolvedUrl);
					                    html.attr("data-resolved", String.valueOf(!resolvedUrl.equals(resolvedLink.getUrl())));
					                    if (link.getTitle().isNotNull()) {
					                        html.attr("title", link.getTitle().unescape());
					                    }
					                    html.srcPos(link.getChars()).withAttr(resolvedLink).tag("a");
					                    context.renderChildren(link);
					                    html.tag("/a");
					                }
					            }
					            
					        }));
						}
				        
				        return set;
					}
	            	
	            };
			}
			
		});
    }
    
    private String resolveUrl(String baseUrl, String urlToResolve) {
    	if (urlToResolve.contains(":") || urlToResolve.startsWith("/") || urlToResolve.startsWith("#")) {
    		return urlToResolve;
    	} else {
        	return PathUtils.normalizeDots(PathUtils.resolve(baseUrl, urlToResolve));
    	}
    }
    
}
