package io.onedev.server.web.component.datepicker;

import java.util.Date;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.Constants;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BasePage;

public class DatePicker extends DateTextField {

	private static final long serialVersionUID = 1L;

	private final boolean withTime;
	
    public DatePicker(String id, boolean withTime) {
        this(id, null, withTime);
    }

    public DatePicker(String id, IModel<Date> model, boolean withTime) {
    	super(id, model, withTime?Constants.DATETIME_FORMAT: Constants.DATE_FORMAT);
    	this.withTime = withTime;
    }

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new DatePickerResourceReference()));
		
		BasePage page = (BasePage) getPage();
		if (page.isDarkMode()) {
			response.render(CssHeaderItem.forReference(
					new BaseDependentCssResourceReference(DatePickerResourceReference.class, "dark.css")));
		}
		
		String script = String.format("onedev.server.datePicker.onDomReady('%s', %b)", 
				getMarkupId(), withTime);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
