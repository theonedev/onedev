package io.onedev.server.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.SerializationUtils;

import io.onedev.server.ci.job.Job;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;

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
	
	public static Map<String, List<String>> getParamValues(OneContext context, Job job, Serializable paramBean) {
		OneContext.push(context);
		try {
			Map<String, List<String>> paramValues = new HashMap<>();
			BeanDescriptor beanDescriptor = new BeanDescriptor(paramBean.getClass());
			Map<String, InputSpec> paramSpecs = new HashMap<>();
			for (InputSpec paramSpec: job.getParamSpecs())
				paramSpecs.put(paramSpec.getName(), paramSpec);
			
			for (List<PropertyDescriptor> groupProperties: beanDescriptor.getPropertyDescriptors().values()) {
				for (PropertyDescriptor property: groupProperties) {
					Object paramValue = property.getPropertyValue(paramBean);
					InputSpec paramSpec = paramSpecs.get(property.getDisplayName());
					if (paramSpec != null && paramValue != null) {
						List<String> strings = paramSpec.convertToStrings(paramValue);
						if (!strings.isEmpty())
							paramValues.put(property.getDisplayName(), strings);
					}
				}
			}
			
			return paramValues;
		} finally {
			OneContext.pop();
		}
	}
	
}
