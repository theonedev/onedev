package com.turbodev.server.web;

import java.util.Collection;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Application;
import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleStateException;
import org.hibernate.exception.ConstraintViolationException;

import com.turbodev.launcher.loader.AbstractPlugin;
import com.turbodev.launcher.loader.AbstractPluginModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.turbodev.server.git.exception.GitException;
import com.turbodev.server.manager.UrlManager;
import com.turbodev.server.util.markdown.MarkdownProcessor;
import com.turbodev.server.util.reviewrequirement.InvalidReviewRuleException;
import com.turbodev.server.web.component.diff.DiffRenderer;
import com.turbodev.server.web.component.markdown.SourcePositionTrackExtension;
import com.turbodev.server.web.component.markdown.emoji.EmojiExtension;
import com.turbodev.server.web.editable.DefaultEditSupportRegistry;
import com.turbodev.server.web.editable.EditSupport;
import com.turbodev.server.web.editable.EditSupportLocator;
import com.turbodev.server.web.editable.EditSupportRegistry;
import com.turbodev.server.web.page.project.blob.render.BlobRendererContribution;
import com.turbodev.server.web.util.avatar.AvatarManager;
import com.turbodev.server.web.util.avatar.DefaultAvatarManager;
import com.turbodev.server.web.util.commitmessagetransform.CommitMessageTransformer;
import com.turbodev.server.web.util.commitmessagetransform.PatternCommitMessageTransformer;
import com.turbodev.server.web.util.markdown.MentionProcessor;
import com.turbodev.server.web.util.markdown.PullRequestProcessor;
import com.turbodev.server.web.util.markdown.RelativeUrlProcessor;
import com.turbodev.server.web.websocket.CodeCommentChangeBroadcaster;
import com.turbodev.server.web.websocket.CommitIndexedBroadcaster;
import com.turbodev.server.web.websocket.DefaultWebSocketManager;
import com.turbodev.server.web.websocket.PullRequestChangeBroadcaster;
import com.turbodev.server.web.websocket.TaskChangeBroadcaster;
import com.turbodev.server.web.websocket.WebSocketManager;
import com.vladsch.flexmark.Extension;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class WebModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(WicketServlet.class).to(DefaultWicketServlet.class);
		bind(WicketFilter.class).to(DefaultWicketFilter.class);
		bind(WebSocketPolicy.class).toInstance(WebSocketPolicy.newServerPolicy());
		bind(EditSupportRegistry.class).to(DefaultEditSupportRegistry.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);

		contribute(CommitMessageTransformer.class, PatternCommitMessageTransformer.class);
		
		contributeFromPackage(EditSupport.class, EditSupport.class);
		
		bind(WebApplication.class).to(TurboDevWebApplication.class);
		bind(Application.class).to(TurboDevWebApplication.class);
		bind(AvatarManager.class).to(DefaultAvatarManager.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);
		
		bind(CommitIndexedBroadcaster.class);
		
		contributeFromPackage(DiffRenderer.class, DiffRenderer.class);
		contributeFromPackage(BlobRendererContribution.class, BlobRendererContribution.class);

		contribute(Extension.class, new EmojiExtension());
		contribute(Extension.class, new SourcePositionTrackExtension());
		
		contribute(MarkdownProcessor.class, new MentionProcessor());
		contribute(MarkdownProcessor.class, new PullRequestProcessor());
		contribute(MarkdownProcessor.class, new RelativeUrlProcessor());

		contribute(ResourcePackScopeContribution.class, new ResourcePackScopeContribution() {
			
			@Override
			public Collection<Class<?>> getResourcePackScopes() {
				return Lists.newArrayList(WebModule.class);
			}
			
		});
		contribute(ExpectedExceptionContribution.class, new ExpectedExceptionContribution() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Collection<Class<? extends Exception>> getExpectedExceptionClasses() {
				return Sets.newHashSet(ConstraintViolationException.class, EntityNotFoundException.class, 
						ObjectNotFoundException.class, StaleStateException.class, UnauthorizedException.class, 
						GitException.class, PageExpiredException.class, StalePageException.class, 
						InvalidReviewRuleException.class);
			}
			
		});

		bind(UrlManager.class).to(DefaultUrlManager.class);
		bind(CodeCommentChangeBroadcaster.class);
		bind(PullRequestChangeBroadcaster.class);
		bind(TaskChangeBroadcaster.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return WebPlugin.class;
	}
	
}
