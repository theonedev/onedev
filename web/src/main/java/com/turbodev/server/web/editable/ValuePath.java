package com.turbodev.server.web.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Path;

@SuppressWarnings("serial")
public class ValuePath implements Serializable {

	private List<PathSegment> elements = new ArrayList<>();
	
	public ValuePath(PathSegment...elements) {
		this.elements = Arrays.asList(elements);
	}
	
	public ValuePath(ValuePath valuePath) {
		this.elements = new ArrayList<>(valuePath.getElements());
	}
	
	public ValuePath(Path validationPath) {
		for (Path.Node node: validationPath) {
			if (node.getIndex() != null) 
				elements.add(new PathSegment.Element(node.getIndex()));
			if (node.getName() != null)
				elements.add(new PathSegment.Property(node.getName()));
		}
	}
	
	public List<PathSegment> getElements() {
		return elements;
	}

	@Override
	public String toString() {
		return elements.toString();
	}
	
}
