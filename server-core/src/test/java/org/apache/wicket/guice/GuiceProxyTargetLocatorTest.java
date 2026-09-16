package org.apache.wicket.guice;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import io.onedev.commons.loader.AppLoader;

public class GuiceProxyTargetLocatorTest {
    private static class Fields {
        List<String> values;
        Runnable missing;
    }

    @Test
    public void resolvesNamedGenericTargetsAfterSerializationWithoutAWicketApplication() throws Exception {
        var previous = AppLoader.injector;
        var values = List.of("one", "two");
        var name = Names.named("values");
        AppLoader.injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Key.get(new TypeLiteral<List<String>>() {}, name)).toInstance(values);
            }
        });
        try {
            var locator = new GuiceProxyTargetLocator(Fields.class.getDeclaredField("values"), name, false);
            var fields = new Fields();
            fields.values = SerializationUtils.clone(locator).locateProxyTarget();
            assertSame(values, fields.values);
            assertTrue(locator.isSingletonScope());
            var optional = new GuiceProxyTargetLocator(Fields.class.getDeclaredField("missing"), null, true);
            fields.missing = optional.locateProxyTarget();
            assertNull(fields.missing);
        } finally {
            AppLoader.injector = previous;
        }
    }
}
