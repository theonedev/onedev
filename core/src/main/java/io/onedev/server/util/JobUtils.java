package io.onedev.server.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.SerializationUtils;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.SecretInput;

public class JobUtils {
	
	private static final String PARAM_BEAN_PREFIX = "JobParamBean";
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> defineParamBeanClass(Collection<InputSpec> paramSpecs) {
		byte[] bytes = SerializationUtils.serialize((Serializable) paramSpecs);
		String className = PARAM_BEAN_PREFIX + "_" + Hex.encodeHexString(bytes);
		
		List<InputSpec> paramSpecsCopy = new ArrayList<>(paramSpecs);
		for (int i=0; i<paramSpecsCopy.size(); i++) {
			InputSpec paramSpec = paramSpecsCopy.get(i);
			if (paramSpec instanceof SecretInput) {
				InputSpec paramSpecClone = (InputSpec) SerializationUtils.clone(paramSpec);
				String description = paramSpecClone.getDescription();
				if (description == null)
					description = "";
				description += String.format("<div class='alert alert-warning' style='margin-bottom: 0; margin-top: 8px; padding-top:10px; padding-bottom: 10px; font-size: 13px;'>Secret less than %d characters "
						+ "will not be masked in build log</div>", SecretInput.MASK.length());
				paramSpecClone.setDescription(description);
				paramSpecsCopy.set(i, paramSpecClone);
			}
		}
		return (Class<? extends Serializable>) InputSpec.defineClass(className, paramSpecsCopy);
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
