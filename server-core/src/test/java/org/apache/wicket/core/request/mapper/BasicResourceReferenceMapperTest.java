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
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.resource.JQueryResourceReference;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.onedev.server.buildspec.param.ParamUtils;

public class BasicResourceReferenceMapperTest {

	private WicketTester tester;

	private BasicResourceReferenceMapper mapper;

	@BeforeEach
	public void setUp() {
		tester = new WicketTester();
		mapper = new BasicResourceReferenceMapper(new PageParametersEncoder(),
				() -> tester.getApplication().getResourceSettings().getCachingStrategy());
	}

	@AfterEach
	public void tearDown() {
		tester.destroy();
	}

	@Test
	public void resourceUrlsCannotDeserializeParameterBeanClassNames() {
		DeserializationProbe.deserialized = false;
		var serialized = Hex.encodeHexString(SerializationUtils.serialize(new DeserializationProbe()));
		var className = ParamUtils.PARAM_BEAN_CLASS_NAME_PREFIX + "_" + serialized;
		assertNull(mapper.mapRequest(new MockWebRequest(Url.parse("wicket/resource/" + className + "/test.js"))));
		assertFalse(DeserializationProbe.deserialized);
	}

	@Test
	public void malformedParameterBeanClassNamesAreRejectedBeforeDecoding() {
		for (var suffix : new String[] {"", "_", "_not-hex"}) {
			var className = ParamUtils.PARAM_BEAN_CLASS_NAME_PREFIX + suffix;
			assertNull(mapper.mapRequest(new MockWebRequest(Url.parse("wicket/resource/" + className + "/test.js"))));
		}
	}

	@Test
	public void ordinaryResourceUrlsStillRoundTrip() {
		var reference = JQueryResourceReference.getV3();
		var url = mapper.mapHandler(new ResourceReferenceRequestHandler(reference));
		var mapped = assertInstanceOf(ResourceReferenceRequestHandler.class,
				mapper.mapRequest(new MockWebRequest(url)));
		assertSame(reference, mapped.getResourceReference());
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
