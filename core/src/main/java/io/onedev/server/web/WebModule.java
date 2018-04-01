package io.onedev.server.web;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vladsch.flexmark.Extension;

import groovy.lang.GroovyRuntimeException;
import io.onedev.launcher.loader.AbstractPlugin;
import io.onedev.launcher.loader.AbstractPluginModule;
import io.onedev.server.git.exception.GitException;
import io.onedev.server.manager.UrlManager;
import io.onedev.server.util.markdown.MarkdownProcessor;
import io.onedev.server.util.reviewrequirement.InvalidReviewRuleException;
import io.onedev.server.web.component.diff.DiffRenderer;
import io.onedev.server.web.component.markdown.SourcePositionTrackExtension;
import io.onedev.server.web.component.markdown.emoji.EmojiExtension;
import io.onedev.server.web.editable.DefaultEditSupportRegistry;
import io.onedev.server.web.editable.EditSupport;
import io.onedev.server.web.editable.EditSupportLocator;
import io.onedev.server.web.editable.EditSupportRegistry;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;
import io.onedev.server.web.util.avatar.AvatarManager;
import io.onedev.server.web.util.avatar.DefaultAvatarManager;
import io.onedev.server.web.util.commitmessagetransform.CommitMessageTransformer;
import io.onedev.server.web.util.commitmessagetransform.PatternCommitMessageTransformer;
import io.onedev.server.web.util.markdown.MentionProcessor;
import io.onedev.server.web.util.markdown.PullRequestProcessor;
import io.onedev.server.web.util.markdown.RelativeUrlProcessor;
import io.onedev.server.web.websocket.CodeCommentChangeBroadcaster;
import io.onedev.server.web.websocket.CommitIndexedBroadcaster;
import io.onedev.server.web.websocket.DefaultWebSocketManager;
import io.onedev.server.web.websocket.PullRequestChangeBroadcaster;
import io.onedev.server.web.websocket.TaskChangeBroadcaster;
import io.onedev.server.web.websocket.WebSocketManager;

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
		
		bind(WebApplication.class).to(OneWebApplication.class);
		bind(Application.class).to(OneWebApplication.class);
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
						InvalidReviewRuleException.class, GroovyRuntimeException.class);
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
