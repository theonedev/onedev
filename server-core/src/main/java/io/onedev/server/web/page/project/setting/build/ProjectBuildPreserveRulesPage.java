package io.onedev.server.web.page.project.setting.build;

import java.io.Serializable;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.user.buildsetting.UserBuildPreserveRulesPage;
import io.onedev.server.web.page.my.buildsetting.MyBuildPreserveRulesPage;

@SuppressWarnings("serial")
public class ProjectBuildPreserveRulesPage extends ProjectBuildSettingPage {

	public ProjectBuildPreserveRulesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				OneDev.getInstance(ProjectManager.class).save(getProject());
				getSession().success("Build preserve rules have been saved");
				setResponsePage(ProjectBuildPreserveRulesPage.class, ProjectBuildPreserveRulesPage.paramsOf(getProject()));
			}
			
		};
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		form.add(PropertyContext.editModel("editor", new AbstractReadOnlyModel<Serializable>() {

			@Override
			public Serializable getObject() {
				return getProject().getBuildSetting();
			}
			
		}, "buildPreservations"));
		
		add(form);
		
		add(PropertyContext.view("inheritedBuildPreservations", 
				getProject().getOwner().getBuildSetting(), "buildPreservations"));

		if (SecurityUtils.isAdministrator()) {
			add(new BookmarkablePageLink<Void>("owner", UserBuildPreserveRulesPage.class, 
					UserBuildPreserveRulesPage.paramsOf(getProject().getOwner())));
		} else if (getProject().getOwner().equals(SecurityUtils.getUser())) {
			add(new BookmarkablePageLink<Void>("owner", MyBuildPreserveRulesPage.class)); 
		} else {
			add(new WebMarkupContainer("owner") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			});
		}
		
	}

}
