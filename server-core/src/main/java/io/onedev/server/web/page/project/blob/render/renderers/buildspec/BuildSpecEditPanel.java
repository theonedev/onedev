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
import io.onedev.server.buildspec.BuildSpecImport;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.Property;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.step.StepTemplate;
import io.onedev.server.buildspec.step.StepTemplateAware;
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
import io.onedev.server.web.editable.EditableUtils;
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
							return BuildSpecRendererProvider.getActiveJobIndex(context, buildSpec);
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
							}
							
							return new JobEditor(componentId, element);
						}

					};
					
					if (target != null) {
						replace(jobsEditor);
						target.add(jobsEditor);
					} else {
						add(jobsEditor);
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
							return BuildSpecRendererProvider.getActiveStepTemplateIndex(context, buildSpec);
						}

						@Override
						protected BeanEditor newElementEditor(String componentId, StepTemplate element) {
							
							class StepTemplateEditor extends BeanEditor implements StepTemplateAware {

								public StepTemplateEditor(String id, NamedElement element) {
									super(id, new BeanDescriptor(StepTemplate.class), Model.of(element));
								}

								@Override
								public StepTemplate getTemplate() {
									return (StepTemplate) getConvertedInput();
								}
							}
							
							return new StepTemplateEditor(componentId, element);
						}

					};
					
					if (target != null) {
						replace(stepTemplatesEditor);
						target.add(stepTemplatesEditor);
					} else {
						add(stepTemplatesEditor);
					}
				}
				
				private void setupPropertiesEditor(@Nullable AjaxRequestTarget target) {
					PropertyEditor<Serializable> propertiesEditor = PropertyContext.edit("body", buildSpec, "properties");
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
					propertiesEditor.add(AttributeAppender.append("class", "properties d-flex"));
					propertiesEditor.setOutputMarkupId(true);
					if (target != null) {
						replace(propertiesEditor);
						target.add(propertiesEditor);
					} else {
						add(propertiesEditor);
					}
				}
				
				private void setupImportsEditor(@Nullable AjaxRequestTarget target) {
					PropertyEditor<Serializable> importsEditor = PropertyContext.edit("body", buildSpec, "imports");
					importsEditor.add(new Behavior() {

						@SuppressWarnings("unchecked")
						@Override
						public void onEvent(Component component, IEvent<?> event) {
							super.onEvent(component, event);
							if (event.getPayload() instanceof FormSubmitted) 
								buildSpec.setImports((List<BuildSpecImport>) importsEditor.getConvertedInput());
						}

						@Override
						public void renderHead(Component component, IHeaderResponse response) {
							super.renderHead(component, response);
							response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildSpec.onTabDomReady('.imports');"));
						}
						
					});
					importsEditor.add(AttributeAppender.append("class", "imports d-flex"));
					importsEditor.setOutputMarkupId(true);
					if (target != null) {
						replace(importsEditor);
						target.add(importsEditor);
					} else {
						add(importsEditor);
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

					String selection = getSelection();
					
					if (selection == null || selection.startsWith("jobs") || selection.equals("new-job"))
						setupJobsEditor(null);
					else if (selection.startsWith("step-templates") || selection.equals("new-step-template"))
						setupStepTemplatesEditor(null);
					else if (selection.equals("imports"))
						setupImportsEditor(null);
					else
						setupPropertiesEditor(null);
					
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
									replaceState(target, "jobs");
									setupJobsEditor(target);
									error("Jobs: " + violation.getMessage());
								} else {
									PathNode.Indexed indexed = (Indexed) path.getNodes().iterator().next();
									path = new Path(path.getNodes().subList(1, path.getNodes().size()));
									if (path.getNodes().isEmpty()) {
										replaceState(target, "jobs");
										setupJobsEditor(target);
										error("Job '" + buildSpec.getJobs().get(indexed.getIndex()).getName() + "': " + violation.getMessage());
									} else {
										String selection = "jobs/" + buildSpec.getJobs().get(indexed.getIndex()).getName();
										replaceState(target, selection);
										setupJobsEditor(target);
										((BeanEditor)get("body:element")).error(path, violation.getMessage());
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
					else if ("new-step-template".equals(getSelection()))
						replaceState(target, "step-templates");
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
	
	private abstract class NamedElementsEditor<T extends NamedElement> extends Fragment {
		
		private final Class<T> elementClass;
		
		@SuppressWarnings("unchecked")
		public NamedElementsEditor() {
			super("body", "namedElementsFrag", BuildSpecEditPanel.this);
			
			List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(NamedElementsEditor.class, getClass());
			elementClass = (Class<T>) typeArguments.get(0);
		}

		protected abstract List<T> getElements();
		
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
					pushState(target, getTypeName(elementClass) + "s/" + getElement().getName());
					setupElementEditor(target, nav);
					resizeWindow(target);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(elementsEdit);
					resizeWindow(target);
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
							"$('.build-spec>.elements>.side>.navs>.nav:eq(%d)').remove();", 
							elementIndex));
					getElements().remove(elementIndex);
					int activeElementIndex = getElements().indexOf(elementEdit.getDefaultModelObject());
					if (activeElementIndex == -1) { 
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
						"$('.build-spec>.elements>.side>.navs').append(\"<div id='%s'></div>\");", 
						nav.getMarkupId(true));
				target.prependJavaScript(script);
				target.add(nav);
			}
		}
		
		private String getTypeName(Class<?> typeClass) {
			return EditableUtils.getDisplayName(typeClass).replace(' ', '-').toLowerCase();
		}
		
		private void setupElementEditor(@Nullable AjaxRequestTarget target, Component nav) {
			AbstractPostAjaxBehavior nameChangeBehavior = new AbstractPostAjaxBehavior() {
				
				@Override
				protected void respond(AjaxRequestTarget target) {
					IRequestParameters params = RequestCycle.get().getRequest().getRequestParameters();
					String name = params.getParameterValue("name").toOptionalString();
					
					String selection = getTypeName(elementClass) + "s";
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
						getElements().set(elementIndex, (T) elementEditor.getConvertedInput());
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
			pushState(target, "new-" +  getTypeName(elementClass));
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
					resizeWindow(target);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(NamedElementsEditor.this);
					resizeWindow(target);
				}
				
			};
			
			List<T> suggestedElements = new ArrayList<>();
			
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
											resizeWindow(target);
										}

										@Override
										protected void onError(AjaxRequestTarget target, Form<?> form) {
											super.onError(target, form);
											dropdown.close();
											target.add(NamedElementsEditor.this);
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
			
			add(AttributeAppender.append("class", "d-flex flex-nowrap elements " + getTypeName(elementClass) + "s"));
			setOutputMarkupId(true);
		}
		
		@Override
		public void renderHead(IHeaderResponse response) {
			super.renderHead(response);
			response.render(OnDomReadyHeaderItem.forScript(
					String.format("onedev.server.buildSpec.onTabDomReady('.%ss');", getTypeName(elementClass))));
		}
		
	}
	
}
