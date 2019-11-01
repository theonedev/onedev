package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class Path implements Serializable {

	private List<PathNode> nodes = new ArrayList<>();
	
	public Path(PathNode...nodes) {
		for (PathNode node: nodes)
			this.nodes.add(node);
	}
	
	public Path(List<PathNode> nodes) {
		this.nodes = nodes;
	}
	
	public Path(Path path) {
		this.nodes = new ArrayList<>(path.getNodes());
	}
	
	public Path(javax.validation.Path path) {
		for (javax.validation.Path.Node node: path) {
			if (node.getIndex() != null) 
				nodes.add(new PathNode.Indexed(node.getIndex()));
			if (node.getName() != null)
				nodes.add(new PathNode.Named(node.getName()));
		}
	}
	
	public List<PathNode> getNodes() {
		return nodes;
	}
	
	@Nullable
	public PathNode takeNode() {
		if (!nodes.isEmpty()) 
			return nodes.remove(0);
		else
			return null;
	}

	@Override
	public String toString() {
		return nodes.stream().map(it->it.toString()).collect(Collectors.joining(" -> "));
	}
	
	public static String describe(PathNode propertyNode, Path pathInProperty) {
		List<PathNode> nodes = Lists.newArrayList(propertyNode);
		nodes.addAll(pathInProperty.getNodes());
		return new Path(nodes).toString();
	}
	
}
