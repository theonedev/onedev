package io.onedev.server.search.commit;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListCommand.Order;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;

public class OrderCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<Order> values;
	
	public OrderCriteria(List<Order> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	public List<Order> getValues() {
		return values;
	}

	@Override
	public void fill(Project project, RevListOptions options) {
		options.orders(values);
	}

	@Override
	public boolean matches(RefUpdated event) {
		// Order doesn't affect whether a commit matches, only the ordering of results
		return true;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (Order value: values) {
			switch (value) {
				case DATE:
					parts.add(getRuleName(CommitQueryLexer.OrderByDate));
					break;
				case AUTHOR_DATE:
					parts.add(getRuleName(CommitQueryLexer.OrderByAuthorDate));
					break;
				case TOPO:
					parts.add(getRuleName(CommitQueryLexer.OrderByTopo));
					break;
			}
		}
		return StringUtils.join(parts, " ");
	}
	
}
