package io.onedev.server.web.component.user.accesstoken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.AuditService;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.User;
import io.onedev.server.web.page.user.UserPage;

public abstract class AccessTokenListPanel extends Panel {

	private WebMarkupContainer container;

	private transient RevealedToken revealedToken;

	private record RevealedToken(Long id, String value) {
	}
	
	public AccessTokenListPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("tokens");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<>("tokens", new IModel<List<AccessToken>>() {

			@Override
			public List<AccessToken> getObject() {
				var tokens = new ArrayList<>(getUser().getAccessTokens());
				tokens.removeIf(it -> it.getOauthGrant() != null);
				Collections.sort(tokens);
				return tokens;
			}
			
		}) {

			private Component newViewer(String componentId, Long tokenId) {
				return new AccessTokenPanel(componentId) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						var token = getToken();
						getAccessTokenService().delete(token);
						if (getPage() instanceof UserPage) {
							var oldAuditContent = token.toAuditXml();
							getAuditService().audit(null, "deleted access token from account \"" + token.getOwner().getName() + "\"", oldAuditContent, null);
						}
						target.add(container);
					}

					@Override
					protected void onEdit(AjaxRequestTarget target) {
						AccessTokenEditPanel editor = new AccessTokenEditPanel("token") {

							private void view(AjaxRequestTarget target) {
								Component viewer = newViewer(componentId, tokenId);
								replaceWith(viewer);
								target.add(viewer);
							}

							@Override
							protected AccessToken getToken() {
								return getAccessTokenService().load(tokenId);
							}

							@Override
							protected void onSaved(AjaxRequestTarget target, Long savedTokenId, @Nullable String newValue) {
								view(target);
							}

							@Override
							protected void onCancelled(AjaxRequestTarget target) {
								view(target);
							}

						};
						replaceWith(editor);
						target.add(editor);
					}

					@Override
					protected void onRegenerated(AjaxRequestTarget target, String value) {
						showRevealedToken(target, this, tokenId, value);
					}

					@Override
					protected String getRevealedValue() {
						if (revealedToken != null && tokenId.equals(revealedToken.id()))
							return revealedToken.value();
						else
							return null;
					}

					@Override
					protected AccessToken getToken() {
						return getAccessTokenService().load(tokenId);
					}

				};
			}
			
			@Override
			protected void populateItem(final ListItem<AccessToken> item) {
				item.add(newViewer("token", item.getModelObject().getId()));
			}

		});
		
		container.add(newAddNewFrag());
	}

	private Component newAddNewFrag() {
		Fragment fragment = new Fragment("newToken", "addNewLinkFrag", this);
		fragment.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Component editor = new AccessTokenEditPanel("newToken") {

					@Override
					protected AccessToken getToken() {
						var token = new AccessToken();
						token.setOwner(getUser());
						return token;
					}

					@Override
					protected void onSaved(AjaxRequestTarget target, Long tokenId, @Nullable String newValue) {
						container.replace(newAddNewFrag());
						showRevealedToken(target, container, tokenId, newValue);
					}

					@Override
					protected void onCancelled(AjaxRequestTarget target) {
						Component newAddNewFrag = newAddNewFrag();
						container.replace(newAddNewFrag);
						target.add(newAddNewFrag);
					}

				};
				container.replace(editor);
				target.add(editor);
			}

		});
		fragment.setOutputMarkupId(true);
		return fragment;
	}

	private void showRevealedToken(AjaxRequestTarget target, Component component, Long tokenId, String value) {
		revealedToken = new RevealedToken(tokenId, value);
		target.add(component);
		target.appendJavaScript(String.format(
				"document.getElementById('%s').querySelector('.access-token-notice')?.closest('.access-token-value').scrollIntoView({block: 'nearest'});",
				component.getMarkupId()));
	}

	@Override
	protected void onDetach() {
		revealedToken = null;
		super.onDetach();
	}
	
	protected abstract User getUser();
	
	private AccessTokenService getAccessTokenService() {
		return OneDev.getInstance(AccessTokenService.class);
	}
	
	private AuditService getAuditService() {
		return OneDev.getInstance(AuditService.class);
	}
	
}
