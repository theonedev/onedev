package com.pmease.commons.wicket.behavior.dropdown;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;

public class DropdownAlignment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final AlignmentTarget target;
	
	private final int x, y; 
	
	private final int offset;
	
	private final boolean showIndicator;

	public DropdownAlignment(@Nullable AlignmentTarget target, int x, int y, int offset, boolean showIndicator) {
		this.target = target;
		this.x = x;
		this.y = y;
		this.offset = offset;
		this.showIndicator = showIndicator;
	}

	public AlignmentTarget getTarget() {
		return target;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getOffset() {
		return offset;
	}
	
	public boolean isShowIndicator() {
		return showIndicator;
	}
	
	public String toJSON(Component trigger) {
		ObjectMapper objectMapper = AppLoader.getInstance(ObjectMapper.class);
		
		Map<String, Object> alignmentSettings = new HashMap<>();
		
		alignmentSettings.put("offset", getOffset());
		alignmentSettings.put("showIndicator", showIndicator);
		if (getTarget() != null) {
			Map<String, String> target = new HashMap<>();
			target.put("x", String.valueOf(getTarget().getX()));
			target.put("y", String.valueOf(getTarget().getY()));
			if (getTarget().getComponent() != null)
				target.put("id", getTarget().getComponent().getMarkupId());
			else
				target.put("id", trigger.getMarkupId());
			alignmentSettings.put("target", target);
		}
		alignmentSettings.put("x", String.valueOf(getX()));
		alignmentSettings.put("y", String.valueOf(getY()));
		
		try {
			return objectMapper.writeValueAsString(alignmentSettings);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

	}
}
