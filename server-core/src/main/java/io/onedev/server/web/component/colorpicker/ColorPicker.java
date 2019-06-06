package io.onedev.server.web.component.colorpicker;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class ColorPicker extends HiddenField<String> {

	private final boolean allowEmpty;
	
	public ColorPicker(String id, IModel<String> model, boolean allowEmpty) {
		super(id, model);
		this.allowEmpty = allowEmpty;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ColorPickerResourceReference()));
		
		String script = String.format("onedev.server.colorPicker.onDomReady('%s', %b);", 
				getMarkupId(true), allowEmpty);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
