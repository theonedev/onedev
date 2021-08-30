package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRendererProvider.getActiveNamedElementIndex;
import static io.onedev.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRendererProvider.getUrlSegment;

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
import org.apache.wicket.feedback.FencedFeedbackPanel;
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

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.Import;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.ParamSpecAware;
import io.onedev.server.buildspec.Property;
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
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (parseResult instanceof BuildSpec) {
			BuildSpec buildSpec = (BuildSpec) parseResult;
			add(new ParseableFragment("content") {

				private WebMarkupContainer body;
				
				private void setupJobsEditor(@Nullable AjaxRequestTarget target) {
					Fragment jobsEditor = new NamedElementsEditor<Job>() {

						@Override
						protected List<Job> getElements() {
							return buildSpec.getJobs();
						}

						@Override
						protected List<Job> getSuggestedElements() {
							List<Job> suggestedJobs = new ArrayList<>();
							
							if (context.getBlobIdent().revision != null) {
								for (JobSuggestion suggestion: OneDev.getExtensions(JobSuggestion.class)) 
									suggestedJobs.addAll(suggestion.suggestJobs(context.getProject(), context.getCommit()));
							}
							return suggestedJobs;
						}

						@Override
						protected int getActiveElementIndex() {
							return getActiveNamedElementIndex(context, Job.class, buildSpec.getJobs());
						}

						@Override
						protected BeanEditor newElementEditor(String componentId, Job element) {
							
							class JobEditor extends BeanEditor implements JobAware {

								public JobEditor(String id, NamedElement element) {
									super(id, new BeanDescriptor(Job.class), Model.of(element));
								}

								@Override
								public Job getJob() {
									return (Job) getConvertedInput();
								}

								@Override
								public List<ParamSpec> getParamSpecs() {
									return getJob().getParamSpecs();
								}
								
							}
							
							return new JobEditor(componentId, element);
						}

						@Override
						protected void setElements(List<Job> elements) {
							buildSpec.setJobs(elements);
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
					Fragment stepTemplatesEditor = new NamedElementsEditor<StepTemplate>() {

						@Override
						protected List<StepTemplate> getElements() {
							return buildSpec.getStepTemplates();
						}

						@Override
						protected List<StepTemplate> getSuggestedElements() {
							return new ArrayList<>();
						}

						@Override
						protected int getActiveElementIndex() {
							return getActiveNamedElementIndex(context, StepTemplate.class, buildSpec.getStepTemplates());
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

						@Override
						protected void setElements(List<StepTemplate> elements) {
							buildSpec.setStepTemplates(elements);
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
					Fragment servicesEditor = new NamedElementsEditor<Service>() {

						@Override
						protected List<Service> getElements() {
							return buildSpec.getServices();
						}

						@Override
						protected void setElements(List<Service> elements) {
							buildSpec.setServices(elements);
						}

						@Override
						protected List<Service> getSuggestedElements() {
							return new ArrayList<>();
						}

						@Override
						protected int getActiveElementIndex() {
							return getActiveNamedElementIndex(context, Service.class, buildSpec.getServices());
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
								buildSpec.setProperties((List<Property>) propertiesEditor.getConvertedInput());
						}

						@Override
						public void renderHead(Component component, IHeaderResponse response) {
							super.renderHead(component, response);
							response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.properties');"));
						}
						
					});
					propertiesEditor.add(AttributeAppender.append("class", "properties"));
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
					importsEditor.add(AttributeAppender.append("class", "imports"));
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
							pushState(target,  "jobs");
							setupJobsEditor(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
						}
						
					});
					
					add(new AjaxSubmitLink("services") {

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "services");
							setupServicesEditor(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
						}
						
					});
					
					add(new AjaxSubmitLink("stepTemplates") {

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "step-templates");
							setupStepTemplatesEditor(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
						}
						
					});

					add(new AjaxSubmitLink("properties") {
						
						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "properties");
							setupPropertiesEditor(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
						}
						
					});
					
					add(new AjaxSubmitLink("imports") {
						
						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							pushState(target, "imports");
							setupImportsEditor(target);
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							super.onError(target, form);
							target.add(getParent());
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
									((BeanEditor)body.get("content:element")).error(path, violation.getMessage());
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
									((BeanEditor)body.get("content:element")).error(path, violation.getMessage());
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
									((BeanEditor)body.get("content:element")).error(path, violation.getMessage());
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
						int jobIndex = getActiveNamedElementIndex(context, Job.class, buildSpec.getJobs());
						if (jobIndex < buildSpec.getJobs().size())
							replaceState(target, "jobs/" + buildSpec.getJobs().get(jobIndex).getName());
					} else if (selection.startsWith("services")) {
						int serviceIndex = getActiveNamedElementIndex(context, Service.class, buildSpec.getServices());
						if (serviceIndex < buildSpec.getServices().size())
							replaceState(target, "services/" + buildSpec.getServices().get(serviceIndex).getName());
					} else if (selection.startsWith("step-templates")) {
						int stepTemplateIndex = getActiveNamedElementIndex(context, StepTemplate.class, buildSpec.getStepTemplates());
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
	
	private abstract class NamedElementsEditor<T extends NamedElement> extends Fragment {
		
		private final Class<T> elementClass;
		
		@SuppressWarnings("unchecked")
		public NamedElementsEditor() {
			super("content", "namedElementsFrag", BuildSpecEditPanel.this);
			
			List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(NamedElementsEditor.class, getClass());
			elementClass = (Class<T>) typeArguments.get(0);
		}

		protected abstract List<T> getElements();
		
		protected abstract void setElements(List<T> elements);
		
		protected abstract List<T> getSuggestedElements();
		
		protected abstract int getActiveElementIndex();
		
		protected abstract BeanEditor newElementEditor(String componentId, T element);
		
		private void newElementNav(@Nullable AjaxRequestTarget target, RepeatingView navsView, int elementIndex) {
			WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId(), Model.of(elementIndex));
			
			Fragment elementsEdit = this;
			
			nav.add(new AjaxSubmitLink("element") {

				private int getElementIndex() {
					return (int) nav.getDefaultModelObject();
				}
				
				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new Label("label", new AbstractReadOnlyModel<String>() {

						@Override
						public String getObject() {
							return getElement().getName();
						}
						
					}));
				}

				private NamedElement getElement() {
					return getElements().get(getElementIndex());
				}
				
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					pushState(target, getUrlSegment(elementClass) + "s/" + getElement().getName());
					setupElementEditor(target, nav);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(elementsEdit);
				}
				
			});
			
			nav.add(new AjaxLink<Void>("delete") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					int elementIndex = (int) nav.getDefaultModelObject();
					Component elementEdit = elementsEdit.get("element");
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
							"$('.build-spec>.body>.elements>.side>.navs>.nav:eq(%d)').remove();", 
							elementIndex));
					getElements().remove(elementIndex);
					int activeElementIndex = getElements().indexOf(elementEdit.getDefaultModelObject());
					if (activeElementIndex == -1) {
						replaceState(target, getUrlSegment(elementClass) + "s");
						if (!getElements().isEmpty()) {
							setupElementEditor(target, navsView.iterator().next());
						} else {
							elementEdit = new WebMarkupContainer("element");
							elementEdit.setOutputMarkupId(true);
							elementsEdit.replace(elementEdit);
							target.add(elementEdit);
						}
					}
					target.appendJavaScript("onedev.server.form.markDirty($('.build-spec').closest('form'));");
				}
				
			});
			
			navsView.add(nav);
			
			if (target != null) {
				String script = String.format(
						"$('.build-spec>.body>.elements>.side>.navs').append(\"<div id='%s'></div>\");", 
						nav.getMarkupId(true));
				target.prependJavaScript(script);
				target.add(nav);
			}
		}
		
		private void setupElementEditor(@Nullable AjaxRequestTarget target, Component nav) {
			AbstractPostAjaxBehavior nameChangeBehavior = new AbstractPostAjaxBehavior() {
				
				@Override
				protected void respond(AjaxRequestTarget target) {
					IRequestParameters params = RequestCycle.get().getRequest().getRequestParameters();
					String name = params.getParameterValue("name").toOptionalString();
					
					String selection = getUrlSegment(elementClass) + "s";
					if (StringUtils.isNotBlank(name)) 
						selection += "/" + name;
					replaceState(target, selection);
				}
				
			};
			
			int elementIndex = (int) nav.getDefaultModelObject();
			BeanEditor elementEditor = newElementEditor("element", getElements().get(elementIndex));
			
			elementEditor.add(new Behavior() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void onEvent(Component component, IEvent<?> event) {
					super.onEvent(component, event);
					if (event.getPayload() instanceof FormSubmitted) {
						int elementIndex = (int) nav.getDefaultModelObject();
						List<T> elements = new ArrayList<>(getElements());
						elements.set(elementIndex, (T) elementEditor.getConvertedInput());
						setElements(elements);
					}
				}
				
				@Override
				public void renderHead(Component component, IHeaderResponse response) {
					super.renderHead(component, response);
					
					int elementIndex = (int) nav.getDefaultModelObject();
					CharSequence callback = nameChangeBehavior.getCallbackFunction(CallbackParameter.explicit("name"));
					String script = String.format("onedev.server.buildSpec.onNamedElementDomReady(%d, %s);", elementIndex, callback);
					response.render(OnDomReadyHeaderItem.forScript(script));
				}
				
			});
			elementEditor.add(nameChangeBehavior);

			elementEditor.setOutputMarkupId(true);
			if (target != null) {
				replace(elementEditor);
				target.add(elementEditor);
			} else {
				add(elementEditor);
			}
		}
		
		@SuppressWarnings("deprecation")
		private void addElement(AjaxRequestTarget target, RepeatingView elementNavs) {
			pushState(target, "new-" +  getUrlSegment(elementClass));
			int elementIndex = getElements().size()-1;
			newElementNav(target, elementNavs, elementIndex);
			setupElementEditor(target, elementNavs.get(elementNavs.size()-1));
		}
		
		@SuppressWarnings("deprecation")
		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			RepeatingView elementNavs = new RepeatingView("navs");
			
			for (int i=0; i<getElements().size(); i++)
				newElementNav(null, elementNavs, i);
			
			add(elementNavs);
			
			if (!getElements().isEmpty()) {
				int elementIndex = getActiveElementIndex();
				setupElementEditor(null, elementNavs.get(elementIndex));
			} else {
				add(new WebMarkupContainer("element").setOutputMarkupId(true));
			}
			
			AjaxSubmitLink createLink = new AjaxSubmitLink("create") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					try {
						getElements().add(elementClass.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					addElement(target, elementNavs);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(NamedElementsEditor.this);
				}
				
			};
			
			List<T> suggestedElements = getSuggestedElements();
			
			if (suggestedElements.isEmpty())
				createLink.add(AttributeAppender.append("class", "no-suggestions"));
			
			add(createLink);
			
			if (!suggestedElements.isEmpty()) {
				add(new MenuLink("suggestions") {

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						List<MenuItem> menuItems = new ArrayList<>();
						for (T element: suggestedElements) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return element.getName();
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new AjaxSubmitLink(id, createLink.findParent(Form.class)) {

										@Override
										protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
											super.onSubmit(target, form);
											getElements().add(element);
											addElement(target, elementNavs);
											dropdown.close();
										}

										@Override
										protected void onError(AjaxRequestTarget target, Form<?> form) {
											super.onError(target, form);
											dropdown.close();
											target.add(NamedElementsEditor.this);
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
							elementNavs.swap(fromIndex+i, fromIndex+i+1);
							Collections.swap(getElements(), fromIndex+i, fromIndex+i+1);
						}
					} else {
						for (int i=0; i<fromIndex-toIndex; i++) {
							elementNavs.swap(fromIndex-i, fromIndex-i-1);
							Collections.swap(getElements(), fromIndex-i, fromIndex-i-1);
						}
					}
					for (int i=0; i<elementNavs.size(); i++)
						elementNavs.get(i).setDefaultModelObject(i);
				}
				
			}.sortable(".side>.navs"));
			
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
