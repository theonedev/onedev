package io.onedev.server.web.page.project.packs;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class PackDetailPage extends ProjectPage {
	
	private static final String PARAM_PACK = "pack";
	
	private final IModel<Pack> packModel;

	public PackDetailPage(PageParameters params) {
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
		bean.setName(getPack().getName());
		bean.setSupport(getPack().getSupport());
		add(BeanContext.view("pack", bean));
		
		var actions = new WebMarkupContainer("actions");
		actions.setVisible(SecurityUtils.canManagePacks(getProject()));
		add(actions);
		
		actions.add(new BookmarkablePageLink<Void>("edit", PackEditPage.class, PackEditPage.paramsOf(getPack())));
		actions.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(PackManager.class).delete(getPack());
				Session.get().success("Package '" + getPack().getName() + "' deleted");

				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Pack.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(PackListPage.class, PackListPage.paramsOf(getProject()));
			}
			
		}.add(new ConfirmClickModifier("Do you really want to delete this package?")));
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
		if (project.isIssueManagement())
			return new ViewStateAwarePageLink<>(componentId, PackListPage.class, PackListPage.paramsOf(project));
		else
			return new ViewStateAwarePageLink<>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("packs", PackListPage.class,
				PackListPage.paramsOf(getProject())));
		fragment.add(new Label("packName", getPack().getName()));
		return fragment;
	}

	public static PageParameters paramsOf(Pack pack) {
		var params = paramsOf(pack.getProject());
		params.add(PARAM_PACK, pack.getId());
		return params;
	}
	
}
