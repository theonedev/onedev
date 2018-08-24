package io.onedev.server.web.page.group;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ComponentRenderer;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public class NewGroupPage extends LayoutPage {

	private Group group = new Group();
	
	private CheckBox administratorInput;
	
	private CheckBox canCreateProjectsInput;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor editor = BeanContext.editBean("editor", group, 
				Sets.newHashSet("administrator", "canCreateProjects"), true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				GroupManager groupManager = OneDev.getInstance(GroupManager.class);
				Group groupWithSameName = groupManager.find(group.getName());
				if (groupWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another group");
				} 
				if (!editor.hasErrors(true)) {
					group.setAdministrator(administratorInput.getModelObject());
					group.setCanCreateProjects(canCreateProjectsInput.getModelObject());
					groupManager.save(group, null);
					Session.get().success("Group created");
					setResponsePage(GroupMembershipsPage.class, GroupMembershipsPage.paramsOf(group));
				}
			}
			
		};
		form.add(editor);
		administratorInput = new CheckBox("administrator", Model.of(group.isAdministrator()));
		administratorInput.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (administratorInput.getModelObject())
					canCreateProjectsInput.setModelObject(true);
				target.add(canCreateProjectsInput);
			}
			
		});
		form.add(administratorInput);
		
		canCreateProjectsInput = new CheckBox("canCreateProjects", Model.of(group.isCanCreateProjects())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(!administratorInput.getModelObject());
			}
			
		};
		canCreateProjectsInput.setOutputMarkupId(true);
		form.add(canCreateProjectsInput);			
		add(form);
	}

	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = super.getBreadcrumbs();
		
		breadcrumbs.add(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new ViewStateAwarePageLink<Void>(componentId, GroupListPage.class) {

					@Override
					public IModel<?> getBody() {
						return Model.of("Groups");
					}
					
				};
			}
			
		});

		breadcrumbs.add(new ComponentRenderer() {
			
			@Override
			public Component render(String componentId) {
				return new Label(componentId, "New Group") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("div");
					}
					
				};
			}
			
		});
		
		return breadcrumbs;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroupCssResourceReference()));
	}
	
}
