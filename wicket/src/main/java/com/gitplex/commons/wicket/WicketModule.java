package com.gitplex.commons.wicket;

import java.util.Collection;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.pegdown.Parser;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.gitplex.calla.loader.AbstractPlugin;
import com.gitplex.calla.loader.AbstractPluginModule;
import com.gitplex.commons.markdown.extensionpoint.HtmlTransformer;
import com.gitplex.commons.markdown.extensionpoint.MarkdownExtension;
import com.gitplex.commons.wicket.behavior.markdown.EmojiTransformer;
import com.gitplex.commons.wicket.editable.DefaultEditSupportRegistry;
import com.gitplex.commons.wicket.editable.EditSupport;
import com.gitplex.commons.wicket.editable.EditSupportRegistry;
import com.gitplex.commons.wicket.websocket.DefaultWebSocketManager;
import com.gitplex.commons.wicket.websocket.WebSocketManager;
import com.google.common.collect.Lists;
import com.gitplex.commons.jetty.ServletConfigurator;

public class WicketModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(WicketServlet.class).to(DefaultWicketServlet.class);
		bind(WicketFilter.class).to(DefaultWicketFilter.class);
		bind(WebSocketPolicy.class).toInstance(WebSocketPolicy.newServerPolicy());
		bind(EditSupportRegistry.class).to(DefaultEditSupportRegistry.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);

		contribute(ServletConfigurator.class, WicketServletConfigurator.class);
		
		contributeFromPackage(EditSupport.class, EditSupport.class);
		
		contribute(MarkdownExtension.class, new MarkdownExtension() {
			
			@Override
			public Collection<Class<? extends Parser>> getInlineParsers() {
				return null;
			}
			
			@Override
			public Collection<HtmlTransformer> getHtmlTransformers() {
				return Lists.newArrayList((HtmlTransformer)new EmojiTransformer());
			}
			
			@Override
			public Collection<ToHtmlSerializerPlugin> getHtmlSerializers() {
				return null;
			}
			
			@Override
			public Collection<Class<? extends Parser>> getBlockParsers() {
				return null;
			}
		});
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return WicketPlugin.class;
	}

}
