package com.pmease.commons.editable;

import java.lang.reflect.Method;

import com.google.inject.ImplementedBy;
import com.pmease.commons.editable.typeconverter.TypeConverter;

@ImplementedBy(DefaultTypeConverterRegistry.class)
public interface TypeConverterRegistry {

	/**
	 * Get type converter of the property given the property getter.
	 * <p>
	 * @param getter
	 * 			getter of the property
	 * @return
	 * 			type converter instance for the property, or <i>null</i> if no 
	 * 			type converter matches the property getter
	 */
	TypeConverter getTypeConverter(Method getter);

}
