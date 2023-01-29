package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getActiveElementIndex;
import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer.getUrlSegment;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import io.onedev.server.model.support.build.JobProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Throwables;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.Import;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.ParamSpecAware;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.step.StepTemplate;
import io.onedev.server.migration.VersionedYamlDoc;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.PathNode.Indexed;
import io.onedev.server.util.PathNode.Named;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.pipeline.JobSelectionChange;
import io.onedev.server.web.component.pipeline.PipelinePanel;
import io.onedev.server.web.component.pipeline.Sortable;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.EditCompleteAware;
import io.onedev.server.web.util.AjaxPayload;

@SuppressWarnings("serial")
public class BuildSpecEditPanel extends FormComponentPanel<byte[]> implements BuildSpecAware, EditCompleteAware {

	private final BlobRenderContext context;
	
	private Serializable parseResult;
	
	public BuildSpecEditPanel(String id, BlobRenderContext context, byte[] initialContent) {
		super(id, Model.of(initialContent));
		this.context = context;
		try {
			parseResult = BuildSpec.parse(initialContent);
			if (parseResult == null)
				parseResult = new BuildSpec();
		} catch (Exception e) {
			parseResult = e;
		}
	}
	
	private void resizeWindow(AjaxRequestTarget target) {
		((BasePage)getPage()).resizeWindow(target);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (parseResult instanceof BuildSpec) {
			BuildSpec buildSpec = (BuildSpec) parseResult;
			add(new ParseableFragment("content") {

				private WebMarkupContainer body;
				
				private void setupJobsEditor(@Nullable AjaxRequestTarget target) {
					Fragment jobsEditor = new Fragment("content", "jobsFrag", BuildSpecEditPanel.this) {

						private void notifyJobSelectionChange(AjaxRequestTarget target, Job job) {
							send(this, Broadcast.BREADTH, new JobSelectionChange(target, job));
						}
						
						private void setupJobDetail(@Nullable AjaxRequestTarget target, int jobIndex) {
							Fragment jobsEditor = this;
							
							WebMarkupContainer jobDetail;
							if (jobIndex != -1) {
								jobDetail = new Fragment("detail", "elementDetailFrag", BuildSpecEditPanel.this);
								
								class JobEditor extends BeanEditor implements JobAware {

									public JobEditor(String id, Job job) {
										super(id, new BeanDescriptor(Job.class), Model.of(job));
									}

									@Override
									public Job getJob() {
										return (Job) getConvertedInput();
									}
									
									@Override
									public List<ParamSpec> getParamSpecs() {
										return ((Job) getConvertedInput()).getParamSpecs();
									}
									
								}
								
								BeanEditor jobEditor = new JobEditor("body", buildSpec.getJobs().get(jobIndex)) {
									
									@Override
									public void onEvent(IEvent<?> event) {
										super.onEvent(event);
										if (event.getPayload() instanceof BeanUpdating) {
												PropertyUpdating propertyUpdating = ((BeanUpdating) event.getPayload()).getSource();
											if (propertyUpdating.getPropertyName().equals(Job.PROP_NAME) 
													|| propertyUpdating.getPropertyName().equals(Job.PROP_JOB_DEPENDENCIES)) {
												Job job = (Job) getConvertedInput();
												String selection = "jobs";
												if (StringUtils.isNotBlank(job.getName())) 
													selection += "/" + job.getName();
												int jobIndex = (int) jobDetail.getDefaultModelObject();
												buildSpec.getJobs().set(jobIndex, job);
												
												AjaxRequestTarget target = (AjaxRequestTarget) propertyUpdating.getHandler();
												replaceState(target, selection);
												target.add(jobsEditor.get("pipeline"));
												resizeWindow(target);
											}
										} else if (event.getPayload() instanceof FormSubmitted) {
											int jobIndex = (int) jobDetail.getDefaultModelObject();
											buildSpec.getJobs().set(jobIndex, (Job) getConvertedInput());
										}
									}
																		
								};
								
								jobDetail.add(jobEditor);
							} else {
								jobDetail = new WebMarkupContainer("detail");
								jobDetail.setVisible(false);
							}
							jobDetail.setDefaultModel(Model.of(jobIndex));
							jobDetail.setOutputMarkupPlaceholderTag(true);
							
							if (target != null) {
								replace(jobDetail);
								target.add(jobDetail);
							} else {
								add(jobDetail);
							}
						}
						
						@Override
						protected void onInitialize() {
							super.onInitialize();
							
							Fragment jobsEditor = this;
							
							add(new PipelinePanel("pipeline") {

								private void addJob(AjaxRequestTarget target, Job job) {
									getJobs().add(job);
									pushState(target, "new-job");
									int jobIndex = getJobs().size()-1;
									setupJobDetail(target, jobIndex);
									target.add(jobsEditor);
									resizeWindow(target);
								}
								
								@Override
								protected Component renderJob(String componentId, int jobIndex) {
									WebMarkupContainer jobNav = new Fragment(componentId, "jobFrag", BuildSpecEditPanel.this);
									
									jobNav.add(new AjaxSubmitLink("select") {

										@Override
										protected void onInitialize() {
											super.onInitialize();
											add(new Label("label", new LoadableDetachableModel<String>() {

												@Override
												protected String load() {
													Job job = getJobs().get(jobIndex);
													if (job.getName() != null)
														return HtmlEscape.escapeHtml5(job.getName());
													else
														return "<i>No Name</i>";
												}
												
											}).setEscapeModelStrings(false));
										}

										@Override
										protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
											super.onSubmit(target, form);
											Job job = getJobs().get(jobIndex);
											pushState(target, "jobs/" + job.getName());
											setupJobDetail(target, buildSpec.getJobs().indexOf(job));
											
											notifyJobSelectionChange(target, job);
											resizeWindow(target);
										}

										@Override
										protected void onError(AjaxRequestTarget target, Form<?> form) {
											super.onError(target, form);
											target.add(jobsEditor);
											resizeWindow(target);
										}
										
									});
									
									jobNav.add(new AjaxLink<Void>("delete") {

										@Override
										public void onClick(AjaxRequestTarget target) {
											getJobs().remove(jobIndex);
											
											Component jobDetail = jobsEditor.get("detail");
											
											int activeJobIndex = (int) jobDetail.getDefaultModelObject();
											if (jobIndex == activeJobIndex) {
												if (getJobs().isEmpty()) {
													replaceState(target, "jobs");
													setupJobDetail(target, -1);
												} else {
													replaceState(target, "jobs/" + getJobs().get(0).getName());
													setupJobDetail(target, 0);
												}
											} else if (jobIndex < activeJobIndex) {
												jobDetail.setDefaultModelObject(activeJobIndex-1);
											}
											target.appendJavaScript("onedev.server.form.markDirty($('.build-spec').closest('form'));");
											target.add(jobsEditor.get("pipeline"));
											resizeWindow(target);
										}
										
									});
									
									jobNav.add(AttributeAppender.append("class", "nav btn-group flex-nowrap"));
									
									return jobNav;
								}

								@Override
								protected Component renderAction(String componentId) {
									Fragment fragment = new Fragment(componentId, "addJobFrag", BuildSpecEditPanel.this);
									
									AjaxSubmitLink createLink = new AjaxSubmitLink("create") {

										@Override
										protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
											super.onSubmit(target, form);
											addJob(target, new Job());
										}

										@Override
										protected void onError(AjaxRequestTarget target, Form<?> form) {
											super.onError(target, form);
											target.add(jobsEditor);
											resizeWindow(target);
										}
										
									};
									
									fragment.add(createLink);
									
									List<Job> suggestedJobs = new ArrayList<>();
									
									if (context.getBlobIdent().revision != null) {
										for (JobSuggestion suggestion: OneDev.getExtensions(JobSuggestion.class)) 
											suggestedJobs.addAll(suggestion.suggestJobs(context.getProject(), context.getCommit()));
									}
									
									if (!suggestedJobs.isEmpty()) {
										fragment.add(new MenuLink("suggestions") {

											@Override
											protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
												List<MenuItem> menuItems = new ArrayList<>();
												for (Job job: suggestedJobs) {
													menuItems.add(new MenuItem() {

														@Override
														public String getLabel() {
															return job.getName();
														}

														@Override
														public WebMarkupContainer newLink(String id) {
															return new AjaxSubmitLink(id, createLink.findParent(Form.class)) {

																@Override
																protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
																	super.onSubmit(target, form);
																	addJob(target, job);
																	dropdown.close();
																}

																@Override
																protected void onError(AjaxRequestTarget target, Form<?> form) {
																	super.onError(target, form);
																	target.add(jobsEditor.get("pipeline"));
																	resizeWindow(target);
																}
																
															};
														}
														
													});
												}
												return menuItems;
											}
											
										});
									} else {
										fragment.add(new WebMarkupContainer("suggestions").setVisible(false));
									}
									
									fragment.add(AttributeAppender.append("class", "add-job nav btn-group flex-nowrap"));
									return fragment;
								}

								@Override
								protected List<Job> getJobs() {
									return buildSpec.getJobs();
								}

								@Override
								protected int getActiveJobIndex() {
									return (int) jobsEditor.get("detail").getDefaultModelObject();
								}

								@Override
								protected Sortable getSortable() {
									return new Sortable() {

										@Override
										public void onSort(AjaxRequestTarget target, int fromIndex, int toIndex) {
											if (fromIndex < toIndex) {
												for (int i=0; i<toIndex-fromIndex; i++)  
													Collections.swap(getJobs(), fromIndex+i, fromIndex+i+1);
											} else {
												for (int i=0; i<fromIndex-toIndex; i++) 
													Collections.swap(getJobs(), fromIndex-i, fromIndex-i-1);
											}
											
											Component jobDetail = jobsEditor.get("detail");

											int activeIndex = (int) jobDetail.getDefaultModelObject();
											
											if (fromIndex < activeIndex) {
												if (toIndex >= activeIndex) 
													jobDetail.setDefaultModelObject(activeIndex-1);
											} else if (fromIndex == activeIndex) {
												jobDetail.setDefaultModelObject(toIndex);
											} else {
												if (toIndex <= activeIndex) 
													jobDetail.setDefaultModelObject(activeIndex+1);
											}
											
											target.add(jobsEditor.get("pipeline"));
											resizeWindow(target);
										}
										
									};
								}
								
							}.setOutputMarkupId(true));
							
							if (!buildSpec.getJobs().isEmpty())
								setupJobDetail(null, getActiveElementIndex(context, Job.class, buildSpec.getJobs(), 0));
							else
								setupJobDetail(null, -1);
							
							add(AttributeAppender.append("class", "elements d-flex flex-nowrap jobs"));
							
							setOutputMarkupId(true);
						}
						
						@Override
						public void renderHead(IHeaderResponse response) {
							super.renderHead(response);
							response.render(OnDomReadyHeaderItem.forScript(
									String.format("onedev.server.buildSpec.onTabDomReady('.jobs');")));
						}
						
					};
					
					if (target != null) {
						body.replace(jobsEditor);
						target.add(jobsEditor);
					} else {
						body.add(jobsEditor);
					}
				}
				
				private void setupStepTemplatesEditor(@Nullable AjaxRequestTarget target) {
					Fragment stepTemplatesEditor = new ElementsEditor<StepTemplate>() {

						@Override
						protected List<StepTemplate> getElements() {
							return buildSpec.getStepTemplates();
						}

						@Override
						protected BeanEditor newElementEditor(String componentId, StepTemplate element) {
							
							class StepTemplateEditor extends BeanEditor implements ParamSpecAware {

								public StepTemplateEditor(String id, NamedElement element) {
									super(id, new BeanDescriptor(StepTemplate.class), Model.of(element));
								}

								@Override
								public List<ParamSpec> getParamSpecs() {
									return ((StepTemplate) getConvertedInput()).getParamSpecs();
								}
								
							}
							
							return new StepTemplateEditor(componentId, element);
						}

					};
					
					if (target != null) {
						body.replace(stepTemplatesEditor);
						target.add(stepTemplatesEditor);
					} else {
						body.add(stepTemplatesEditor);
					}
				}
				
				private void setupServicesEditor(@Nullable AjaxRequestTarget target) {
					Fragment servicesEditor = new ElementsEditor<Service>() {

						@Override
						protected List<Service> getElements() {
							return buildSpec.getServices();
						}

						@Override
						protected BeanEditor newElementEditor(String componentId, Service element) {
							return new BeanEditor(componentId, new BeanDescriptor(Service.class), Model.of(element));
						}

					};
					
					if (target != null) {
						body.replace(servicesEditor);
						target.add(servicesEditor);
					} else {
						body.add(servicesEditor);
					}
				}
				
				private void setupPropertiesEditor(@Nullable AjaxRequestTarget target) {
					PropertyEditor<Serializable> propertiesEditor = PropertyContext.edit("content", buildSpec, "properties");
					propertiesEditor.add(new Behavior() {

						@SuppressWarnings("unchecked")
						@Override
						public void onEvent(Component component, IEvent<?> event) {
							super.onEvent(component, event);
							if (event.getPayload() instanceof FormSubmitted) 
								buildSpec.setProperties((List<JobProperty>) propertiesEditor.getConvertedInput());
						}

						@Override
						public void renderHead(Component component, IHeaderResponse response) {
							super.renderHead(component, response);
							response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.properties');"));
						}
						
					});
					propertiesEditor.add(AttributeAppender.append("class", "properties pr-3"));
					propertiesEditor.setOutputMarkupId(true);
					if (target != null) {
						body.replace(propertiesEditor);
						target.add(propertiesEditor);
					} else {
						body.add(propertiesEditor);
					}
				}
				
				private void setupImportsEditor(@Nullable AjaxRequestTarget target) {
					PropertyEditor<Serializable> importsEditor = PropertyContext.edit("content", buildSpec, "imports");
					importsEditor.add(new Behavior() {

						@SuppressWarnings("unchecked")
						@Override
						public void onEvent(Component component, IEvent<?> event) {
							super.onEvent(component, event);
							if (event.getPayload() instanceof FormSubmitted) 
								buildSpec.setImports((List<Import>) importsEditor.getConvertedInput());
						}

						@Override
						public void renderHead(Component component, IHeaderResponse response) {
							super.renderHead(component, response);
							response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.imports');"));
						}
						
					});
					importsEditor.add(AttributeAppender.append("class", "imports pr-3"));
					importsEditor.setOutputMarkupId(true);
					if (target != null) {
						body.replace(importsEditor);
						target.add(importsEditor);
					} else {
						body.add(importsEditor);
					}
				}

				@Override
				protected void onInitialize() {
					super.onInitialize();
					
					add(new AjaxSubmitLink("jobs") {

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "jobs");
							setupJobsEditor(target);
							resizeWindow(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
							resizeWindow(target);
						}
						
					});
					
					add(new AjaxSubmitLink("services") {

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "services");
							setupServicesEditor(target);
							resizeWindow(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
							resizeWindow(target);
						}
						
					});
					
					add(new AjaxSubmitLink("stepTemplates") {

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "step-templates");
							setupStepTemplatesEditor(target);
							resizeWindow(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
							resizeWindow(target);
						}
						
					});

					add(new AjaxSubmitLink("properties") {
						
						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "properties");
							setupPropertiesEditor(target);
							resizeWindow(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
							resizeWindow(target);
						}
						
					});
					
					add(new AjaxSubmitLink("imports") {
						
						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "imports");
							setupImportsEditor(target);
							resizeWindow(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
							resizeWindow(target);
						}
						
					});
					
					add(body = new WebMarkupContainer("body"));
					body.add(new FencedFeedbackPanel("feedback", body));

					String selection = getSelection();
					
					if (selection == null || selection.startsWith("jobs") || selection.equals("new-job"))
						setupJobsEditor(null);
					else if (selection.startsWith("services") || selection.equals("new-service"))
						setupServicesEditor(null);
					else if (selection.startsWith("step-templates") || selection.equals("new-step-template"))
						setupStepTemplatesEditor(null);
					else if (selection.equals("imports"))
						setupImportsEditor(null);
					else
						setupPropertiesEditor(null);
					
					add(AttributeAppender.append("class", "parseable"));
					setOutputMarkupId(true);
				}

				@SuppressWarnings("rawtypes")
				@Override
				public boolean onEditComplete(AjaxRequestTarget target) {
					BuildSpec buildSpec = (BuildSpec) parseResult;
					Validator validator = AppLoader.getInstance(Validator.class);
					Collection<ConstraintViolation<BuildSpec>> violations = validator.validate(buildSpec);
					if (!violations.isEmpty()) {
						ConstraintViolation<BuildSpec> violation = violations.iterator().next();
						Path path = new Path(violation.getPropertyPath());
						if (path.getNodes().isEmpty()) {
							error(violation.getMessage());
						} else {
							PathNode.Named named = (Named) path.getNodes().iterator().next();
							switch (named.getName()) {
							case "jobs":
								path = new Path(path.getNodes().subList(1, path.getNodes().size()));
								if (path.getNodes().isEmpty()) {
									replaceState(target, named.getName());
									setupJobsEditor(target);
									body.error(violation.getMessage());
								} else {
									PathNode.Indexed indexed = (Indexed) path.getNodes().iterator().next();
									path = new Path(path.getNodes().subList(1, path.getNodes().size()));
									String selection = "jobs/" + buildSpec.getJobs().get(indexed.getIndex()).getName();
									replaceState(target, selection);
									setupJobsEditor(target);
									((BeanEditor)body.get("content:detail:body")).error(path, violation.getMessage());
								}
								break;
							case "services":
								path = new Path(path.getNodes().subList(1, path.getNodes().size()));
								if (path.getNodes().isEmpty()) {
									replaceState(target, named.getName());
									setupServicesEditor(target);
									body.error(violation.getMessage());
								} else {
									PathNode.Indexed indexed = (Indexed) path.getNodes().iterator().next();
									path = new Path(path.getNodes().subList(1, path.getNodes().size()));
									String selection = "services/" + buildSpec.getServices().get(indexed.getIndex()).getName();
									replaceState(target, selection);
									setupServicesEditor(target);
									((BeanEditor)body.get("content:detail:body")).error(path, violation.getMessage());
								}
								break;
							case "stepTemplates":
								path = new Path(path.getNodes().subList(1, path.getNodes().size()));
								if (path.getNodes().isEmpty()) {
									replaceState(target, named.getName());
									setupStepTemplatesEditor(target);
									body.error(violation.getMessage());
								} else {
									PathNode.Indexed indexed = (Indexed) path.getNodes().iterator().next();
									path = new Path(path.getNodes().subList(1, path.getNodes().size()));
									String selection = "step-templates/" + buildSpec.getStepTemplates().get(indexed.getIndex()).getName();
									replaceState(target, selection);
									setupStepTemplatesEditor(target);
									((BeanEditor)body.get("content:detail:body")).error(path, violation.getMessage());
								}
								break;
							case "properties":
								path = new Path(path.getNodes().subList(1, path.getNodes().size()));
								replaceState(target, "properties");
								setupPropertiesEditor(target);
								((PropertyEditor)body.get("content")).error(path, violation.getMessage());
								break;
							case "imports":
								path = new Path(path.getNodes().subList(1, path.getNodes().size()));
								replaceState(target, "imports");
								setupImportsEditor(target);
								((PropertyEditor)body.get("content")).error(path, violation.getMessage());
								break;
							default:
								throw new RuntimeException("Unexpected element name: " + named.getName());
							}
						}
						return false;
					} else {
						fixState(target);
						return true;
					}
				}
				
				@Override
				public void onEditCancel(AjaxRequestTarget target) {
					fixState(target);
				}
				
				private void fixState(AjaxRequestTarget target) {
					String selection = getSelection();
					if (selection == null || selection.startsWith("jobs")) {
						int jobIndex = getActiveElementIndex(context, Job.class, buildSpec.getJobs(), 0);
						if (jobIndex < buildSpec.getJobs().size())
							replaceState(target, "jobs/" + buildSpec.getJobs().get(jobIndex).getName());
					} else if (selection.startsWith("services")) {
						int serviceIndex = getActiveElementIndex(context, Service.class, buildSpec.getServices(), 0);
						if (serviceIndex < buildSpec.getServices().size())
							replaceState(target, "services/" + buildSpec.getServices().get(serviceIndex).getName());
					} else if (selection.startsWith("step-templates")) {
						int stepTemplateIndex = getActiveElementIndex(context, StepTemplate.class, buildSpec.getStepTemplates(), 0);
						if (stepTemplateIndex < buildSpec.getStepTemplates().size())
							replaceState(target, "step-templates/" + buildSpec.getStepTemplates().get(stepTemplateIndex).getName());
					} else if (selection.equals("new-job")) { 
						replaceState(target, "jobs");
					} else if (selection.equals("new-service")) {
						replaceState(target, "services");
					} else if (selection.equals("new-step-template")) {
						replaceState(target, "step-templates");
					}
				}
				
			});
		} else {
			Fragment unparseableFrag = new Fragment("content", "unparseableFrag", this);
			unparseableFrag.add(new MultilineLabel("errorMessage", 
					Throwables.getStackTraceAsString((Throwable) parseResult)));
			unparseableFrag.add(AttributeAppender.append("class", "unparseable"));
			add(unparseableFrag);
		}
		
	}

	@Override
	public void convertInput() {
		if (parseResult instanceof BuildSpec) {
			AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
			send(this, Broadcast.BREADTH, new FormSubmitted(target));
			setConvertedInput(VersionedYamlDoc.fromBean(parseResult).toYaml().getBytes(StandardCharsets.UTF_8));
		} else { 
			setConvertedInput(getModelObject());
		}
	}
	
	private void pushState(AjaxRequestTarget target, String selection) {
		String position = BuildSpecRenderer.getPosition(selection);
		context.pushState(target, context.getBlobIdent(), position);
	}

	private void replaceState(AjaxRequestTarget target, String selection) {
		String position = BuildSpecRenderer.getPosition(selection);
		context.replaceState(target, context.getBlobIdent(), position);
	}
	
	@Nullable
	private String getSelection() {
		return BuildSpecRenderer.getSelection(context.getPosition());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BuildSpecResourceReference()));
	}

	@Override
	public BuildSpec getBuildSpec() {
		if (parseResult instanceof BuildSpec) 
			return (BuildSpec) parseResult;
		else 
			return null;
	}
	
	private static class FormSubmitted extends AjaxPayload {

		public FormSubmitted(AjaxRequestTarget target) {
			super(target);
		}

	}

	@Override
	public boolean onEditComplete(AjaxRequestTarget target) {
		Component content = get("content");
		if (content instanceof EditCompleteAware)
			return ((EditCompleteAware) content).onEditComplete(target);
		else
			return true;
	}
	
	@Override
	public void onEditCancel(AjaxRequestTarget target) {
		Component content = get("content");
		if (content instanceof EditCompleteAware)
			((EditCompleteAware) content).onEditCancel(target);
	}
	
	private abstract class ParseableFragment extends Fragment implements EditCompleteAware {

		public ParseableFragment(String id) {
			super(id, "parseableFrag", BuildSpecEditPanel.this);
		}
		
	}
	
	private abstract class ElementsEditor<T extends NamedElement> extends Fragment {
		
		private final Class<T> elementClass;
		
		@SuppressWarnings("unchecked")
		public ElementsEditor() {
			super("content", "elementsFrag", BuildSpecEditPanel.this);
			
			List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(ElementsEditor.class, getClass());
			elementClass = (Class<T>) typeArguments.get(0);
		}

		protected abstract List<T> getElements();
		
		protected abstract BeanEditor newElementEditor(String componentId, T element);
		
		private void setupElementDetail(@Nullable AjaxRequestTarget target, int elementIndex) {
			WebMarkupContainer elementDetail;
			
			if (elementIndex != -1) {
				elementDetail = new Fragment("detail", "elementDetailFrag", BuildSpecEditPanel.this);
				elementDetail.setDefaultModel(Model.of(elementIndex));
				
				BeanEditor elementEditor = newElementEditor("body", getElements().get(elementIndex));
				
				elementEditor.add(new Behavior() {
					
					@SuppressWarnings("unchecked")
					@Override
					public void onEvent(Component component, IEvent<?> event) {
						super.onEvent(component, event);
						if (event.getPayload() instanceof BeanUpdating) {
							PropertyUpdating propertyUpdating = ((BeanUpdating) event.getPayload()).getSource();
							if (propertyUpdating.getPropertyName().equals(Job.PROP_NAME)) {
								T element = (T) elementEditor.getConvertedInput();
								String selection = getUrlSegment(elementClass) + "s";
								if (StringUtils.isNotBlank(element.getName())) 
									selection += "/" + element.getName();
								int elementIndex = (int) elementDetail.getDefaultModelObject();
								getElements().set(elementIndex, element);
								
								AjaxRequestTarget target = (AjaxRequestTarget) propertyUpdating.getHandler();
								replaceState(target, selection);
								target.add(ElementsEditor.this.get("navs"));
								resizeWindow(target);
							}
						} else if (event.getPayload() instanceof FormSubmitted) {
							int elementIndex = (int) elementDetail.getDefaultModelObject();
							getElements().set(elementIndex, (T) elementEditor.getConvertedInput());
						}
					}
					
				});
				elementDetail.add(elementEditor);
				elementDetail.setOutputMarkupId(true);
			} else {
				elementDetail = new WebMarkupContainer("detail");
				elementDetail.setOutputMarkupPlaceholderTag(true).setVisible(false);
			}
			elementDetail.setDefaultModel(Model.of(elementIndex));
			
			if (target != null) {
				replace(elementDetail);
				target.add(elementDetail);
			} else {
				add(elementDetail);
			}
		}
		
		@Override
		protected void onInitialize() {
			super.onInitialize();

			WebMarkupContainer navs = new WebMarkupContainer("navs") {

				@Override
				public void renderHead(IHeaderResponse response) {
					super.renderHead(response);
					
					int elementIndex = (int) ElementsEditor.this.get("detail").getDefaultModelObject();
					String script = String.format("onedev.server.buildSpec.markElementActive(%d);", elementIndex);
					response.render(OnDomReadyHeaderItem.forScript(script));
				}
				
			};
			
			navs.add(new ListView<T>("navs", new AbstractReadOnlyModel<List<T>>() {

				@Override
				public List<T> getObject() {
					return getElements();
				}
				
			}) {

				@Override
				protected void populateItem(ListItem<T> item) {
					item.add(new AjaxSubmitLink("select") {

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(new Label("label", new AbstractReadOnlyModel<String>() {

								@Override
								public String getObject() {
									T element = getElements().get(item.getIndex());
									if (element.getName() != null)
										return HtmlEscape.escapeHtml5(element.getName());
									else
										return "<i>No Name</i>";
								}
								
							}).setEscapeModelStrings(false));
						}

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, getUrlSegment(elementClass) + "s/" + item.getModelObject().getName());
							setupElementDetail(target, item.getIndex());
							target.appendJavaScript(String.format(
									"onedev.server.buildSpec.markElementActive(%d);", item.getIndex()));
							resizeWindow(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(ElementsEditor.this);
							resizeWindow(target);
						}
						
					});
					
					item.add(new AjaxLink<Void>("delete") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							getElements().remove(item.getIndex());
							
							Component elementDetail = ElementsEditor.this.get("detail");
							
							int activeElementIndex = (int) elementDetail.getDefaultModelObject();
							if (item.getIndex() == activeElementIndex) {
								if (getElements().isEmpty()) {
									replaceState(target, getUrlSegment(elementClass) + "s");
									setupElementDetail(target, -1);
								} else {
									replaceState(target, getUrlSegment(elementClass) + "s/" + getElements().get(0).getName());
									setupElementDetail(target, 0);
								}
							} else if (item.getIndex() < activeElementIndex) {
								elementDetail.setDefaultModelObject(activeElementIndex-1);
							}
							target.appendJavaScript("onedev.server.form.markDirty($('.build-spec').closest('form'));");
							target.add(ElementsEditor.this.get("navs"));
							resizeWindow(target);
						}
						
					});
				}
				
			});
			
			if (!getElements().isEmpty()) {
				int elementIndex = getActiveElementIndex(context, elementClass, getElements(), 0);
				setupElementDetail(null, elementIndex);
			} else {
				setupElementDetail(null, -1);
			}
			
			navs.add(new SortBehavior() {

				@Override
				protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
					int fromIndex = from.getItemIndex();
					int toIndex = to.getItemIndex();
					
					if (fromIndex < toIndex) {
						for (int i=0; i<toIndex-fromIndex; i++)  
							Collections.swap(getElements(), fromIndex+i, fromIndex+i+1);
					} else {
						for (int i=0; i<fromIndex-toIndex; i++) 
							Collections.swap(getElements(), fromIndex-i, fromIndex-i-1);
					}
					
					Component jobDetail = get("detail");

					int activeIndex = (int) jobDetail.getDefaultModelObject();
					
					if (fromIndex < activeIndex) {
						if (toIndex >= activeIndex) 
							jobDetail.setDefaultModelObject(activeIndex-1);
					} else if (fromIndex == activeIndex) {
						jobDetail.setDefaultModelObject(toIndex);
					} else {
						if (toIndex <= activeIndex) 
							jobDetail.setDefaultModelObject(activeIndex+1);
					}
					
					target.add(get("navs"));
					resizeWindow(target);
				}
				
			}.sortable(null));
			
			add(navs.setOutputMarkupId(true));
			
			AjaxSubmitLink createLink = new AjaxSubmitLink("create") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					T newElement;
					try {
						newElement = elementClass.getDeclaredConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
					
					getElements().add(newElement);
					pushState(target, "new-" +  getUrlSegment(elementClass));
					int elementIndex = getElements().size()-1;
					setupElementDetail(target, elementIndex);
					target.add(ElementsEditor.this);
					resizeWindow(target);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(ElementsEditor.this);
					resizeWindow(target);
				}
				
			};
			
			add(createLink);
			
			add(AttributeAppender.append("class", "elements d-flex flex-nowrap " + getUrlSegment(elementClass) + "s"));
			setOutputMarkupId(true);
		}
		
		@Override
		public void renderHead(IHeaderResponse response) {
			super.renderHead(response);
			response.render(OnDomReadyHeaderItem.forScript(
					String.format("onedev.server.buildSpec.onTabDomReady('.%ss');", getUrlSegment(elementClass))));
		}
		
	}
	
}
