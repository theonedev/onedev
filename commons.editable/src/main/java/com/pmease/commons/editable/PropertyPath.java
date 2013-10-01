package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class PropertyPath implements Serializable {
	
	private final List<Serializable> elements;

	public PropertyPath() {
		this.elements = new ArrayList<Serializable>();
	}
	
	public PropertyPath(List<Serializable> elements) {
		this.elements = new ArrayList<Serializable>(elements);
	}
	
	public List<Serializable> getElements() {
		return Collections.unmodifiableList(elements);
	}
	
	public PropertyPath prepend(Serializable element) {
		PropertyPath newPath = new PropertyPath(elements);
		newPath.elements.add(0, element);
		return newPath;
	}
	
	public PropertyPath append(Serializable element) {
		PropertyPath newPath = new PropertyPath(elements);
		newPath.elements.add(element);
		return newPath;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Serializable element: elements) {
			if (element instanceof String) {
				if (buffer.length() != 0)
					buffer.append(".");
				buffer.append(element);
			} else {
				buffer.append("[").append(element.toString()).append("]");
			}
		}
		return buffer.toString();
	}
}
