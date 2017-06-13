package com.gitplex.server.util.markdown;

import java.util.HashSet;
import java.util.Set;

import com.gitplex.server.util.PathUtils;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.html.CustomNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.LinkType;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
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
					            	String url = image.getUrl().toString();
					                ResolvedLink resolvedLink = context.resolveLink(LinkType.IMAGE, url, null);
					                html.attr("src", resolveUrl(baseUrl, url));
					                html.attr("alt", image.getText());
					                html.withAttr(resolvedLink);
					                html.tagVoid("img");
					            }
					            
					        }));
					        set.add(new NodeRenderingHandler<Link>(Link.class, new CustomNodeRenderer<Link>() {
					        	
					            @Override
					            public void render(Link link, NodeRendererContext context, HtmlWriter html) {
					            	String url = link.getUrl().toString();
					                ResolvedLink resolvedLink = context.resolveLink(LinkType.LINK, url, null);
					                html.attr("href", resolveUrl(baseUrl, url));
					                html.withAttr(resolvedLink);
					                html.tag("a").text(link.getText()).closeTag("a");
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
        	return PathUtils.normalize(PathUtils.resolveSibling(baseUrl, urlToResolve));
    	}
    }
    
}
