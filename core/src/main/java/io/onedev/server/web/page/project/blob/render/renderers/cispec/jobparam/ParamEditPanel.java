package io.onedev.server.web.page.project.blob.render.renderers.cispec.jobparam;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.SourceVersion;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathElement;

@SuppressWarnings("serial")
abstract class ParamEditPanel extends Panel implements InputContext {

	private final List<InputSpec> params;
	
	private final int paramIndex;
	
	public ParamEditPanel(String id, List<InputSpec> params, int paramIndex) {
		super(id);
	
		this.params = params;
		this.paramIndex = paramIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ParamBean bean = new ParamBean();
		if (paramIndex != -1)
			bean.setParam(params.get(paramIndex));

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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(ParamEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.editBean("editor", bean);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				InputSpec param = bean.getParam();
				if (paramIndex != -1) { 
					InputSpec oldParam = params.get(paramIndex);
					if (!param.getName().equals(oldParam.getName()) && getInputSpec(param.getName()) != null) {
						editor.getErrorContext(new PathElement.Named("param"))
								.getErrorContext(new PathElement.Named("name"))
								.addError("This name has already been used by another parameter");
					}
				} else if (getInputSpec(param.getName()) != null) {
					editor.getErrorContext(new PathElement.Named("param"))
							.getErrorContext(new PathElement.Named("name"))
							.addError("This name has already been used by another parameter");
				}

				if (!editor.hasErrors(true)) {
					if (paramIndex != -1) {
						params.set(paramIndex, param);
					} else {
						params.add(param);
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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(ParamEditPanel.this));
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
		for (InputSpec param: params) {
			if (currentIndex != paramIndex)
				inputNames.add(param.getName());
			currentIndex++;
		}
		return inputNames;
	}
	
	@Override
	public InputSpec getInputSpec(String paramName) {
		for (InputSpec param: params) {
			if (paramName.equals(param.getName()))
				return param;
		}
		return null;
	}
	
	@Override
	public String validateName(String inputName) {
		if (!SourceVersion.isIdentifier(inputName)) 
			return JobParam.INVALID_CHARS_MESSAGE;
		else if (BuildConstants.ALL_FIELDS.contains(inputName))
			return "'" + inputName + "' is reserved";
		else
			return null;
	}
	
}
