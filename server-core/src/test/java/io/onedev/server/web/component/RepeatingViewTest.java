package io.onedev.server.web.component;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

public class RepeatingViewTest {
    @Test
    public void reordersRenderedChildrenWithoutDetachingAndRetainsOrderAfterSerialization() {
        var tester = new WicketTester();
        try {
            var rows = new RepeatingView("rows");
            var first = new Label(rows.newChildId(), "first-value");
            var second = new Label(rows.newChildId(), "second-value");
            rows.add(first, second);
            first.error("Keep feedback attached");
            rows.swap(0, 1);
            assertSame(second, rows.get(0));
            assertSame(rows, first.getParent());
            assertTrue(first.hasErrorMessage());
            var restored = SerializationUtils.clone(rows);
            assertEquals(second.getId(), restored.get(0).getId());
            tester.startComponentInPage(restored, Markup.of("<div wicket:id='rows'></div>"));
            String html = tester.getLastResponseAsString();
            assertTrue(html.indexOf("second-value") < html.indexOf("first-value"));
            var third = new Label(restored.newChildId(), "third-value");
            restored.add(third);
            assertSame(third, restored.get(2));
        } finally {
            tester.destroy();
        }
    }
}
