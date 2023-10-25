package io.onedev.server.web.page.project.packs;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class NewPackPage extends ProjectPage {

	public NewPackPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var bean = new PackEditBean();
		BeanEditor editor = BeanContext.edit("editor", bean);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				var pack = new Pack();
				pack.setName(bean.getName());
				pack.setSupport(bean.getSupport());
				pack.setType(EditableUtils.getDisplayName(bean.getSupport().getClass()));
				pack.setProject(getProject());
				OneDev.getInstance(PackManager.class).create(pack);
				Session.get().success("Package created");
				setResponsePage(PackDetailPage.class, PackDetailPage.paramsOf(pack));
			}

		};
		form.add(editor);
		add(form);		
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isPackManagement())
			return new ViewStateAwarePageLink<>(componentId, PackListPage.class, PackListPage.paramsOf(project));
		else
			return new ViewStateAwarePageLink<>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		var fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("packs", 
				PackListPage.class, PackListPage.paramsOf(getProject())));
		return fragment;
	}
	
}
