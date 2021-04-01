package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;

import com.google.common.base.Throwables;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.git.Blob;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.job.RunJobLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class BuildSpecBlobViewPanel extends BlobViewPanel {

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
				add(new Fragment("content", "validFrag", this) {

					private void newPropertiesView(@Nullable AjaxRequestTarget target) {
						Component propertiesView;
						if (!buildSpec.getProperties().isEmpty()) {
							propertiesView = PropertyContext.view("body", buildSpec, "properties");
							propertiesView.add(AttributeAppender.append("class", "properties"));
						} else {
							propertiesView = new Label("body", "No properties defined");
							String cssClasses = "properties not-defined alert alert-notice alert-light-warning d-flex";
							propertiesView.add(AttributeAppender.append("class", cssClasses));
						}
						propertiesView.add(new Behavior() {
							
							@Override
							public void renderHead(Component component, IHeaderResponse response) {
								super.renderHead(component, response);
								response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.properties');"));
							}
							
						});
						propertiesView.setOutputMarkupId(true);
						if (target != null) {
							replace(propertiesView);
							target.add(propertiesView);
						} else {
							add(propertiesView);
						}
					}
					
					private void newJobsView(@Nullable AjaxRequestTarget target) {
						Component jobsView;
						if (!buildSpec.getJobs().isEmpty()) {
							jobsView = new Fragment("body", "jobsFrag", BuildSpecBlobViewPanel.this) {

								@Override
								protected void onInitialize() {
									super.onInitialize();
									
									RepeatingView navsView = new RepeatingView("navs");
									for (int i=0; i<buildSpec.getJobs().size(); i++) {
										int jobIndex = i;
										Job job = buildSpec.getJobs().get(jobIndex);
										WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId());
										AjaxLink<Void> jobLink = new AjaxLink<Void>("job") {

											@Override
											public void onClick(AjaxRequestTarget target) {
												String position = BuildSpecRendererProvider.getPosition("jobs/" + job.getName());
												context.pushState(target, context.getBlobIdent(), position);
												newJobView(target, jobIndex);
											}
											
										};
										jobLink.add(new Label("label", job.getName()));
										nav.add(jobLink);
										
										nav.add(new RunJobLink("runJob", context.getCommit().copy(), job.getName(), getContext().getRefName()) {

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
									}
									add(navsView);

									newJobView(null, BuildSpecRendererProvider.getActiveJobIndex(context, buildSpec));
									
									add(AttributeAppender.append("class", "jobs d-flex flex-nowrap"));
								}

								private void newJobView(@Nullable AjaxRequestTarget target, int jobIndex) {
									Component jobView = BeanContext.view("job", buildSpec.getJobs().get(jobIndex));
									jobView.add(new Behavior() {
										
										@Override
										public void renderHead(Component component, IHeaderResponse response) {
											super.renderHead(component, response);
											
											String script = String.format("onedev.server.buildSpec.onNamedElementDomReady(%d);", jobIndex);
											response.render(OnDomReadyHeaderItem.forScript(script));
										}
										
									});
									jobView.setOutputMarkupId(true);
									if (target != null) {
										replace(jobView);
										target.add(jobView);
									} else {
										add(jobView);
									}
								}
								
							};
						} else {
							String cssClasses = "jobs not-defined alert alert-notice alert-light-warning d-flex";
							jobsView = new Label("body", "No jobs defined").add(AttributeAppender.append("class", cssClasses));
						}
						jobsView.add(new Behavior() {
								
							@Override
							public void renderHead(Component component, IHeaderResponse response) {
								super.renderHead(component, response);
								response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.jobs');"));
							}
							
						});
						jobsView.setOutputMarkupId(true);
						if (target != null) {
							replace(jobsView);
							target.add(jobsView);
						} else {
							add(jobsView);
						}
					}
					
					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new AjaxLink<Void>("jobs") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = BuildSpecRendererProvider.getPosition("jobs");
								context.pushState(target, context.getBlobIdent(), position);
								newJobsView(target);
							}
							
						});
						add(new AjaxLink<Void>("properties") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = BuildSpecRendererProvider.getPosition("properties");
								context.pushState(target, context.getBlobIdent(), position);
								newPropertiesView(target);
							}
							
						});
						
						String selection = BuildSpecRendererProvider.getSelection(context.getPosition());
						if (selection == null || selection.startsWith("jobs"))
							newJobsView(null);
						else
							newPropertiesView(null);
						
						add(AttributeAppender.append("class", "valid"));
					}
					
				}.setOutputMarkupId(true));
			} else {
				String cssClasses = "not-defined m-4 alert alert-notice alert-light-warning";
				add(new Label("content", "Build spec not defined").add(AttributeAppender.append("class", cssClasses)));
			}
		} catch (Exception e) {
			Fragment invalidFrag = new Fragment("content", "invalidFrag", this);
			invalidFrag.add(AttributeAppender.append("class", "invalid"));
			invalidFrag.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString(e)));
			add(invalidFrag);
		}
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BuildSpecResourceReference()));
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
