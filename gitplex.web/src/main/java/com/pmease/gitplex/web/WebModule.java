package com.pmease.gitplex.web;

import java.util.Collection;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.pegdown.Parser;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import com.google.common.collect.Lists;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.markdown.extensionpoint.HtmlTransformer;
import com.pmease.commons.markdown.extensionpoint.MarkdownExtension;
import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.core.util.validation.AccountNameReservation;
import com.pmease.gitplex.core.util.validation.DepotNameReservation;
import com.pmease.gitplex.core.util.validation.TeamNameReservation;
import com.pmease.gitplex.search.IndexListener;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.avatar.DefaultAvatarManager;
import com.pmease.gitplex.web.component.comment.MentionTransformer;
import com.pmease.gitplex.web.component.comment.PullRequestTransformer;
import com.pmease.gitplex.web.component.diff.DiffRenderer;
import com.pmease.gitplex.web.component.repofile.blobview.BlobRenderer;
import com.pmease.gitplex.web.editable.EditSupportLocator;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.websocket.PullRequestChangeBroadcaster;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class WebModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		bind(AbstractWicketConfig.class).to(WicketConfig.class);		
		bind(WebApplication.class).to(WicketConfig.class);
		bind(Application.class).to(WicketConfig.class);
		bind(AvatarManager.class).to(DefaultAvatarManager.class);
		
		contribute(ServletConfigurator.class, WebServletConfigurator.class);
		contribute(AccountNameReservation.class, WebAccountNameReservation.class);
		contribute(DepotNameReservation.class, WebDepotNameReservation.class);
		contribute(TeamNameReservation.class, WebTeamNameReservation.class);
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);
		
		contribute(PullRequestListener.class, PullRequestChangeBroadcaster.class);
		contribute(IndexListener.class, DepotFilePage.IndexedListener.class);
		
		contributeFromPackage(DiffRenderer.class, DiffRenderer.class);
		contributeFromPackage(BlobRenderer.class, BlobRenderer.class);
		
		contribute(MarkdownExtension.class, new MarkdownExtension() {
			
			@Override
			public Collection<Class<? extends Parser>> getInlineParsers() {
				return null;
			}
			
			@Override
			public Collection<ToHtmlSerializerPlugin> getHtmlSerializers() {
				return null;
			}
			
			@Override
			public Collection<Class<? extends Parser>> getBlockParsers() {
				return null;
			}

			@Override
			public Collection<HtmlTransformer> getHtmlTransformers() {
				return Lists.newArrayList(new MentionTransformer(), new PullRequestTransformer());
			}
			
		});

		bind(UrlManager.class).to(WebUrlManager.class);
	}
	
}
