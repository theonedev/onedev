package com.pmease.gitop.web.common.wicket.form;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.assets.AssetLocator;

public class BaseForm<T> extends Form<T> {
	private static final long serialVersionUID = 1L;

	public BaseForm(String id) {
		super(id);
	}
	
	public BaseForm(String id, IModel<T> model) {
		super(id, model);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptReferenceHeaderItem.forReference(AssetLocator.ARE_YOU_SURE_JS));
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("gitop.form.init('#%s')", getMarkupId(true))));
	}
}
