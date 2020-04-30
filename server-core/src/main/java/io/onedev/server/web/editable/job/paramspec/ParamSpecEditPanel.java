package io.onedev.server.web.editable.job.paramspec;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
abstract class ParamSpecEditPanel extends Panel implements InputContext {

	private final List<ParamSpec> paramSpecs;
	
	private final int paramSpecIndex;
	
	public ParamSpecEditPanel(String id, List<ParamSpec> paramSpecs, int paramSpecIndex) {
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

				ParamSpec param = bean.getParamSpec();
				if (paramSpecIndex != -1) { 
					ParamSpec oldParam = paramSpecs.get(paramSpecIndex);
					if (!param.getName().equals(oldParam.getName()) && getInputSpec(param.getName()) != null) {
						editor.error(new Path(new PathNode.Named("paramSpec"), new PathNode.Named("name")),
								"This name has already been used by another parameter");
					}					
				} else if (getInputSpec(param.getName()) != null) {
					editor.error(new Path(new PathNode.Named("paramSpec"), new PathNode.Named("name")),
							"This name has already been used by another parameter");
				}

				if (editor.isValid()) {
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
		List<String> paramNames = new ArrayList<>();
		int currentIndex = 0;
		for (ParamSpec param: paramSpecs) {
			if (currentIndex != paramSpecIndex)
				paramNames.add(param.getName());
			currentIndex++;
		}
		return paramNames;
	}
	
	@Override
	public ParamSpec getInputSpec(String paramName) {
		for (ParamSpec param: paramSpecs) {
			if (paramName.equals(param.getName()))
				return param;
		}
		return null;
	}
		
}
