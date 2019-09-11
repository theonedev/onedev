package io.onedev.server.web.editable.job.paramspec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.validation.ValidationException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathElement;

@SuppressWarnings("serial")
abstract class ParamSpecEditPanel extends Panel implements InputContext {

	private final List<InputSpec> paramSpecs;
	
	private final int paramSpecIndex;
	
	public ParamSpecEditPanel(String id, List<InputSpec> paramSpecs, int paramSpecIndex) {
		super(id);
	
		this.paramSpecs = paramSpecs;
		this.paramSpecIndex = paramSpecIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ParamSpecBean bean = new ParamSpecBean();
		if (paramSpecIndex != -1)
			bean.setParamSpec(paramSpecs.get(paramSpecIndex));

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(ParamSpecEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.edit("editor", bean);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				InputSpec param = bean.getParamSpec();
				if (paramSpecIndex != -1) { 
					InputSpec oldParam = paramSpecs.get(paramSpecIndex);
					if (!param.getName().equals(oldParam.getName()) && getInputSpec(param.getName()) != null) {
						editor.getErrorContext(new PathElement.Named("paramSpec"))
								.getErrorContext(new PathElement.Named("name"))
								.addError("This name has already been used by another parameter");
					}
				} else if (getInputSpec(param.getName()) != null) {
					editor.getErrorContext(new PathElement.Named("paramSpec"))
							.getErrorContext(new PathElement.Named("name"))
							.addError("This name has already been used by another parameter");
				}

				if (!editor.hasErrors(true)) {
					if (paramSpecIndex != -1) {
						paramSpecs.set(paramSpecIndex, param);
					} else {
						paramSpecs.add(param);
					}
					onSave(target);
				} else {
					target.add(form);
				}
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(ParamSpecEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	public List<String> getInputNames() {
		List<String> inputNames = new ArrayList<>();
		int currentIndex = 0;
		for (InputSpec param: paramSpecs) {
			if (currentIndex != paramSpecIndex)
				inputNames.add(param.getName());
			currentIndex++;
		}
		return inputNames;
	}
	
	@Override
	public InputSpec getInputSpec(String paramName) {
		for (InputSpec param: paramSpecs) {
			if (paramName.equals(param.getName()))
				return param;
		}
		return null;
	}
	
	@Override
	public void validateName(String inputName) {
		if (!SourceVersion.isIdentifier(inputName) || inputName.contains("$")) { 
			throw new ValidationException("param name should start with letter and can only consist of "
					+ "alphanumeric and underscore characters");
		} else if (BuildConstants.ALL_FIELDS.contains(inputName)) {
			throw new ValidationException("'" + inputName + "' is reserved");
		}
	}
	
}
