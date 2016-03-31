package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pmease.gitplex.core.entity.Depot;

public class DepotVisits implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int MAX_VISITED = 100;
	
	private List<Long> visited = new ArrayList<>();
	
	public void visit(Depot depot) {
		visited.remove(depot.getId());
		visited.add(0, depot.getId());
		if (visited.size() > MAX_VISITED) {
			for (int i=MAX_VISITED; i<visited.size(); i++) {
				visited.remove(i);
			}
		}
	}

	public Comparator<Depot> getComparator() {
		Map<Long, Integer> visitOrder = new HashMap<>();
		for (int i=0; i<visited.size(); i++) {
			visitOrder.put(visited.get(i), i);
		}

		return (depot1, depot2) -> {
			Integer order1 = visitOrder.get(depot1.getId());
			Integer order2 = visitOrder.get(depot2.getId());
			if (order1 != null && order2 != null) {
				return order1 - order2;
			} else if (order1 != null && order2 == null) {
				return -1;
			} else if (order1 == null && order2 != null) {
				return 1;
			} else {
				return depot1.compareTo(depot2);
			}
		};
	}
}
