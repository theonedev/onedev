package com.pmease.gitop.web.common.component.vex;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.common.component.vex.VexLinkBehavior.VexIcon;

@SuppressWarnings("serial")
public abstract class AjaxConfirmLink<T> extends AjaxLink<T> {

//	private static final String ATTR_MESSAGE = "data-confirm-message";
//	private static final String ATTR_YES_LABEL = "data-confirm-yes-label";
//	private static final String ATTR_NO_LABEL = "data-confirm-no-label";
//	private static final String ATTR_CSS_CLASS_NAME = "data-confirm-css-class";
//	private static final String ATTR_CONFIRM_ICON = "data-confirm-icon";
//	
//	private final IModel<String> textModel;
//	private final IModel<String> iconModel;
//	private final IModel<String> yesLabelModel;
//	private final IModel<String> noLabelModel;
//	private final IModel<String> confirmCssClassModel;
	
	public AjaxConfirmLink(String id, IModel<String> textModel) {
		this(id, null, textModel, null, null, null, null);
	}
	
	public AjaxConfirmLink(
			String id, 
			IModel<T> model, 
			IModel<String> textModel, 
			IModel<VexIcon> iconModel,
			IModel<String> yesLabelModel, 
			IModel<String> noLabelModel, 
			IModel<String> confirmCssClassModel) {
		super(id, model);
		setOutputMarkupId(true);
		
		add(new VexLinkBehavior(textModel, iconModel, yesLabelModel, noLabelModel, confirmCssClassModel));
		
		add(AttributeAppender.append("class", "confirm-link"));
	}
	
	@Override
	protected AjaxEventBehavior newAjaxEventBehavior(String event) {
		return new AjaxEventBehavior("vex.confirm") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled(Component component) {
				return AjaxConfirmLink.this.isLinkEnabled();
			}

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				onClick(target);
			}
		};
	}
}
