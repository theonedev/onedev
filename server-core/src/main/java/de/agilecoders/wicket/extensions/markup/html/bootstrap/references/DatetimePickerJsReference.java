package de.agilecoders.wicket.extensions.markup.html.bootstrap.references;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import de.agilecoders.wicket.core.util.Dependencies;

/**
 * Eonasdan datetime-picker js reference
 *
 * @author Alexey Volkov
 * @since 01.02.15
 */
public class DatetimePickerJsReference extends JavaScriptResourceReference {

    private static final long serialVersionUID = 1L;

    /**
     * Singleton instance of this reference
     */
    private static final class Holder {

        private static final DatetimePickerJsReference INSTANCE = new DatetimePickerJsReference();
    }

    /**
     * @return the single instance of the resource reference
     */
    public static DatetimePickerJsReference instance() {
        return Holder.INSTANCE;
    }

    /**
     * Private constructor.
     */
    private DatetimePickerJsReference() {
        super(DatetimePickerJsReference.class, "bootstrap-datetimepicker.min.js");
    }

    @Override
    public List<HeaderItem> getDependencies() {
        return Dependencies.combine(super.getDependencies(),
            JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()),
            MomentWithLocalesJsReference.asHeaderItem());
    }

    /**
     * @return this resource reference singleton instance as header item
     */
    public static HeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(instance());
    }
}


