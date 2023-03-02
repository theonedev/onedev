package io.onedev.server.web.page.project.setting.pluginsettings;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nullable;
import javax.validation.Validator;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class ContributedProjectSettingPage extends ProjectSettingPage {

	public static final String PARAM_SETTING = "projectSetting";
	
	private Class<? extends ContributedProjectSetting> settingClass;
	
	public ContributedProjectSettingPage(PageParameters params) {
		super(params);
		
		String settingName = params.get(PARAM_SETTING).toString();
		
		for (ProjectSettingContribution contribution: 
				OneDev.getExtensions(ProjectSettingContribution.class)) {
			for (Class<? extends ContributedProjectSetting> each: contribution.getSettingClasses()) {
				if (getSettingName(each).equals(settingName)) { 
					settingClass = each;
					break;
				}
			}
			if (settingClass != null)
				break;
		}

		if (settingClass == null)
			throw new RuntimeException("Unexpected setting: " + settingName);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String help = EditableUtils.getDescription(settingClass);
		if (help != null)
			add(new Label("help", help).setEscapeModelStrings(false));
		else
			add(new WebMarkupContainer("help").setVisible(false));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Component editor = get("editor");
				if (editor instanceof BeanEditor && editor.isVisible()) 
					getProject().setContributedSetting(settingClass, (ContributedProjectSetting) ((BeanEditor)editor).getModelObject());
				else 
					getProject().setContributedSetting(settingClass, null);
				
				OneDev.getInstance(ProjectManager.class).update(getProject());
				
				getSession().success("Setting has been saved");
				
				setResponsePage(ContributedProjectSettingPage.class, paramsOf(getProject(), settingClass));
			}
			
		};
		
		form.add(new CheckBox("enable", new IModel<Boolean>() {
			
			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				Component beanEditor = form.get("editor");
				return beanEditor != null && beanEditor.isVisible();
			}

			@Override
			public void setObject(Boolean object) {
				Component beanEditor = form.get("editor");
				if (beanEditor instanceof BeanEditor || !object) {
					beanEditor.setVisible(object);
				} else {
					try {
						form.replace(newBeanEditor(settingClass.getDeclaredConstructor().newInstance()));
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				try {
					setVisible(!OneDev.getInstance(Validator.class)
							.validate(settingClass.getDeclaredConstructor().newInstance())
							.isEmpty());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
			
		}.add(new AjaxFormComponentUpdatingBehavior("click"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(form.get("editor"));
			}
			
		}));
		
		Serializable setting = getProject().getContributedSetting(settingClass);
		form.add(newBeanEditor(setting));
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		add(form);
	}
	
	private Component newBeanEditor(@Nullable Serializable setting) {
		Component beanEditor;
		if (setting != null)
			beanEditor = BeanContext.edit("editor", setting);
		else 
			beanEditor = new WebMarkupContainer("editor").setVisible(false);
		beanEditor.setOutputMarkupPlaceholderTag(true);
		return beanEditor;
	}

	public static String getSettingName(Class<?> settingClass) {
		return EditableUtils.getDisplayName(settingClass).replace(' ', '-').toLowerCase();
	}
	
	public static PageParameters paramsOf(Project project, Class<?> settingClass) {
		PageParameters params = ProjectSettingPage.paramsOf(project);
		params.add(PARAM_SETTING, getSettingName(settingClass));
		return params;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, EditableUtils.getDisplayName(settingClass));
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (SecurityUtils.canManage(project)) 
			return new ViewStateAwarePageLink<Void>(componentId, ContributedProjectSettingPage.class, paramsOf(project, settingClass));
		else 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectPage.paramsOf(project.getId()));
	}
	
}
