package io.onedev.server.web.page.project.issues.milestones;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathNode;
import io.onedev.server.web.editable.Path;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;

@SuppressWarnings("serial")
public class MilestoneEditPage extends ProjectIssuesPage {

	private static final String PARAM_MILESTONE = "milestone";
	
	private final IModel<Milestone> milestoneModel;
	
	public MilestoneEditPage(PageParameters params) {
		super(params);
		
		Long milestoneId = params.get(PARAM_MILESTONE).toLong();
		milestoneModel = new LoadableDetachableModel<Milestone>() {

			@Override
			protected Milestone load() {
				return OneDev.getInstance(MilestoneManager.class).load(milestoneId);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Milestone milestone = milestoneModel.getObject();
		BeanEditor editor = BeanContext.edit("editor", milestone);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
				Milestone milestoneWithSameName = milestoneManager.find(getProject(), milestone.getName());
				if (milestoneWithSameName != null && !milestoneWithSameName.equals(milestone)) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another milestone in the project");
				} 
				if (editor.isValid()){
					editor.getDescriptor().copyProperties(milestone, milestoneModel.getObject());
					milestoneManager.save(milestoneModel.getObject());
					Session.get().success("Milestone saved");
					setResponsePage(MilestoneDetailPage.class, MilestoneDetailPage.paramsOf(milestoneModel.getObject(), null));
				}
				
			}
			
		};
		form.add(editor);
		add(form);
	}
	
	@Override
	protected void onDetach() {
		milestoneModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MilestonesResourceReference()));
	}

	public static PageParameters paramsOf(Milestone milestone) {
		PageParameters params = paramsOf(milestone.getProject());
		params.add(PARAM_MILESTONE, milestone.getId());
		return params;
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManageIssues(getProject());
	}
	
}
