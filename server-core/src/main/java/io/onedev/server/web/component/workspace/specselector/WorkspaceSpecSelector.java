package io.onedev.server.web.component.workspace.specselector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.selectbytyping.SelectByTypingResourceReference;
import io.onedev.server.web.behavior.InputChangeBehavior;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.workspace.WorkspaceService;

public abstract class WorkspaceSpecSelector extends Panel {

	private final String branch;

	private final IModel<List<WorkspaceSpec>> specsModel = new LoadableDetachableModel<>() {
		@Override
		protected List<WorkspaceSpec> load() {
			List<WorkspaceSpec> specs = new ArrayList<>();
			for (var spec : getProject().getHierarchyWorkspaceSpecs()) {
				if (searchInput == null || spec.getName().toLowerCase().contains(searchInput.toLowerCase()))
					specs.add(spec);
			}
			return specs;
		}
	};

	private RepeatingView specsView;

	private TextField<String> searchField;

	private String searchInput;

	public WorkspaceSpecSelector(String id, String branch) {
		super(id);
		this.branch = branch;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var specsContainer = new WebMarkupContainer("specs") {

			@Override
			protected void onBeforeRender() {
				specsView = new RepeatingView("specs");
				int index = 0;
				for (var spec : getSpecs()) {
					Component item = newItem(specsView.newChildId(), spec);
					if (index == 0)
						item.add(AttributeAppender.append("class", "active"));
					specsView.add(item);
					if (++index >= WebConstants.PAGE_SIZE)
						break;
				}
				addOrReplace(specsView);
				super.onBeforeRender();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getSpecs().isEmpty());
			}
		};
		specsContainer.setOutputMarkupPlaceholderTag(true);
		add(specsContainer);

		specsContainer.add(new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {

			@Override
			protected void appendMore(AjaxRequestTarget target, int offset, int count) {
				for (int i = offset; i < offset + count; i++) {
					if (i >= getSpecs().size())
						break;
					var spec = getSpecs().get(i);
					Component item = newItem(specsView.newChildId(), spec);
					specsView.add(item);
					String script = String.format("$('#%s').append('<li id=\"%s\"></li>');",
							specsContainer.getMarkupId(), item.getMarkupId());
					target.prependJavaScript(script);
					target.add(item);
				}
			}

		});

		var noSpecsContainer = new WebMarkupContainer("noSpecs") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getSpecs().isEmpty());
			}
		};
		noSpecsContainer.setOutputMarkupPlaceholderTag(true);
		add(noSpecsContainer);

		searchField = new TextField<>("search", Model.of(""));
		add(searchField);
		searchField.add(new InputChangeBehavior() {

			@Override
			protected void onInputChange(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(specsContainer);
				target.add(noSpecsContainer);
			}

		});
	}

	private Component newItem(String componentId, WorkspaceSpec spec) {
		WebMarkupContainer item = new WebMarkupContainer(componentId);

		var link = new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				var workspaceService = OneDev.getInstance(WorkspaceService.class);
				var workspace = new Workspace();
				workspace.setProject(getProject());
				workspace.setUser(SecurityUtils.getUser());
				workspace.setBranch(branch);
				workspace.setSpecName(spec.getName());
				workspace.setToken(UUID.randomUUID().toString());
				workspaceService.create(workspace);
				onSelect(target, spec);
				setResponsePage(WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(workspace));
			}

		};
		link.add(new Label("label", spec.getName()));
		item.add(link);

		return item;
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		specsModel.detach();
	}

	private List<WorkspaceSpec> getSpecs() {
		return specsModel.getObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new SelectByTypingResourceReference()));
		response.render(CssHeaderItem.forReference(new WorkspaceSpecSelectorCssResourceReference()));
		String script = String.format("$('#%s').selectByTyping($('#%s'));", searchField.getMarkupId(), getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Project getProject();

	protected void onSelect(AjaxRequestTarget target, WorkspaceSpec spec) {
	}

}
