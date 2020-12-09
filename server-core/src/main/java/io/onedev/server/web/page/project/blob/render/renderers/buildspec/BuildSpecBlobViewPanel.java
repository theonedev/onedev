package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.javascript.JavaScriptEscape;

import com.google.common.base.Throwables;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.git.Blob;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.job.RunJobLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class BuildSpecBlobViewPanel extends BlobViewPanel {

	private AbstractPostAjaxBehavior selectBehavior;
	
	public BuildSpecBlobViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		try {
			Blob blob = context.getProject().getBlob(context.getBlobIdent(), true);
			BuildSpec buildSpec = BuildSpec.parse(blob.getBytes());
			
			if (buildSpec != null) {
				Fragment validFrag = new Fragment("content", "validFrag", this);			
				if (!buildSpec.getJobs().isEmpty()) {
					Fragment hasJobsFrag = new Fragment("jobs", "hasJobsFrag", this);
					hasJobsFrag.add(AttributeAppender.append("class", "d-flex d-none"));
					RepeatingView navsView = new RepeatingView("navs");
					RepeatingView jobsView = new RepeatingView("contents");
					for (Job job: buildSpec.getJobs()) {
						WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId());
						nav.add(new Label("jobName", job.getName()));
						nav.add(AttributeAppender.append("data-name", job.getName()));
						nav.add(new RunJobLink("run", context.getCommit().copy(), job.getName(), getContext().getRefName()) {

							@Override
							protected Project getProject() {
								return context.getProject();
							}

							@Override
							protected PullRequest getPullRequest() {
								return context.getPullRequest();
							}

						});
						navsView.add(nav);
						jobsView.add(BeanContext.view(jobsView.newChildId(), job));
					}
					hasJobsFrag.add(navsView);
					hasJobsFrag.add(jobsView);
					
					validFrag.add(hasJobsFrag);
				} else {
					validFrag.add(new Label("jobs", "No jobs defined").add(AttributeAppender.append("class", "not-defined alert alert-notice alert-light-warning d-flex d-none")));
				}
				
				if (!buildSpec.getProperties().isEmpty())
					validFrag.add(PropertyContext.view("properties", buildSpec, "properties"));
				else
					validFrag.add(new Label("properties", "No properties defined").add(AttributeAppender.append("class", "not-defined alert alert-notice alert-light-warning d-flex d-none")));
					
				add(validFrag);
			} else {
				add(new Label("content", "Build spec not defined").add(AttributeAppender.append("class", "not-defined m-4 alert alert-notice alert-light-warning")));
			}
		} catch (Exception e) {
			Fragment invalidFrag = new Fragment("content", "invalidFrag", this);
			invalidFrag.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString(e)));
			add(invalidFrag);
		}
		
		add(selectBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String selection = params.getParameterValue("selection").toString();
				String position = BuildSpecRendererProvider.getPosition(selection);
				context.pushState(target, context.getBlobIdent(), position);
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BuildSpecResourceReference()));
		String selection = BuildSpecRendererProvider.getSelection(context.getPosition());
		String script = String.format("onedev.server.buildSpec.onDomReady(%s, %s);", 
				selection!=null? "'" + JavaScriptEscape.escapeJavaScript(selection) + "'": "undefined", 
				selectBehavior.getCallbackFunction(CallbackParameter.explicit("selection")));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected boolean isEditSupported() {
		return true;
	}

	@Override
	protected boolean isViewPlainSupported() {
		return true;
	}

}
