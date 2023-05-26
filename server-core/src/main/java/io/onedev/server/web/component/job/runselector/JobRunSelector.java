package io.onedev.server.web.component.job.runselector;

import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.asset.selectbytyping.SelectByTypingResourceReference;
import io.onedev.server.web.behavior.InputChangeBehavior;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.component.job.RunJobLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class JobRunSelector extends Panel {
	
	private final String revision;

	private final IModel<List<String>> jobsModel = new LoadableDetachableModel<>() {
		@Override
		protected List<String> load() {
			List<String> jobNames = new ArrayList<>();
			var buildSpec = getProject().getBuildSpec(getCommitId());
			if (buildSpec != null) {
				for (var job: buildSpec.getJobs()) {
					var jobName = job.getName();
					if (SecurityUtils.canRunJob(getProject(), jobName) 
							&& (searchInput == null || jobName.toLowerCase().contains(searchInput.toLowerCase()))) {
						jobNames.add(jobName);
					}
				}
			}
			return jobNames;
		}
	};
	
	private RepeatingView jobsView;
	
	private TextField<String> searchField;
	
	private String searchInput;
	
	public JobRunSelector(String id, String revision) {
		super(id);
		this.revision = revision;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var jobsContainer = new WebMarkupContainer("jobs") {

			@Override
			protected void onBeforeRender() {
				jobsView = new RepeatingView("jobs");
				int index = 0;
				for (var jobName: getJobs()) {
					Component item = newItem(jobsView.newChildId(), jobName);
					if (index == 0)
						item.add(AttributeAppender.append("class", "active"));
					jobsView.add(item);
					if (++index >= WebConstants.PAGE_SIZE)
						break;
				}
				addOrReplace(jobsView);

				super.onBeforeRender();
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getJobs().isEmpty());
			}
		};
		jobsContainer.setOutputMarkupPlaceholderTag(true);
		add(jobsContainer);
		
		jobsContainer.add(new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {

			@Override
			protected void appendMore(AjaxRequestTarget target, int offset, int count) {
				for (int i=offset; i<offset+count; i++) {
					if (i >= getJobs().size())
						break;
					var jobName = getJobs().get(i);

					Component item = newItem(jobsView.newChildId(), jobName);
					jobsView.add(item);
					String script = String.format("$('#%s').append('<li id=\"%s\"></li>');",
							jobsContainer.getMarkupId(), item.getMarkupId());
					target.prependJavaScript(script);
					target.add(item);
				}
			}

		});
		
		var noJobsContainer = new WebMarkupContainer("noJobs") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getJobs().isEmpty());				
			}
		};
		noJobsContainer.setOutputMarkupPlaceholderTag(true);
		add(noJobsContainer);

		searchField = new TextField<>("search", Model.of(""));
		add(searchField);
		searchField.add(new InputChangeBehavior() {

			@Override
			protected void onInputChange(AjaxRequestTarget target) {
				searchInput = searchField.getInput();

				target.add(jobsContainer);
				target.add(noJobsContainer);
			}

		});
	}
	
	private Component newItem(String componentId, String jobName) {
		WebMarkupContainer item = new WebMarkupContainer(componentId);

		RefFacade branchRef, tagRef;

		String refName;
		if ((branchRef = getProject().getBranchRef(revision)) != null)
			refName = branchRef.getName();
		else if ((tagRef = getProject().getTagRef(revision)) != null)
			refName = tagRef.getName();
		else
			refName = null;
		var jobLink = new RunJobLink("link", getCommitId(), jobName, refName) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, jobName);
				super.onClick(target);
			}

			@Override
			protected Project getProject() {
				return JobRunSelector.this.getProject();
			}

			@Override
			protected String getPipeline() {
				return UUID.randomUUID().toString();
			}

			@Nullable
			@Override
			protected PullRequest getPullRequest() {
				return null;
			}

		};
		jobLink.add(new Label("label", jobName));
		item.add(jobLink);
		
		return item;
	}
	
	private ObjectId getCommitId() {
		return getProject().getRevCommit(revision, true).copy();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		jobsModel.detach();
	}

	private List<String> getJobs() {
		return jobsModel.getObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new SelectByTypingResourceReference()));
		response.render(CssHeaderItem.forReference(new JobRunSelectorCssResourceReference()));
		String script = String.format("$('#%s').selectByTyping($('#%s'));", searchField.getMarkupId(), getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Project getProject();

	protected void onSelect(AjaxRequestTarget target, String jobName) {
	}

}
