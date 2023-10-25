package io.onedev.server.web.page.project.packs;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class PackEditPage extends ProjectPage {

	private static final String PARAM_PACK = "pack";
	
	private final IModel<Pack> packModel;
	
	public PackEditPage(PageParameters params) {
		super(params);

		var packId = params.get(PARAM_PACK).toLong();
		packModel = new LoadableDetachableModel<>() {
			@Override
			protected Pack load() {
				return OneDev.getInstance(PackManager.class).load(packId);
			}
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var bean = new PackEditBean();
		bean.setOldName(getPack().getName());
		bean.setName(getPack().getName());
		
		var supportBean = getPack().getSupport();
		var form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				var pack = getPack();
				pack.setName(bean.getName());
				pack.setSupport(supportBean);
				OneDev.getInstance(PackManager.class).update(pack);
				setResponsePage(PackDetailPage.class, PackDetailPage.paramsOf(pack));
			}
		};
		form.add(BeanContext.edit("editor", bean, Sets.newHashSet("support"), true));
		form.add(BeanContext.edit("supportEditor", supportBean));
		add(form);
	}

	@Override
	protected void onDetach() {
		packModel.detach();
		super.onDetach();
	}
	
	private Pack getPack() {
		return packModel.getObject();
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
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("packs", PackListPage.class,
				PackListPage.paramsOf(getProject())));
		Link<Void> link = new BookmarkablePageLink<Void>("pack", PackDetailPage.class,
				PackDetailPage.paramsOf(getPack()));
		link.add(new Label("name", getPack().getName()));
		fragment.add(link);
		return fragment;
	}
	
	public static PageParameters paramsOf(Pack pack) {
		var params = paramsOf(pack.getProject());
		params.add(PARAM_PACK, pack.getId());
		return params;
	}
	
}
