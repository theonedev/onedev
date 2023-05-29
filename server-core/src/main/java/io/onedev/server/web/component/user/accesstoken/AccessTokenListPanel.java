package io.onedev.server.web.component.user.accesstoken;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.model.support.AccessToken;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import java.util.List;

@SuppressWarnings("serial")
public abstract class AccessTokenListPanel extends Panel {

	private WebMarkupContainer container;
	
	public AccessTokenListPanel(String id) {
		super(id);
	}

	protected abstract User getUser();
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		container = new WebMarkupContainer("accessTokens");
		container.setOutputMarkupId(true);
		add(container);
		container.add(new ListView<>("accessTokens", new AbstractReadOnlyModel<List<AccessToken>>() {

			@Override
			public List<AccessToken> getObject() {
				return getUser().getAccessTokens();
			}
		}) {

			private Component newViewer(String componentId, int index, AccessToken accessToken) {
				return new AccessTokenPanel(componentId, accessToken) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						getUser().getAccessTokens().remove(index);
						getUserManager().update(getUser(), null);
						target.add(container);
					}

					@Override
					protected void onEdit(AjaxRequestTarget target) {
						AccessTokenEditPanel editor = new AccessTokenEditPanel("accessToken", accessToken) {

							private void view(AjaxRequestTarget target) {
								Component viewer = newViewer(componentId, index, accessToken);
								replaceWith(viewer);
								target.add(viewer);
							}
							
							@Override
							protected void onSave(AjaxRequestTarget target, AccessToken accessToken) {
								getUser().getAccessTokens().set(index, accessToken);
								getUserManager().update(getUser(), null);
								view(target);
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								view(target);
							}

						};
						replaceWith(editor);
						target.add(editor);
					}

				};
			}
			
			@Override
			protected void populateItem(final ListItem<AccessToken> item) {
				item.add(newViewer("accessToken", item.getIndex(), item.getModelObject()));
			}

		});
		
		container.add(newAddNewFrag());
	}

	private Component newAddNewFrag() {
		Fragment fragment = new Fragment("newAccessToken", "addNewLinkFrag", this);
		fragment.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Component editor = new AccessTokenEditPanel("newAccessToken", new AccessToken()) {

					@Override
					protected void onSave(AjaxRequestTarget target, AccessToken accessToken) {
						getUser().getAccessTokens().add(accessToken);
						getUserManager().update(getUser(), null);
						container.replace(newAddNewFrag());
						target.add(container);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
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
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
}
