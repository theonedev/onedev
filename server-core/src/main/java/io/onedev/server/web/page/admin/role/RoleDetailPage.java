package io.onedev.server.web.page.admin.role;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public class RoleDetailPage extends AdministrationPage {
	
	private static final String PARAM_ROLE = "role";
	
	protected final IModel<Role> roleModel;
	
	private String oldName;
	
	private BeanEditor editor;
	
	public RoleDetailPage(PageParameters params) {
		super(params);
		
		String roleIdString = params.get(PARAM_ROLE).toString();
		if (StringUtils.isBlank(roleIdString))
			throw new RestartResponseException(RoleListPage.class);
		
		roleModel = new LoadableDetachableModel<Role>() {

			@Override
			protected Role load() {
				return OneDev.getInstance(RoleManager.class).load(Long.valueOf(roleIdString));
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getRole();
			}

			@Override
			public void setObject(Serializable object) {
				oldName = getRole().getName();
				editor.getDescriptor().copyProperties(object, getRole());
			}
			
		}, getRole().isManager()?Sets.newHashSet("name"):Sets.newHashSet(), !getRole().isManager());
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Role role = getRole();
				RoleManager roleManager = OneDev.getInstance(RoleManager.class);
				Role roleWithSameName = roleManager.find(role.getName());
				if (roleWithSameName != null && !roleWithSameName.equals(role)) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another role.");
				} 
				if (editor.isValid()) {
					roleManager.save(role, oldName);
					setResponsePage(RoleDetailPage.class, RoleDetailPage.paramsOf(role));
					Session.get().success("Role updated");
				}
			}
			
		};	
		
		if (getRole().isManager())
			form.add(AttributeAppender.append("class", "manager"));
		
		form.add(editor);
		form.add(new FencedFeedbackPanel("feedback", form).setEscapeModelStrings(false));
		
		form.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				try {
					OneDev.getInstance(RoleManager.class).delete(getRole());
					setResponsePage(RoleListPage.class);
				} catch (OneException e) {
					error(e.getMessage());
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getRole().isManager());
			}
			
		}.add(new ConfirmOnClick("Do you really want to delete role '" + getRole().getName() + "'?")));
		
		add(form);
	}
	
	@Override
	protected void onDetach() {
		roleModel.detach();
		super.onDetach();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new RoleCssResourceReference()));
	}
	
	public Role getRole() {
		return roleModel.getObject();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	public static PageParameters paramsOf(Role role) {
		PageParameters params = new PageParameters();
		params.add(PARAM_ROLE, role.getId());
		return params;
	}

}
