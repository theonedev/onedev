package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Path;

@SuppressWarnings("serial")
public class ValuePath implements Serializable {

	private List<PathElement> elements = new ArrayList<>();
	
	public ValuePath(PathElement...elements) {
		this.elements = Arrays.asList(elements);
	}
	
	public ValuePath(List<PathElement> elements) {
		this.elements = elements;
	}
	
	public ValuePath(ValuePath valuePath) {
		this.elements = new ArrayList<>(valuePath.getElements());
	}
	
	public ValuePath(Path validationPath) {
		for (Path.Node node: validationPath) {
			if (node.getIndex() != null) 
				elements.add(new PathElement.Indexed(node.getIndex()));
			if (node.getName() != null)
				elements.add(new PathElement.Named(node.getName()));
		}
	}
	
	public List<PathElement> getElements() {
		return elements;
	}

	@Override
	public String toString() {
		return elements.toString();
	}
	
}
