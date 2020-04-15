package io.onedev.server.web.component.issue.workflowreconcile;

import java.util.List;

public class ReconcileUtils {

	public static void renameItem(List<String> list, String oldItem, String newItem) {
		int index = list.indexOf(oldItem);
		if (index != -1) {
			if (list.contains(newItem)) 
				list.remove(index);
			else 
				list.set(index, newItem);
		}
	}

}
