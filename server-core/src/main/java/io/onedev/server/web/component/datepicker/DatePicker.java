package io.onedev.server.web.component.datepicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.DateConverter;

import io.onedev.server.util.DateUtils;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BasePage;

public class DatePicker extends DateTextField {

	private static final long serialVersionUID = 1L;

	private final boolean withTime;

	private final DateConverter dateConverter;
	
    public DatePicker(String id, boolean withTime) {
        this(id, null, withTime);
    }

    public DatePicker(String id, IModel<Date> model, boolean withTime) {
    	super(id, model, getDatePattern(withTime));
    	this.withTime = withTime;
		dateConverter = new DateConverter() {

			@Override
			public DateFormat getDateFormat(Locale locale) {
				if (locale == null) {
					locale = Locale.getDefault(Locale.Category.FORMAT);
				}
				var dateFormat = new SimpleDateFormat(getDatePattern(withTime), locale);
				dateFormat.setTimeZone(TimeZone.getTimeZone(DateUtils.getZoneId()));
				return dateFormat;
			}

		};
    }

	private static String getDatePattern(boolean withTime) {
		return withTime?DateUtils.DATETIME_FORMAT: DateUtils.DATE_FORMAT;
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

    @Override
    protected IConverter<?> createConverter(Class<?> type) {
		return dateConverter;
    }
		
}
