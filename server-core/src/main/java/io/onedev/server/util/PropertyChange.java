package io.onedev.server.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;

public class PropertyChange implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String oldValue;
	
	private String newValue;
	
	public PropertyChange(String name, String oldValue, String newValue) {
		this.name = name;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	
	public static List<PropertyChange> listOf(Map<String, String> oldProperties, Map<String, String> newProperties) {
		List<PropertyChange> changes = new ArrayList<>();
		MapDifference<String, String> diff = Maps.difference(oldProperties, newProperties);
		for (Map.Entry<String, ValueDifference<String>> entry: diff.entriesDiffering().entrySet()) { 
			changes.add(new PropertyChange(entry.getKey(), 
					entry.getValue().leftValue(), entry.getValue().rightValue()));
		}
		for (Map.Entry<String, String> entry: diff.entriesOnlyOnLeft().entrySet())  
			changes.add(new PropertyChange(entry.getKey(), entry.getValue(), null));
		for (Map.Entry<String, String> entry: diff.entriesOnlyOnRight().entrySet())  
			changes.add(new PropertyChange(entry.getKey(), null, entry.getValue()));
		return changes;
	}

}
