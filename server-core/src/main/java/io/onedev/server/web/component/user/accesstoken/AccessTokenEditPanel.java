package io.onedev.server.web.component.user.accesstoken;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AccessTokenAuthorizationManager;
import io.onedev.server.entitymanager.AccessTokenManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.ArrayList;

abstract class AccessTokenEditPanel extends Panel {
	
	public AccessTokenEditPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");

		var token = getToken();
		var bean = AccessTokenEditBean.of(token);
		
		var editor = BeanContext.edit("editor", bean, Sets.newHashSet("value"), true);
		form.add(editor);
			
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				var token = getToken();
				
				var nameConflict = false;
				var tokenWithSameName = getTokenManager().findByOwnerAndName(token.getOwner(), bean.getName());
				if (token.isNew()) {
					if (tokenWithSameName != null)
						nameConflict = true;
				} else {
					if (tokenWithSameName != null && !tokenWithSameName.equals(token))
						nameConflict = true;						
				}
				if (nameConflict) {
					editor.error(new Path(new PathNode.Named("name")), "Name already used by another access token of the owner");
					target.add(AccessTokenEditPanel.this);
				} else {
					token.setName(bean.getName());
					token.setValue(bean.getValue());
					token.setHasOwnerPermissions(bean.isHasOwnerPermissions());

					var authorizations = new ArrayList<AccessTokenAuthorization>();
					for (var authorizationBean : bean.getAuthorizations()) {
						var authorization = new AccessTokenAuthorization();
						authorization.setProject(getProjectManager().findByPath(authorizationBean.getProjectPath()));
						authorization.setRole(getRoleManager().find(authorizationBean.getRoleName()));
						authorization.setToken(token);
						authorizations.add(authorization);
					}
					token.setExpireDate(bean.getExpireDate());

					getTransactionManager().run(() -> {
						getTokenManager().createOrUpdate(token);
						getAuthorizationManager().syncAuthorizations(token, authorizations);
					});
					onSaved(target);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		add(form);
		
		setOutputMarkupId(true);
	}
	
	private TransactionManager getTransactionManager() {
		return OneDev.getInstance(TransactionManager.class);
	}
	
	private AccessTokenManager getTokenManager() {
		return OneDev.getInstance(AccessTokenManager.class);
	}
	
	private AccessTokenAuthorizationManager getAuthorizationManager() {
		return OneDev.getInstance(AccessTokenAuthorizationManager.class);
	}
	
	private RoleManager getRoleManager() {
		return OneDev.getInstance(RoleManager.class);
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	protected abstract AccessToken getToken();
	
	protected abstract void onSaved(AjaxRequestTarget target);
	
	protected abstract void onCancelled(AjaxRequestTarget target);
}
