package com.pmease.gitop.web.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wicket.util.io.IClusterable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class JsOptions implements IClusterable {
	private static final long serialVersionUID = 1L;

	private Map<String, Serializable> options = Maps.newLinkedHashMap();
	
	public boolean containsKey(Object key) {
		Preconditions.checkNotNull(key, "key");
		return options.containsKey(key);
	}
	
	public Serializable get(Object key) {
		Preconditions.checkNotNull(key, "key");
		return options.get(key.toString());
	}
	
	public JsOptions set(Object key, Serializable value) {
		Preconditions.checkNotNull(key, "key");
		if (value != null) {
			options.put(key.toString(), value);
		} else {
			options.remove(key);
		}
		
		return this;
	}
	
	public Set<Entry<String, Serializable>> entries() {
		return Collections.unmodifiableSet(options.entrySet());
	}
	
	@Override
	public String toString() {
		return JsUtil.formatOptions(this.options);
	}
}
