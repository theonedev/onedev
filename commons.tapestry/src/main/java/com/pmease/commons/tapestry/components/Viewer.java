package com.pmease.commons.tapestry.components;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

@SuppressWarnings("rawtypes")
@Import(library="context:scripts/viewer.js")
public class Viewer {

	@Parameter(required=true)
	private List elements;
	
	@Property
	private Stack<Context> stack = new Stack<Context>();
	
	@SuppressWarnings("unused")
	@Parameter(required=true)
	private String value;
	
	public Object getCurrent() {
		return stack.peek().getCurrent();
	}
	
	void setupRender() {
		if (stack.isEmpty())
			stack.push(new Context(elements, 0));
		else
			stack.push(new Context((List) stack.peek().getCurrent(), 0));
	}
	
	void cleanupRender() {
		stack.pop();
	}
	
	Object beginRender() {
		if (stack.peek().getCurrent() instanceof List)
			return this;
		else 
			return null;
	}
	
	boolean beforeRenderTemplate() {
		Context context = stack.peek();
		Object current = context.getCurrent();
		if (current instanceof String) {
			value = (String) current;
			return true;
		} else {
			return false;
		}
	}
	
	boolean afterRender() {
		stack.peek().setPosition(stack.peek().getPosition() + 1);
		return stack.peek().getCurrent() == null; 
	}
	
	public static class Context implements Serializable {
		private static final long serialVersionUID = 1L;

		private List elements;
		
		private int position;
		
		public Context(List elements, int position) {
			this.elements = elements;
			this.position = position;
		}

		public List getElements() {
			return elements;
		}

		public void setElements(List elements) {
			this.elements = elements;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}
		
		public Object getCurrent() {
			if (position < elements.size())
				return elements.get(position);
			else
				return null;
		}
	}
	
}
