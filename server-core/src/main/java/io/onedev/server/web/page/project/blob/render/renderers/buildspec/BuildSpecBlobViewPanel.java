package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getActiveElementIndex;
import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getPosition;
import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getSelection;
import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getUrlSegment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import io.onedev.server.model.support.build.JobProperty;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.eclipse.jgit.lib.FileMode;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Throwables;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.Import;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.step.StepTemplate;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.job.RunJobLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.pipeline.JobSelectionChange;
import io.onedev.server.web.component.pipeline.PipelinePanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
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

		Serializable parseResult;
		try {
			Blob blob = context.getProject().getBlob(context.getBlobIdent(), true);
			parseResult = BuildSpec.parse(blob.getBytes());
		} catch (Exception e) {
			parseResult = e;
		}
		if (parseResult != null) {
			if (parseResult instanceof BuildSpec) {
				BuildSpec buildSpec = (BuildSpec) parseResult;
				
				add(new Fragment("content", "parseableFrag", this) {
	
					private void resizeWindow(AjaxRequestTarget target) {
						((BasePage)getPage()).resizeWindow(target);
					}
					
					private void setupPropertiesViewer(@Nullable AjaxRequestTarget target) {
						Component propertiesViewer;
						List<JobProperty> properties = new ArrayList<>(buildSpec.getPropertyMap().values()); 
						
						if (!properties.isEmpty()) {
							propertiesViewer = new Fragment("content", "propertiesFrag", BuildSpecBlobViewPanel.this) {

								@Override
								protected void onInitialize() {
									super.onInitialize();
									
									add(new ListView<JobProperty>("properties", properties) {

										@Override
										protected void populateItem(ListItem<JobProperty> item) {
											JobProperty property = item.getModelObject();
											item.add(new Label("name", property.getName()));
											
											Import aImport = null;
											for (Import each: buildSpec.getImports()) {
												try {
													if (each.getBuildSpec().getPropertyMap().get(property.getName()) == property) {
														aImport = each;
														break;
													}
												} catch (Exception e) {
													// Ignore. This error will be shown when validating
												}
											}
											
											if (aImport != null) {
												ProjectBlobPage.State state = new ProjectBlobPage.State();
												state.blobIdent = new BlobIdent(aImport.getRevision(), BuildSpec.BLOB_PATH, 
														FileMode.REGULAR_FILE.getBits());
												state.position = BuildSpecRenderer.getPosition("properties");
												Project project = aImport.getProject();
												Link<?> link = new ViewStateAwarePageLink<Void>("imported", ProjectBlobPage.class, 
														ProjectBlobPage.paramsOf(project, state)) {

													@Override
													protected void onComponentTag(ComponentTag tag) {
														super.onComponentTag(tag);
														if (!isEnabled())
															tag.setName("span");
													}
													
												};
												link.add(AttributeAppender.append("title", 
														"This property is imported from " + aImport.getProjectPath()));
												link.setEnabled(SecurityUtils.canReadCode(project));
												item.add(link);
											} else {
												item.add(new WebMarkupContainer("imported").setVisible(false));
											}
											
											item.add(new Label("value", property.getValue()));
										}
										
									});
								}
								
							};
							propertiesViewer.add(AttributeAppender.append("class", "properties autofit pr-3"));
						} else {
							propertiesViewer = new Label("content", "No properties defined");
							String cssClasses = "properties not-defined alert alert-notice alert-light-warning d-flex";
							propertiesViewer.add(AttributeAppender.append("class", cssClasses));
						}
						propertiesViewer.add(new Behavior() {
							
							@Override
							public void renderHead(Component component, IHeaderResponse response) {
								super.renderHead(component, response);
								response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.properties');"));
							}
							
						});
						propertiesViewer.setOutputMarkupId(true);
						if (target != null) {
							replace(propertiesViewer);
							target.add(propertiesViewer);
						} else {
							add(propertiesViewer);
						}
					}
					
					private <T extends NamedElement> List<T> getAllElements(List<T> elements, Map<String, T> elementMap) {
						List<T> allElements = new ArrayList<>(elements);
						for (T element: elementMap.values()) {
							if (!elements.contains(element))
								allElements.add(element);
						}
						return allElements;
					}
					
					private void setupJobsViewer(@Nullable AjaxRequestTarget target) {
						Component jobsViewer;
						List<Job> jobs = getAllElements(buildSpec.getJobs(), buildSpec.getJobMap());
						if (!jobs.isEmpty()) {
							jobsViewer = new Fragment("content", "jobsFrag", BuildSpecBlobViewPanel.this) {
	
								private void notifyJobSelectionChange(AjaxRequestTarget target, Job job) {
									send(this, Broadcast.BREADTH, new JobSelectionChange(target, job));
								}
								
								@Override
								protected void onInitialize() {
									super.onInitialize();

									int activeJobIndex = getActiveElementIndex(context, Job.class, jobs, 0);
									
									add(new PipelinePanel("pipeline") {

										@Override
										protected Component renderJob(String componentId, int jobIndex) {
											Fragment jobNav = new Fragment(componentId, "jobFrag", BuildSpecBlobViewPanel.this);
											
											AjaxLink<Void> jobLink = new AjaxLink<Void>("select") {
												
												@Override
												public void onClick(AjaxRequestTarget target) {
													Job job = getJobs().get(jobIndex);
													String position = getPosition("jobs/" + job.getName());
													context.pushState(target, context.getBlobIdent(), position);
													setupJobDetail(target, jobIndex);
													notifyJobSelectionChange(target, job);
													resizeWindow(target);
												}
												
											};
											
											Job job = getJobs().get(jobIndex);
											String label = HtmlEscape.escapeHtml5(job.getName());
											if (!buildSpec.getJobs().contains(job)) {
												String iconHtml = String.format(
														"<svg class='icon imported'><use xlink:href='%s'/></svg>", 
														SpriteImage.getVersionedHref(IconScope.class, "arrow2"));
												label += iconHtml;
											} 
											jobLink.add(new Label("label", label).setEscapeModelStrings(false));
											
											jobNav.add(jobLink);
											
											jobNav.add(new RunJobLink("run", context.getCommit().copy(), job.getName(), getContext().getRefName()) {
		
												@Override
												protected Project getProject() {
													return context.getProject();
												}
		
												@Override
												protected PullRequest getPullRequest() {
													return null;
												}

												@Override
												protected String getPipeline() {
													return UUID.randomUUID().toString();
												}
		
											});
											jobNav.add(AttributeAppender.append("class", "nav btn-group flex-nowrap"));
											return jobNav;
										}

										@Override
										protected List<Job> getJobs() {
											return jobs;
										}

										@Override
										protected int getActiveJobIndex() {
											return activeJobIndex;
										}
										
									});
									
									setupJobDetail(null, activeJobIndex);									
									
									add(AttributeAppender.append("class", "elements jobs d-flex flex-nowrap"));
								}
	
								private void setupJobDetail(@Nullable AjaxRequestTarget target, int jobIndex) {
									Job job = jobs.get(jobIndex);
									Fragment detailFrag = new Fragment("detail", "elementDetailFrag", BuildSpecBlobViewPanel.this);
									detailFrag.add(new ElementImportNoticePanel(buildSpec, Job.class, job.getName()) {

										@Override
										protected Map<String, ? extends NamedElement> getElementMap(BuildSpec buildSpec) {
											return buildSpec.getJobMap();
										}
										
									});
									detailFrag.add(BeanContext.view("content", job));
									detailFrag.setOutputMarkupId(true);
									
									if (target != null) {
										replace(detailFrag);
										target.add(detailFrag);
									} else {
										add(detailFrag);
									}
								}
								
							};
						} else {
							String cssClasses = "jobs not-defined alert alert-notice alert-light-warning d-flex";
							jobsViewer = new Label("content", "No jobs defined").add(AttributeAppender.append("class", cssClasses));
						}
						jobsViewer.add(new Behavior() {
								
							@Override
							public void renderHead(Component component, IHeaderResponse response) {
								super.renderHead(component, response);
								response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.jobs');"));
							}
							
						});
						jobsViewer.setOutputMarkupId(true);
						if (target != null) {
							replace(jobsViewer);
							target.add(jobsViewer);
						} else {
							add(jobsViewer);
						}
					}
					
					private void setupServicesViewer(@Nullable AjaxRequestTarget target) {
						Component servicesViewer;
						List<Service> services = getAllElements(buildSpec.getServices(), buildSpec.getServiceMap());
						if (!services.isEmpty()) {
							servicesViewer = new ElementsViewer<Service>(buildSpec) {

								@Override
								protected List<Service> getElements(boolean includeImported) {
									if (includeImported)
										return services;
									else
										return buildSpec.getServices();
								}

								@Override
								protected Map<String, Service> getElementMap(BuildSpec buildSpec) {
									return buildSpec.getServiceMap();
								}
								
							};
						} else {
							String cssClasses = "services not-defined alert alert-notice alert-light-warning d-flex";
							servicesViewer = new Label("content", "No services defined").add(AttributeAppender.append("class", cssClasses));
						}
						servicesViewer.add(new Behavior() {
								
							@Override
							public void renderHead(Component component, IHeaderResponse response) {
								super.renderHead(component, response);
								response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.services');"));
							}
							
						});
						servicesViewer.setOutputMarkupId(true);
						if (target != null) {
							replace(servicesViewer);
							target.add(servicesViewer);
						} else {
							add(servicesViewer);
						}
					}
					
					private void setupStepTemplatesViewer(@Nullable AjaxRequestTarget target) {
						Component templatesViewer;
						List<StepTemplate> templates = getAllElements(buildSpec.getStepTemplates(), buildSpec.getStepTemplateMap());
						if (!templates.isEmpty()) {
							templatesViewer = new ElementsViewer<StepTemplate>(buildSpec) {

								@Override
								protected List<StepTemplate> getElements(boolean includeImported) {
									if (includeImported)
										return templates;
									else
										return buildSpec.getStepTemplates();
								}

								@Override
								protected Map<String, StepTemplate> getElementMap(BuildSpec buildSpec) {
									return buildSpec.getStepTemplateMap();
								}
								
							};
						} else {
							String cssClasses = "step-templates not-defined alert alert-notice alert-light-warning d-flex";
							templatesViewer = new Label("content", "No step templates defined").add(AttributeAppender.append("class", cssClasses));
						}
						templatesViewer.add(new Behavior() {
								
							@Override
							public void renderHead(Component component, IHeaderResponse response) {
								super.renderHead(component, response);
								response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.step-templates');"));
							}
							
						});
						templatesViewer.setOutputMarkupId(true);
						if (target != null) {
							replace(templatesViewer);
							target.add(templatesViewer);
						} else {
							add(templatesViewer);
						}
					}
					
					private void setupImportsViewer(@Nullable AjaxRequestTarget target) {
						Component importsViewer;
						if (!buildSpec.getImports().isEmpty()) {
							importsViewer = PropertyContext.view("content", buildSpec, "imports");
							importsViewer.add(AttributeAppender.append("class", "imports autofit pr-3"));
						} else {
							importsViewer = new Label("content", "No imports defined");
							String cssClasses = "imports not-defined alert alert-notice alert-light-warning d-flex";
							importsViewer.add(AttributeAppender.append("class", cssClasses));
						}
						importsViewer.add(new Behavior() {
							
							@Override
							public void renderHead(Component component, IHeaderResponse response) {
								super.renderHead(component, response);
								response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.imports');"));
							}
							
						});
						importsViewer.setOutputMarkupId(true);
						if (target != null) {
							replace(importsViewer);
							target.add(importsViewer);
						} else {
							add(importsViewer);
						}
					}
					
					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						Validator validator = OneDev.getInstance(Validator.class);
						for (ConstraintViolation<BuildSpec> violation: validator.validate(buildSpec)) {
							if (StringUtils.isNotBlank(violation.getPropertyPath().toString())) {
								error(String.format("Error validating build spec (location: %s, error message: %s)", 
										violation.getPropertyPath(), violation.getMessage()));
							} else {
								error(String.format("Error validating build spec: %s", violation.getMessage()));
							}
						}
						
						add(new FencedFeedbackPanel("feedback", this));
						
						add(new AjaxLink<Void>("jobs") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("jobs");
								context.pushState(target, context.getBlobIdent(), position);
								setupJobsViewer(target);
								resizeWindow(target);
							}
							
						});
						add(new AjaxLink<Void>("services") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("services");
								context.pushState(target, context.getBlobIdent(), position);
								setupServicesViewer(target);
								resizeWindow(target);
							}
							
						});
						add(new AjaxLink<Void>("stepTemplates") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("step-templates");
								context.pushState(target, context.getBlobIdent(), position);
								setupStepTemplatesViewer(target);
								resizeWindow(target);
							}
							
						});
						add(new AjaxLink<Void>("properties") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("properties");
								context.pushState(target, context.getBlobIdent(), position);
								setupPropertiesViewer(target);
								resizeWindow(target);
							}
							
						});
						add(new AjaxLink<Void>("imports") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("imports");
								context.pushState(target, context.getBlobIdent(), position);
								setupImportsViewer(target);
								resizeWindow(target);
							}
							
						});
	
						String selection = getSelection(context.getPosition());
						if (selection == null || selection.startsWith("jobs"))
							setupJobsViewer(null);
						else if (selection.startsWith("services"))
							setupServicesViewer(null);
						else if (selection.startsWith("step-templates"))
							setupStepTemplatesViewer(null);
						else if (selection.equals("imports"))
							setupImportsViewer(null);
						else
							setupPropertiesViewer(null);
						
						add(AttributeAppender.append("class", "parseable"));
					}
					
				}.setOutputMarkupId(true));
			} else {
				Fragment unparseableFrag = new Fragment("content", "unparseableFrag", this);
				unparseableFrag.add(AttributeAppender.append("class", "unparseable"));
				unparseableFrag.add(new MultilineLabel("errorMessage", 
						Throwables.getStackTraceAsString((Throwable) parseResult)));
				add(unparseableFrag);
			}				
		} else {
			String cssClasses = "not-defined m-4 alert alert-notice alert-light-warning";
			add(new Label("content", "Build spec not defined").add(AttributeAppender.append("class", cssClasses)));
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
	
	private abstract class ElementsViewer<T extends NamedElement> extends Fragment {
		
		private final BuildSpec buildSpec;
		
		private final Class<T> elementClass;
		
		@SuppressWarnings("unchecked")
		public ElementsViewer(BuildSpec buildSpec) {
			super("content", "elementsFrag", BuildSpecBlobViewPanel.this);
			
			this.buildSpec = buildSpec;
			
			List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(ElementsViewer.class, getClass());
			elementClass = (Class<T>) typeArguments.get(0);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			RepeatingView navsView = new RepeatingView("navs");
			
			String urlSegment = getUrlSegment(elementClass);
			for (int i=0; i<getElements(true).size(); i++) {
				int elementIndex = i;
				NamedElement element = getElements(true).get(elementIndex);
				WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId());
				AjaxLink<Void> elementLink = new AjaxLink<Void>("select") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						String position = getPosition(urlSegment + "s/" + element.getName());
						context.pushState(target, context.getBlobIdent(), position);
						setupElementDetail(target, elementIndex);
					}
					
				};
				
				String label = HtmlEscape.escapeHtml5(element.getName());
				if (!getElements(false).contains(element)) {
					String iconHtml = String.format(
							"<svg class='icon imported'><use xlink:href='%s'/></svg>", 
							SpriteImage.getVersionedHref(IconScope.class, "arrow2"));
					label += iconHtml;
				} 
				elementLink.add(new Label("label", label).setEscapeModelStrings(false));
				
				nav.add(elementLink);
				
				navsView.add(nav);
			}
			add(navsView);

			setupElementDetail(null, getActiveElementIndex(context, elementClass, getElements(true), 0));
			
			add(AttributeAppender.append("class", "elements d-flex flex-nowrap " + urlSegment + "s"));
		}

		private void setupElementDetail(@Nullable AjaxRequestTarget target, int elementIndex) {
			Fragment detailFrag = new Fragment("detail", "elementDetailFrag", BuildSpecBlobViewPanel.this) {
				
				@Override
				public void renderHead(IHeaderResponse response) {
					super.renderHead(response);
					
					String script = String.format("onedev.server.buildSpec.markElementActive(%d);", elementIndex);
					response.render(OnDomReadyHeaderItem.forScript(script));
				}
				
			};
			
			T element = getElements(true).get(elementIndex);
			
			detailFrag.add(new ElementImportNoticePanel(buildSpec, elementClass, element.getName()) {

				@Override
				protected Map<String, ? extends NamedElement> getElementMap(BuildSpec buildSpec) {
					return ElementsViewer.this.getElementMap(buildSpec);
				}
				
			});
			
			detailFrag.add(BeanContext.view("content", element));
			detailFrag.setOutputMarkupId(true);
			
			if (target != null) {
				replace(detailFrag);
				target.add(detailFrag);
			} else {
				add(detailFrag);
			}
		}
		
		protected abstract Map<String, T> getElementMap(BuildSpec buildSpec);
		
		protected abstract List<T> getElements(boolean includeImported);
		
	}

	private abstract class ElementImportNoticePanel extends Fragment {

		private final Class<? extends NamedElement> elementClass;
		
		private final String elementName;
		
		private Import aImport;
		
		public ElementImportNoticePanel(BuildSpec buildSpec, 
				Class<? extends NamedElement> elementClass, String elementName) {
			super("notice", "elementImportNoticeFrag", BuildSpecBlobViewPanel.this);

			this.elementClass = elementClass;
			this.elementName = elementName;
			
			NamedElement element = getElementMap(buildSpec).get(elementName);
			for (Import aImport: buildSpec.getImports()) {
				try {
					if (getElementMap(aImport.getBuildSpec()).get(elementName) == element) {
						this.aImport = aImport;
						break;
					}
				} catch (Exception e) {
					// Ignore. This error will be shown when validating
				}
			}
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			add(new Label("elementTypeName", EditableUtils.getDisplayName(elementClass).toLowerCase()));
			
			if (aImport != null) {
				ProjectBlobPage.State state = new ProjectBlobPage.State();
				state.blobIdent = new BlobIdent(aImport.getRevision(), BuildSpec.BLOB_PATH, 
						FileMode.REGULAR_FILE.getBits());
				String urlSegment = BuildSpecRenderer.getUrlSegment(elementClass);
				state.position = BuildSpecRenderer.getPosition(urlSegment+"s/" + elementName);
				Project project = aImport.getProject();
				Link<?> link = new ViewStateAwarePageLink<Void>("link", ProjectBlobPage.class, 
						ProjectBlobPage.paramsOf(project, state)) {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (!isEnabled())
							tag.setName("span");
					}
					
				};
				link.setEnabled(SecurityUtils.canReadCode(project));
				add(link);
				link.add(new Label("label", project.getPath() + ":" + aImport.getRevision()));
			} else {
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(new WebMarkupContainer("label"));
				add(link);
			}
		}
		
		protected abstract Map<String, ? extends NamedElement> getElementMap(BuildSpec buildSpec);

		@Override
		protected void onConfigure() {
			super.onConfigure();
			setVisible(aImport != null);
		}
		
	}
}
