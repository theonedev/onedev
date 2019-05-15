package io.onedev.server.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.SerializationUtils;

import io.onedev.server.util.inputspec.InputSpec;

public class JobUtils {
	
	private static final String PARAM_BEAN_PREFIX = "JobParamBean";
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> defineParamBeanClass(Collection<InputSpec> paramSpecs) {
		byte[] bytes = SerializationUtils.serialize((Serializable) paramSpecs);
		String className = PARAM_BEAN_PREFIX + "_" + Hex.encodeHexString(bytes);
		
		return (Class<? extends Serializable>) InputSpec.defineClass(className, paramSpecs);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static Class<? extends Serializable> loadParamBeanClass(String className) {
		if (className.startsWith(PARAM_BEAN_PREFIX)) {
			byte[] bytes;
			try {
				bytes = Hex.decodeHex(className.substring(PARAM_BEAN_PREFIX.length()+1).toCharArray());
			} catch (DecoderException e) {
				throw new RuntimeException(e);
			}
			List<InputSpec> paramSpecs = (List<InputSpec>) SerializationUtils.deserialize(bytes);
			return defineParamBeanClass(paramSpecs);
		} else {
			return null;
		}
	}
	
}
