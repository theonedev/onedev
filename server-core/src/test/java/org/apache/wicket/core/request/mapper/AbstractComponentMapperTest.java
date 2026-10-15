package org.apache.wicket.core.request.mapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.onedev.server.buildspec.param.ParamUtils;

public class AbstractComponentMapperTest {

	private WicketTester tester;

	@BeforeEach
	public void setUp() {
		tester = new WicketTester();
	}

	@AfterEach
	public void tearDown() {
		tester.destroy();
	}

	@Test
	public void bookmarkableUrlsCannotDeserializeParameterBeanClassNames() {
		var serialized = Hex.encodeHexString(SerializationUtils.serialize(new DeserializationProbe()));
		var className = ParamUtils.PARAM_BEAN_CLASS_NAME_PREFIX + "_" + serialized;
		var mapper = tester.getApplication().getRootRequestMapper();
		for (var prefix : new String[] {"wicket/bookmarkable/", "bookmarkable/"}) {
			for (var suffix : new String[] {"", ";ignored", "?0"}) {
				DeserializationProbe.deserialized = false;
				assertNull(mapper.mapRequest(new MockWebRequest(Url.parse(prefix + className + suffix))));
				assertFalse(DeserializationProbe.deserialized);
			}
		}
	}

	@Test
	public void malformedParameterBeanClassNamesAreRejectedBeforeDecoding() {
		var mapper = tester.getApplication().getRootRequestMapper();
		for (var suffix : new String[] {"", "_", "_not-hex", ";ignored"}) {
			var className = ParamUtils.PARAM_BEAN_CLASS_NAME_PREFIX + suffix;
			assertNull(mapper.mapRequest(new MockWebRequest(Url.parse("wicket/bookmarkable/" + className))));
		}
	}

	@Test
	public void ordinaryBookmarkableUrlsStillResolvePages() {
		var mapper = new BookmarkableMapper();
		var url = mapper.mapHandler(new BookmarkablePageRequestHandler(new PageProvider(WebPage.class)));
		for (var suffix : new String[] {"", ";ignored"}) {
			var mapped = assertInstanceOf(RenderPageRequestHandler.class,
					mapper.mapRequest(new MockWebRequest(Url.parse(url.toString() + suffix))));
			assertSame(WebPage.class, mapped.getPageClass());
		}
	}

	private static class DeserializationProbe implements Serializable {

		private static final long serialVersionUID = 1L;

		private static boolean deserialized;

		private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
			input.defaultReadObject();
			deserialized = true;
		}
	}
}
