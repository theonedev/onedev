package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.base.Throwables;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.Property;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.migration.VersionedYamlDoc;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.PathNode.Indexed;
import io.onedev.server.util.PathNode.Named;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyEditor;
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
			add(new ValidFragment("content") {

				private void newJobsEdit(@Nullable AjaxRequestTarget target, int activeJobIndex) {
					Fragment jobsEdit = new Fragment("body", "jobsFrag", BuildSpecEditPanel.this) {

						private void newJobNav(@Nullable AjaxRequestTarget target, RepeatingView navsView, int jobIndex) {
							WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId(), Model.of(jobIndex));
							
							Fragment jobsEdit = this;
							
							nav.add(new AjaxSubmitLink("job") {

								private int getJobIndex() {
									return (int) nav.getDefaultModelObject();
								}
								
								@Override
								protected void onInitialize() {
									super.onInitialize();
									add(new Label("label", new AbstractReadOnlyModel<String>() {

										@Override
										public String getObject() {
											return getJob().getName();
										}
										
									}));
								}

								private Job getJob() {
									return buildSpec.getJobs().get(getJobIndex());
								}
								
								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);
									pushState(target, "jobs/" + getJob().getName());
									newJobEdit(target, nav);
									resizeWindow(target);
								}

								@Override
								protected void onError(AjaxRequestTarget target, Form<?> form) {
									super.onError(target, form);
									target.add(jobsEdit);
									resizeWindow(target);
								}
								
							});
							
							nav.add(new AjaxLink<Void>("deleteJob") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									int jobIndex = (int) nav.getDefaultModelObject();
									Component jobEdit = jobsEdit.get("job");
									boolean found = false;
									for (Iterator<Component> it = navsView.iterator(); it.hasNext();) {
										Component child = it.next();
										if (child == nav) {
											it.remove();
											found = true;
										} else if (found) {
											child.setDefaultModelObject((int)child.getDefaultModelObject()-1);
										}
									}
									target.appendJavaScript(String.format(
											"$('.build-spec>.jobs>.side>.navs>.nav:eq(%d)').remove();", 
											jobIndex));
									buildSpec.getJobs().remove(jobIndex);
									int activeJobIndex = buildSpec.getJobs().indexOf(jobEdit.getDefaultModelObject());
									if (activeJobIndex == -1) { 
										if (!buildSpec.getJobs().isEmpty()) {
											newJobEdit(target, navsView.iterator().next());
										} else {
											jobEdit = new WebMarkupContainer("job");
											jobEdit.setOutputMarkupId(true);
											jobsEdit.replace(jobEdit);
											target.add(jobEdit);
										}
									}
								}
								
							});
							
							navsView.add(nav);
							
							if (target != null) {
								String script = String.format(
										"$('.build-spec>.jobs>.side>.navs').append(\"<div id='%s'></div>\");", 
										nav.getMarkupId(true));
								target.prependJavaScript(script);
								target.add(nav);
							}
						}
						
						private void newJobEdit(@Nullable AjaxRequestTarget target, Component nav) {
							AbstractPostAjaxBehavior nameChangeBehavior = new AbstractPostAjaxBehavior() {
								
								@Override
								protected void respond(AjaxRequestTarget target) {
									IRequestParameters params = RequestCycle.get().getRequest().getRequestParameters();
									String name = params.getParameterValue("name").toOptionalString();
									
									String selection = "jobs";
									if (StringUtils.isNotBlank(name)) 
										selection += "/" + name;
									replaceState(target, selection);
								}
								
							};
							
							int jobIndex = (int) nav.getDefaultModelObject();
							BeanEditor jobEdit = new JobEditor("job", buildSpec.getJobs().get(jobIndex)) {
								
								@Override
								public void onEvent(IEvent<?> event) {
									super.onEvent(event);
									if (event.getPayload() instanceof FormSubmitted) {
										int jobIndex = (int) nav.getDefaultModelObject();
										buildSpec.getJobs().set(jobIndex, getJob());
									}
								}
								
								@Override
								public void renderHead(IHeaderResponse response) {
									super.renderHead(response);
									
									int jobIndex = (int) nav.getDefaultModelObject();
									CharSequence callback = nameChangeBehavior.getCallbackFunction(CallbackParameter.explicit("name"));
									String script = String.format("onedev.server.buildSpec.onJobDomReady(%d, %s);", jobIndex, callback);
									response.render(OnDomReadyHeaderItem.forScript(script));
								}
								
							};
							jobEdit.add(nameChangeBehavior);

							jobEdit.setOutputMarkupId(true);
							if (target != null) {
								replace(jobEdit);
								target.add(jobEdit);
							} else {
								add(jobEdit);
							}
						}
						
						@SuppressWarnings("deprecation")
						private void addJob(AjaxRequestTarget target, RepeatingView jobNavs) {
							pushState(target, "new-job");
							int jobIndex = buildSpec.getJobs().size()-1;
							newJobNav(target, jobNavs, jobIndex);
							newJobEdit(target, jobNavs.get(jobNavs.size()-1));
						}
						
						@SuppressWarnings("deprecation")
						@Override
						protected void onInitialize() {
							super.onInitialize();
							
							Fragment jobsEdit = this;
							
							RepeatingView jobNavs = new RepeatingView("navs");
							
							for (int i=0; i<buildSpec.getJobs().size(); i++)
								newJobNav(null, jobNavs, i);
							
							add(jobNavs);
							
							if (!buildSpec.getJobs().isEmpty()) {
								int jobIndex = activeJobIndex;
								if (jobIndex == -1)
									jobIndex = BuildSpecRendererProvider.getActiveJobIndex(context, buildSpec);
								newJobEdit(null, jobNavs.get(jobIndex));
							} else {
								add(new WebMarkupContainer("job").setOutputMarkupId(true));
							}
							
							List<Job> suggestedJobs = new ArrayList<>();
							
							if (context.getBlobIdent().revision != null) {
								for (JobSuggestion suggestion: OneDev.getExtensions(JobSuggestion.class)) 
									suggestedJobs.addAll(suggestion.suggestJobs(context.getProject(), context.getCommit()));
							}

							AjaxSubmitLink createLink = new AjaxSubmitLink("create") {

								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);
									buildSpec.getJobs().add(new Job());
									addJob(target, jobNavs);
									resizeWindow(target);
								}

								@Override
								protected void onError(AjaxRequestTarget target, Form<?> form) {
									super.onError(target, form);
									target.add(jobsEdit);
									resizeWindow(target);
								}
								
							};
							if (suggestedJobs.isEmpty())
								createLink.add(AttributeAppender.append("class", "no-suggestions"));
							
							add(createLink);
							
							if (!suggestedJobs.isEmpty()) {
								add(new MenuLink("suggestions") {

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
															buildSpec.getJobs().add(job);
															addJob(target, jobNavs);
															resizeWindow(target);
														}

														@Override
														protected void onError(AjaxRequestTarget target, Form<?> form) {
															super.onError(target, form);
															dropdown.close();
															target.add(jobsEdit);
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
								add(new WebMarkupContainer("suggestions").setVisible(false));
							}
							
							add(new SortBehavior() {

								@Override
								protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
									int fromIndex = from.getItemIndex();
									int toIndex = to.getItemIndex();
									if (fromIndex < toIndex) {
										for (int i=0; i<toIndex-fromIndex; i++) { 
											jobNavs.swap(fromIndex+i, fromIndex+i+1);
											Collections.swap(buildSpec.getJobs(), fromIndex+i, fromIndex+i+1);
										}
									} else {
										for (int i=0; i<fromIndex-toIndex; i++) {
											jobNavs.swap(fromIndex-i, fromIndex-i-1);
											Collections.swap(buildSpec.getJobs(), fromIndex-i, fromIndex-i-1);
										}
									}
									for (int i=0; i<jobNavs.size(); i++)
										jobNavs.get(i).setDefaultModelObject(i);
								}
								
							}.sortable(".side>.navs"));
						}
						
						@Override
						public void renderHead(IHeaderResponse response) {
							super.renderHead(response);
							response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.jobs');"));
						}
						
					};
					
					jobsEdit.add(AttributeAppender.append("class", "jobs d-flex flex-nowrap"));
					jobsEdit.setOutputMarkupId(true);
					
					if (target != null) {
						replace(jobsEdit);
						target.add(jobsEdit);
					} else {
						add(jobsEdit);
					}
				}
				
				private void newPropertiesEdit(@Nullable AjaxRequestTarget target) {
					PropertyEditor<Serializable> propertiesEdit = PropertyContext.edit("body", buildSpec, "properties");
					propertiesEdit.add(new Behavior() {

						@SuppressWarnings("unchecked")
						@Override
						public void onEvent(Component component, IEvent<?> event) {
							super.onEvent(component, event);
							if (event.getPayload() instanceof FormSubmitted) 
								buildSpec.setProperties((List<Property>) propertiesEdit.getConvertedInput());
						}

						@Override
						public void renderHead(Component component, IHeaderResponse response) {
							super.renderHead(component, response);
							response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.properties');"));
						}
						
					});
					propertiesEdit.add(AttributeAppender.append("class", "properties d-flex"));
					propertiesEdit.setOutputMarkupId(true);
					if (target != null) {
						replace(propertiesEdit);
						target.add(propertiesEdit);
					} else {
						add(propertiesEdit);
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
							newJobsEdit(target, -1);
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
							newPropertiesEdit(target);
							resizeWindow(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
							resizeWindow(target);
						}
						
					});
					
					String selection = getSelection();
					if (selection == null || selection.startsWith("jobs") || selection.equals("new-job"))
						newJobsEdit(null, -1);
					else
						newPropertiesEdit(null);
					
					add(AttributeAppender.append("class", "valid"));
					setOutputMarkupId(true);
				}

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
									newJobsEdit(target, -1);
									error("Jobs: " + violation.getMessage());
									replaceState(target, "jobs");
								} else {
									PathNode.Indexed indexed = (Indexed) path.getNodes().iterator().next();
									path = new Path(path.getNodes().subList(1, path.getNodes().size()));
									if (path.getNodes().isEmpty()) {
										newJobsEdit(target, -1);
										error("Job '" + buildSpec.getJobs().get(indexed.getIndex()).getName() + "': " + violation.getMessage());
										replaceState(target, "jobs");
									} else {
										newJobsEdit(target, indexed.getIndex());
										((BeanEditor)get("body:job")).error(path, violation.getMessage());
										String selection = "jobs/" + buildSpec.getJobs().get(indexed.getIndex()).getName();
										replaceState(target, selection);
									}
								}
								break;
							default:
								throw new RuntimeException("Unexpected element name: " + named.getName());
							}
						}
						resizeWindow(target);
						return false;
					} else {
						resizeWindow(target);
						return true;
					}
				}
				
				@Override
				public void onEditCancel(AjaxRequestTarget target) {
					if ("new-job".equals(getSelection())) 
						replaceState(target, "jobs");
				}
				
			});
		} else {
			Fragment invalidFrag = new Fragment("content", "invalidFrag", this);
			invalidFrag.add(new MultilineLabel("errorMessage", 
					Throwables.getStackTraceAsString((Throwable) parseResult)));
			invalidFrag.add(AttributeAppender.append("class", "invalid"));
			add(invalidFrag);
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
		String position = BuildSpecRendererProvider.getPosition(selection);
		context.pushState(target, context.getBlobIdent(), position);
	}

	private void replaceState(AjaxRequestTarget target, String selection) {
		String position = BuildSpecRendererProvider.getPosition(selection);
		context.replaceState(target, context.getBlobIdent(), position);
	}
	
	@Nullable
	private String getSelection() {
		return BuildSpecRendererProvider.getSelection(context.getPosition());
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
	
	private static class JobEditor extends BeanEditor implements JobAware {

		public JobEditor(String id, Job job) {
			super(id, new BeanDescriptor(Job.class), Model.of(job));
		}

		@Override
		public Job getJob() {
			return (Job) getConvertedInput();
		}
		
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
	
	private abstract class ValidFragment extends Fragment implements EditCompleteAware {

		public ValidFragment(String id) {
			super(id, "validFrag", BuildSpecEditPanel.this);
		}
		
	}
}
