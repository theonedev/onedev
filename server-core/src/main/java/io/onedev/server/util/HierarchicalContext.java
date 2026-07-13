package io.onedev.server.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.wicket.Page;
import org.jspecify.annotations.Nullable;

import io.onedev.server.web.util.WicketUtils;

public class HierarchicalContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private static ThreadLocal<Stack<HierarchicalContext>> stack =  new ThreadLocal<Stack<HierarchicalContext>>() {

		@Override
		protected Stack<HierarchicalContext> initialValue() {
			return new Stack<HierarchicalContext>();
		}
	
	};
	
	private Hierarchical hierarchical;
	
	public HierarchicalContext(Hierarchical hierarchical) {
		this.hierarchical = hierarchical;
	}
	
	public static void push(HierarchicalContext context) {
		stack.get().push(context);
	}

	public static void pop() {
		stack.get().pop();
	}

	@Nullable
	public static HierarchicalContext get() {
		if (!stack.get().isEmpty()) { 			
			return stack.get().peek();
		} else {
			Page page = WicketUtils.getPage();
			return page!=null? new HierarchicalContext(new ComponentHierarchical(page)): null;
		}
	}
	
	public Hierarchical getHierarchical() {
		return hierarchical;
	}
	
	public HierarchicalContext getChildContext(String childName) {
		return null;
	}

	@Nullable
	public <T> T findData(Class<T> clazz) {
		var parents = findAllData(clazz);
		return parents.isEmpty()? null: parents.get(0);
	}

	public <T> List<T> findAllData(Class<T> clazz) {
		List<T> allData = new ArrayList<>();
		Hierarchical hierarchical = this.hierarchical;
		while (hierarchical != null) {
			var data = hierarchical.getData(clazz);
			if (data != null) 
				allData.add(data);
			hierarchical = hierarchical.getParent();
		}
		return allData;
	}

}
