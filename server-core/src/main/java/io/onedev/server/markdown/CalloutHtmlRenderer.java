package io.onedev.server.markdown;

import java.util.HashSet;
import java.util.Set;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;

import io.onedev.server.web.component.svg.SpriteImage;

/**
 * HTML renderer for callout blocks.
 * 
 * Renders callouts as styled div elements with appropriate CSS classes and icons.
 */
public class CalloutHtmlRenderer implements NodeRenderer {
    
    public CalloutHtmlRenderer(DataHolder options) {
        // Constructor for options if needed
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(CalloutNode.class, this::render));
        return set;
    }

    private void render(CalloutNode node, NodeRendererContext context, HtmlWriter html) {
        String calloutType = node.getCalloutTypeString();
        String calloutTitle = node.getCalloutTitleString();
        
        // Get CSS class and icon for the callout type
        String cssClass = getCalloutCssClass(calloutType);
        String icon = getCalloutIcon(calloutType);
        
        html.line();
        html.withAttr().attr("class", "callout callout-" + calloutType + " " + cssClass).tag("div");
        html.line();
        
        // Render header with icon and title
        html.withAttr().attr("class", "callout-header h4").tag("div");
        if (icon != null) {
            html.withAttr().attr("class", "callout-icon mr-2").tag("span");
            html.raw("<svg class='icon icon-lg'><use xlink:href='" + SpriteImage.getVersionedHref(icon) + "'/></svg>");
            html.tag("/span");
        }
        html.withAttr().attr("class", "callout-title").tag("span");
        html.text(calloutTitle);
        html.tag("/span");
        html.tag("/div");
        html.line();
        
        // Render content
        html.withAttr().attr("class", "callout-content").tag("div");
        context.renderChildren(node);
        html.tag("/div");
        html.line();
        
        html.tag("/div");
        html.line();
    }
    
    private String getCalloutCssClass(String type) {
        switch (type) {
            case "note":
                return "alert alert-light-primary alert-notice";
            case "tip":
                return "alert alert-light-success alert-notice";
            case "important":
                return "alert alert-light-info alert-notice";
            case "warning":
                return "alert alert-light-warning alert-notice";
            case "caution":
                return "alert alert-light-danger alert-notice";
            default:
                return "alert alert-light alert-notice";
        }
    }
    
    private String getCalloutIcon(String type) {
        switch (type) {
            case "note":
                return "info";
            case "tip":
                return "bulb";
            case "important":
                return "bell-ring";
            case "warning":
                return "warning";
            case "caution":
                return "exclamation-circle";
            default:
                return "hand";
        }
    }

    public static class Factory implements NodeRendererFactory {
        @Override
        public NodeRenderer apply(DataHolder options) {
            return new CalloutHtmlRenderer(options);
        }
    }
}
