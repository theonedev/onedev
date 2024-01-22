package io.onedev.server.model.support.widget;

import io.onedev.server.annotation.Editable;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class Widget implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<WidgetTabWrapper> tabWrappers = new ArrayList<>();
	
	private int left;
	
	private int top;
	
	private int right;
	
	private int bottom;
	
	private boolean autoHeight = true;

	@Editable(order=100, name="Tabs")
	@Size(min=1, message = "At least one tab should be added")
	public List<WidgetTabWrapper> getTabWrappers() {
		return tabWrappers;
	}

	public void setTabWrappers(List<WidgetTabWrapper> tabWrappers) {
		this.tabWrappers = tabWrappers;
	}
	
	@Editable(order=100000, description = "Adjust widget height automatically to show its content " +
			"in dashboard view mode. It also extend to fill the gap between current widget and " +
			"the widget beneath it, or extend to page bottom if no widgets beneath it")
	public boolean isAutoHeight() {
		return autoHeight;
	}

	public void setAutoHeight(boolean autoHeight) {
		this.autoHeight = autoHeight;
	}
	
	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getRight() {
		return right;
	}

	public void setRight(int right) {
		this.right = right;
	}

	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
	
	public int getDefaultWidth() {
		return 16;
	}
	
	public int getDefaultHeight() {
		return 8;
	}
	
	public boolean isIntersectedWith(Widget widget) {
		return !(widget.left >= right || widget.right <= left || widget.top >= bottom || widget.bottom <= top);		
	}
	
}
