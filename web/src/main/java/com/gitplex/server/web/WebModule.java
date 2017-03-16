package com.gitplex.server.web;

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

import com.gitplex.launcher.loader.AbstractPlugin;
import com.gitplex.launcher.loader.AbstractPluginModule;
import com.gitplex.server.git.exception.GitException;
import com.gitplex.server.manager.UrlManager;
import com.gitplex.server.util.markdown.HtmlTransformer;
import com.gitplex.server.util.validation.AccountNameReservation;
import com.gitplex.server.util.validation.DepotNameReservation;
import com.gitplex.server.util.validation.TeamNameReservation;
import com.gitplex.server.web.behavior.markdown.SourcePositionTrackExtension;
import com.gitplex.server.web.behavior.markdown.emoji.EmojiExtension;
import com.gitplex.server.web.component.comment.MentionTransformer;
import com.gitplex.server.web.component.comment.PullRequestTransformer;
import com.gitplex.server.web.component.diff.DiffRenderer;
import com.gitplex.server.web.editable.DefaultEditSupportRegistry;
import com.gitplex.server.web.editable.EditSupport;
import com.gitplex.server.web.editable.EditSupportLocator;
import com.gitplex.server.web.editable.EditSupportRegistry;
import com.gitplex.server.web.page.depot.blob.render.BlobRendererContribution;
import com.gitplex.server.web.util.avatar.AvatarManager;
import com.gitplex.server.web.util.avatar.DefaultAvatarManager;
import com.gitplex.server.web.util.commitmessagetransform.CommitMessageTransformer;
import com.gitplex.server.web.util.commitmessagetransform.PatternCommitMessageTransformer;
import com.gitplex.server.web.websocket.CodeCommentChangeBroadcaster;
import com.gitplex.server.web.websocket.CommitIndexedBroadcaster;
import com.gitplex.server.web.websocket.DefaultWebSocketManager;
import com.gitplex.server.web.websocket.PullRequestChangeBroadcaster;
import com.gitplex.server.web.websocket.TaskChangeBroadcaster;
import com.gitplex.server.web.websocket.WebSocketManager;
import com.google.common.collect.Lists;
import com.vladsch.flexmark.Extension;

import jersey.repackaged.com.google.common.collect.Sets;

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
		
		bind(WebApplication.class).to(GitPlexWebApplication.class);
		bind(Application.class).to(GitPlexWebApplication.class);
		bind(AvatarManager.class).to(DefaultAvatarManager.class);
		bind(WebSocketManager.class).to(DefaultWebSocketManager.class);
		
		contribute(AccountNameReservation.class, WebAccountNameReservation.class);
		contribute(DepotNameReservation.class, WebDepotNameReservation.class);
		contribute(TeamNameReservation.class, WebTeamNameReservation.class);
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);
		
		bind(CommitIndexedBroadcaster.class);
		
		contributeFromPackage(DiffRenderer.class, DiffRenderer.class);
		contributeFromPackage(BlobRendererContribution.class, BlobRendererContribution.class);

		contribute(Extension.class, new EmojiExtension());
		contribute(Extension.class, new SourcePositionTrackExtension());
		contribute(HtmlTransformer.class, new MentionTransformer());
		contribute(HtmlTransformer.class, new PullRequestTransformer());

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
						GitException.class, PageExpiredException.class, StalePageException.class);
			}
			
		});

		bind(UrlManager.class).to(WicketUrlManager.class);
		bind(CodeCommentChangeBroadcaster.class);
		bind(PullRequestChangeBroadcaster.class);
		bind(TaskChangeBroadcaster.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return WebPlugin.class;
	}
	
}
