package com.pmease.commons.wicket.behavior.sortable;

import com.google.common.base.Objects;

public class SortPosition {
	
	private final int listIndex;
	
	private final int itemIndex;

	public SortPosition(int listIndex, int itemIndex) {
		this.listIndex = listIndex;
		this.itemIndex = itemIndex;
	}
	
	public int getListIndex() {
		return listIndex;
	}

	public int getItemIndex() {
		return itemIndex;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("listIndex", listIndex)
				.add("itemIndex", itemIndex)
				.toString();
	}

}