package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getActiveNamedElementIndex;
import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getPosition;
import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getSelection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
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
import io.onedev.server.buildspec.Property;
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
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyContext;
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
	
					private void setupPropertiesViewer(@Nullable AjaxRequestTarget target) {
						Component propertiesViewer;
						List<Property> properties = new ArrayList<>(buildSpec.getPropertyMap().values()); 
						
						if (!properties.isEmpty()) {
							propertiesViewer = new Fragment("content", "propertiesFrag", BuildSpecBlobViewPanel.this) {

								@Override
								protected void onInitialize() {
									super.onInitialize();
									
									add(new ListView<Property>("properties", properties) {

										@Override
										protected void populateItem(ListItem<Property> item) {
											Property property = item.getModelObject();
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
												state.blobIdent = new BlobIdent(aImport.getTag(), BuildSpec.BLOB_PATH, 
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
							propertiesViewer.add(AttributeAppender.append("class", "properties"));
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
					
					private <T extends NamedElement> List<T> getNamedElementsToShow(List<T> elements, Map<String, T> elementMap) {
						List<T> elementsToShow = new ArrayList<>(elements);
						for (T element: elementMap.values()) {
							if (!elements.contains(element))
								elementsToShow.add(element);
						}
						return elementsToShow;
					}
					
					private void setupJobsViewer(@Nullable AjaxRequestTarget target) {
						Component jobsViewer;
						List<Job> jobs = getNamedElementsToShow(buildSpec.getJobs(), buildSpec.getJobMap());
						if (!jobs.isEmpty()) {
							jobsViewer = new Fragment("content", "jobsFrag", BuildSpecBlobViewPanel.this) {
	
								@Override
								protected void onInitialize() {
									super.onInitialize();
									
									RepeatingView navsView = new RepeatingView("navs");
									for (int i=0; i<jobs.size(); i++) {
										int jobIndex = i;
										Job job = jobs.get(jobIndex);
										WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId());
										AjaxLink<Void> jobLink = new AjaxLink<Void>("job") {
	
											@Override
											public void onClick(AjaxRequestTarget target) {
												String position = getPosition("jobs/" + job.getName());
												context.pushState(target, context.getBlobIdent(), position);
												newJobViewer(target, jobIndex);
											}
											
										};
										
										String label = HtmlEscape.escapeHtml5(job.getName());
										if (!buildSpec.getJobs().contains(job)) {
											String iconHtml = String.format(
													"<svg class='icon imported'><use xlink:href='%s'/></svg>", 
													SpriteImage.getVersionedHref(IconScope.class, "arrow2"));
											label += iconHtml;
										} 
										jobLink.add(new Label("label", label).setEscapeModelStrings(false));
										
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

											@Override
											protected String getTriggerChain() {
												return UUID.randomUUID().toString();
											}
	
										});
										navsView.add(nav);
									}
									add(navsView);
	
									newJobViewer(null, getActiveNamedElementIndex(context, Job.class, jobs));
									
									add(AttributeAppender.append("class", "jobs elements d-flex flex-nowrap"));
								}
	
								private void newJobViewer(@Nullable AjaxRequestTarget target, int jobIndex) {
									WebMarkupContainer jobContainer = new WebMarkupContainer("job") {
										
										@Override
										public void renderHead(IHeaderResponse response) {
											super.renderHead(response);
											
											String script = String.format("onedev.server.buildSpec.onNamedElementDomReady(%d);", jobIndex);
											response.render(OnDomReadyHeaderItem.forScript(script));
										}
										
									};
									Job job = jobs.get(jobIndex);
									jobContainer.add(new NamedElementImportNoticePanel<Job>(buildSpec, job.getName()) {

										@Override
										protected Map<String, Job> getElementMap(BuildSpec buildSpec) {
											return buildSpec.getJobMap();
										}
										
									});
									jobContainer.add(BeanContext.view("content", job));
									jobContainer.setOutputMarkupId(true);
									
									if (target != null) {
										replace(jobContainer);
										target.add(jobContainer);
									} else {
										add(jobContainer);
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
						List<Service> services = getNamedElementsToShow(buildSpec.getServices(), buildSpec.getServiceMap());
						if (!services.isEmpty()) {
							servicesViewer = new Fragment("content", "servicesFrag", BuildSpecBlobViewPanel.this) {
	
								@Override
								protected void onInitialize() {
									super.onInitialize();
									
									RepeatingView navsView = new RepeatingView("navs");
									for (int i=0; i<services.size(); i++) {
										int serviceIndex = i;
										Service service = services.get(serviceIndex);
										WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId());
										AjaxLink<Void> serviceLink = new AjaxLink<Void>("service") {
	
											@Override
											public void onClick(AjaxRequestTarget target) {
												String position = getPosition("services/" + service.getName());
												context.pushState(target, context.getBlobIdent(), position);
												newServiceViewer(target, serviceIndex);
											}
											
										};
										
										String label = HtmlEscape.escapeHtml5(service.getName());
										if (!buildSpec.getServices().contains(service)) {
											String iconHtml = String.format(
													"<svg class='icon imported'><use xlink:href='%s'/></svg>", 
													SpriteImage.getVersionedHref(IconScope.class, "arrow2"));
											label += iconHtml;
										} 
										serviceLink.add(new Label("label", label).setEscapeModelStrings(false));
										
										nav.add(serviceLink);
										
										navsView.add(nav);
									}
									add(navsView);
	
									newServiceViewer(null, getActiveNamedElementIndex(context, Service.class, services));
									
									add(AttributeAppender.append("class", "services elements d-flex flex-nowrap"));
								}
	
								private void newServiceViewer(@Nullable AjaxRequestTarget target, int serviceIndex) {
									WebMarkupContainer serviceContainer = new WebMarkupContainer("service") {
										
										@Override
										public void renderHead(IHeaderResponse response) {
											super.renderHead(response);
											
											String script = String.format("onedev.server.buildSpec.onNamedElementDomReady(%d);", serviceIndex);
											response.render(OnDomReadyHeaderItem.forScript(script));
										}
										
									};
									
									Service service = services.get(serviceIndex);
									
									serviceContainer.add(new NamedElementImportNoticePanel<Service>(buildSpec, service.getName()) {

										@Override
										protected Map<String, Service> getElementMap(BuildSpec buildSpec) {
											return buildSpec.getServiceMap();
										}
										
									});
									
									serviceContainer.add(BeanContext.view("content", service));
									serviceContainer.setOutputMarkupId(true);
									
									if (target != null) {
										replace(serviceContainer);
										target.add(serviceContainer);
									} else {
										add(serviceContainer);
									}
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
						List<StepTemplate> templates = getNamedElementsToShow(buildSpec.getStepTemplates(), buildSpec.getStepTemplateMap());
						if (!templates.isEmpty()) {
							templatesViewer = new Fragment("content", "stepTemplatesFrag", BuildSpecBlobViewPanel.this) {
	
								@Override
								protected void onInitialize() {
									super.onInitialize();
									
									RepeatingView navsView = new RepeatingView("navs");
									for (int i=0; i<templates.size(); i++) {
										int templateIndex = i;
										StepTemplate template = templates.get(templateIndex);
										WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId());
										AjaxLink<Void> templateLink = new AjaxLink<Void>("template") {
	
											@Override
											public void onClick(AjaxRequestTarget target) {
												String position = getPosition("step-templates/" + template.getName());
												context.pushState(target, context.getBlobIdent(), position);
												newTemplateViewer(target, templateIndex);
											}
											
										};
										
										String label = HtmlEscape.escapeHtml5(template.getName());
										if (!buildSpec.getStepTemplates().contains(template)) {
											String iconHtml = String.format(
													"<svg class='icon imported'><use xlink:href='%s'/></svg>", 
													SpriteImage.getVersionedHref(IconScope.class, "arrow2"));
											label += iconHtml;
										} 
										templateLink.add(new Label("label", label).setEscapeModelStrings(false));
										
										nav.add(templateLink);
										
										navsView.add(nav);
									}
									add(navsView);
	
									newTemplateViewer(null, getActiveNamedElementIndex(context, StepTemplate.class, templates));
									
									add(AttributeAppender.append("class", "step-templates elements d-flex flex-nowrap"));
								}
	
								private void newTemplateViewer(@Nullable AjaxRequestTarget target, int templateIndex) {
									WebMarkupContainer templateContainer = new WebMarkupContainer("template") {
										
										@Override
										public void renderHead(IHeaderResponse response) {
											super.renderHead(response);
											
											String script = String.format("onedev.server.buildSpec.onNamedElementDomReady(%d);", templateIndex);
											response.render(OnDomReadyHeaderItem.forScript(script));
										}
										
									};
									
									StepTemplate template = templates.get(templateIndex);
									
									templateContainer.add(new NamedElementImportNoticePanel<StepTemplate>(buildSpec, template.getName()) {

										@Override
										protected Map<String, StepTemplate> getElementMap(BuildSpec buildSpec) {
											return buildSpec.getStepTemplateMap();
										}
										
									});
									
									templateContainer.add(BeanContext.view("content", template));
									templateContainer.setOutputMarkupId(true);
									
									if (target != null) {
										replace(templateContainer);
										target.add(templateContainer);
									} else {
										add(templateContainer);
									}
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
							importsViewer.add(AttributeAppender.append("class", "imports"));
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
							}
							
						});
						add(new AjaxLink<Void>("services") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("services");
								context.pushState(target, context.getBlobIdent(), position);
								setupServicesViewer(target);
							}
							
						});
						add(new AjaxLink<Void>("stepTemplates") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("step-templates");
								context.pushState(target, context.getBlobIdent(), position);
								setupStepTemplatesViewer(target);
							}
							
						});
						add(new AjaxLink<Void>("properties") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("properties");
								context.pushState(target, context.getBlobIdent(), position);
								setupPropertiesViewer(target);
							}
							
						});
						add(new AjaxLink<Void>("imports") {
	
							@Override
							public void onClick(AjaxRequestTarget target) {
								String position = getPosition("imports");
								context.pushState(target, context.getBlobIdent(), position);
								setupImportsViewer(target);
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

	private abstract class NamedElementImportNoticePanel<T extends NamedElement> extends Fragment {

		private final Class<T> elementClass;
		
		private final String elementName;
		
		private Import aImport;
		
		@SuppressWarnings("unchecked")
		public NamedElementImportNoticePanel(BuildSpec buildSpec, String elementName) {
			super("notice", "namedElementImportNoticeFrag", BuildSpecBlobViewPanel.this);
			
			List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(
					NamedElementImportNoticePanel.class, getClass());
			elementClass = (Class<T>) typeArguments.get(0);
			this.elementName = elementName;
			
			T element = getElementMap(buildSpec).get(elementName);
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
				state.blobIdent = new BlobIdent(aImport.getTag(), BuildSpec.BLOB_PATH, 
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
				link.add(new Label("label", project.getPath() + ":" + aImport.getTag()));
			} else {
				WebMarkupContainer link = new WebMarkupContainer("link");
				link.add(new WebMarkupContainer("label"));
				add(link);
			}
		}
		
		protected abstract Map<String, T> getElementMap(BuildSpec buildSpec);

		@Override
		protected void onConfigure() {
			super.onConfigure();
			setVisible(aImport != null);
		}
		
	}
}
