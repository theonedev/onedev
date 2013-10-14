package com.pmease.gitop.web.common.component.vex;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.common.component.vex.VexLinkBehavior.VexIcon;

@SuppressWarnings("serial")
public class AjaxConfirmButton extends AjaxButton {

	public AjaxConfirmButton(String id, Form<?> form, IModel<String> textModel) {
		this(id, form, textModel, null, null, null, null);
	}
	
	public AjaxConfirmButton(String id, Form<?> form, IModel<String> textModel,
			final IModel<VexIcon> iconModel, IModel<String> yesLabelModel,
			IModel<String> noLabelModel, IModel<String> confirmCssClassModel) {
		super(id, form);
		add(new VexLinkBehavior(textModel, iconModel, yesLabelModel,
				noLabelModel, confirmCssClassModel));
	}

	@Override
	protected AjaxFormSubmitBehavior newAjaxFormSubmitBehavior(String event) {
		return new AjaxFormSubmitBehavior(getForm(), "vex.confirm") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled(Component component) {
				return AjaxConfirmButton.this.isEnabledInHierarchy();
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				AjaxConfirmButton.this.onSubmit(target, AjaxConfirmButton.this.getForm());
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				AjaxConfirmButton.this.onError(target, AjaxConfirmButton.this.getForm());
			}
		};
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}
}
