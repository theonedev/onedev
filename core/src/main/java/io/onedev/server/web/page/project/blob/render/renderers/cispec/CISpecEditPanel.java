package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.editable.PathElement.Indexed;
import io.onedev.server.web.editable.PathElement.Named;
import io.onedev.server.web.editable.ValuePath;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.util.WicketUtils;

@SuppressWarnings("serial")
public class CISpecEditPanel extends FormComponentPanel<byte[]> {

	private Serializable parseResult;
	
	private RepeatingView jobNavs;
	
	private RepeatingView jobContents;
	
	public CISpecEditPanel(String id, BlobRenderContext context, byte[] initialContent) {
		super(id, Model.of(initialContent));
		parseResult = parseCISpec(getModelObject());
	}
	
	private Serializable parseCISpec(byte[] bytes) {
		try {
			CISpec ciSpec = CISpec.parse(bytes);
			if (ciSpec == null)
				ciSpec = new CISpec();
			return ciSpec;
		} catch (Exception e) {
			return e;
		}
	}

	private Component newJobNav(Job job) {
		WebMarkupContainer nav = new WebMarkupContainer(jobNavs.newChildId());
		
		nav.add(new AjaxLink<Void>("select") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				String script = String.format("onedev.ciSpec.edit.showJob(%d);", 
						WicketUtils.getChildIndex(jobNavs, nav));
				target.appendJavaScript(script);
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
			}

		});
		
		nav.add(new AjaxLink<Void>("delete") {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(AjaxRequestTarget target) {
				int index = WicketUtils.getChildIndex(jobNavs, nav);
				jobNavs.remove(nav);
				jobContents.remove(jobContents.get(index));
				target.appendJavaScript(String.format("onedev.ciSpec.edit.deleteJob(%d);", index));
			}
			
		});
		jobNavs.add(nav.setOutputMarkupId(true));
		
		return nav;
	}
	
	private Component newJobContent(Job job) {
		BeanEditor content = BeanContext.editBean(jobContents.newChildId(), job);
		content.add(new Behavior() {

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				int index = WicketUtils.getChildIndex(jobContents, content);
				String script = String.format("onedev.ciSpec.edit.trackJobNameChange(%d);", index);
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		jobContents.add(content.setOutputMarkupId(true));
		return content;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Fragment validFrag;
		if (parseResult instanceof CISpec) {
			CISpec ciSpec = (CISpec) parseResult;

			validFrag = new Fragment("content", "validFrag", this) {

				@Override
				public void renderHead(IHeaderResponse response) {
					super.renderHead(response);

					int index = 0;
					for (Component child: jobContents) {
						if (((BeanEditor) child).hasErrors(true)) {
							index = WicketUtils.getChildIndex(jobContents, child);
							break;
						}
					}
					
					String script = String.format("onedev.ciSpec.edit.showJob(%d);", index);
					response.render(OnDomReadyHeaderItem.forScript(script));
				}
				
			};
			
			jobNavs = new RepeatingView("navs");
			jobContents = new RepeatingView("contents");
			for (Job job: ciSpec.getJobs()) {
				newJobNav(job);
				newJobContent(job);
			}
			validFrag.add(jobNavs);
			validFrag.add(jobContents);
			
			validFrag.add(new AjaxLink<Void>("add") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					Job job = new Job();

					Component nav = newJobNav(job);
					String script = String.format("$(\"#%s\").prev().append(\"<div id='%s'></div>\");", 
							getMarkupId(), nav.getMarkupId());
					target.prependJavaScript(script);
					target.add(nav);

					Component content = newJobContent(job);
					script = String.format("$(\"#%s\").parent().next().append(\"<div id='%s'></div>\");", 
							getMarkupId(), content.getMarkupId());
					target.prependJavaScript(script);
					target.add(content);
					
					script = String.format("onedev.ciSpec.edit.showJob(%d);", jobNavs.size() - 1);
					target.appendJavaScript(script);
				}
				
			});
			
			validFrag.add(new SortBehavior() {

				@SuppressWarnings("deprecation")
				@Override
				protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
					int fromIndex = from.getItemIndex();
					int toIndex = to.getItemIndex();
					if (fromIndex < toIndex) {
						for (int i=0; i<toIndex-fromIndex; i++) { 
							jobNavs.swap(fromIndex+i, fromIndex+i+1);
							jobContents.swap(fromIndex+i, fromIndex+i+1);
						}
					} else {
						for (int i=0; i<fromIndex-toIndex; i++) {
							jobNavs.swap(fromIndex-i, fromIndex-i-1);
							jobContents.swap(fromIndex-i, fromIndex-i-1);
						}
					}
					target.appendJavaScript(String.format("onedev.ciSpec.edit.swapJobs(%d, %d)", fromIndex, toIndex));
				}
				
			}.sortable(".jobs>.body>.side>.navs"));
			
			add(new IValidator<byte[]>() {
				
				@Override
				public void validate(IValidatable<byte[]> validatable) {
					Serializable parseResult = parseCISpec(validatable.getValue());
					if (parseResult instanceof CISpec) {
						CISpec ciSpec = (CISpec) parseResult;
						Validator validator = AppLoader.getInstance(Validator.class);
						for (ConstraintViolation<CISpec> violation: validator.validate(ciSpec)) {
							ValuePath path = new ValuePath(violation.getPropertyPath());
							if (path.getElements().isEmpty()) {
								error(violation.getMessage());
							} else {
								PathElement.Named named = (Named) path.getElements().iterator().next();
								switch (named.getName()) {
								case "jobs":
									path = new ValuePath(path.getElements().subList(1, path.getElements().size()));
									if (path.getElements().isEmpty()) {
										error("Jobs: " + violation.getMessage());
									} else {
										PathElement.Indexed indexed = (Indexed) path.getElements().iterator().next();
										path = new ValuePath(path.getElements().subList(1, path.getElements().size()));
										if (path.getElements().isEmpty()) {
											error("Job '" + ciSpec.getJobs().get(indexed.getIndex()).getName() + "': " + violation.getMessage());
										} else {
											@SuppressWarnings("deprecation")
											BeanEditor editor = (BeanEditor) jobContents.get(indexed.getIndex());
											ErrorContext errorContext = editor.getErrorContext(path);
											if (errorContext != null)
												errorContext.addError(violation.getMessage());
										}
									}
									break;
								default:
									throw new RuntimeException("Unexpected element name: " + named.getName());
								}
							}
						}					
					}
				}
				
			});
		} else {
			validFrag = new Fragment("content", "invalidFrag", this);
			validFrag.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString((Throwable) parseResult)));
		}
		add(validFrag);
	}

	@Override
	public void convertInput() {
		CISpec editingCISpec = getEditingCISpec();
		if (editingCISpec != null) {
			setConvertedInput(VersionedDocument.fromBean(editingCISpec).toXML().getBytes(Charsets.UTF_8));
		} else {
			setConvertedInput(getModelObject());
		}
	}

	@Nullable
	public CISpec getEditingCISpec() {
		if (parseResult instanceof CISpec) {
			CISpec ciSpec = new CISpec();
			for (Component child: jobContents) {
				BeanEditor jobContent = (BeanEditor) child;
				ciSpec.getJobs().add((Job) jobContent.getConvertedInput());
			}
			return ciSpec;
		} else {
			return null;
		}
	}
	
}
