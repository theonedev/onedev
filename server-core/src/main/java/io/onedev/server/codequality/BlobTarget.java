package io.onedev.server.codequality;

import io.onedev.commons.utils.PlanarRange;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;

import org.jspecify.annotations.Nullable;
import java.util.*;

import static java.util.Comparator.comparingInt;

public class BlobTarget extends ProblemTarget {

	private static final long serialVersionUID = 1L;

	private final PlanarRange location;

	public BlobTarget(String blobPath, @Nullable PlanarRange location) {
		super(new GroupKey(blobPath));
		this.location = location;
	}

	@Nullable
	public PlanarRange getLocation() {
		return location;
	}

	public static Map<Integer, List<CodeProblem>> groupByLine(Collection<CodeProblem> problems, List<String> lines) {
		Map<Integer, List<CodeProblem>> problemsByLine = new HashMap<>();

		for (CodeProblem problem: problems) {
			if (problem.getTarget() instanceof BlobTarget) {
				BlobTarget repoTarget = (BlobTarget) problem.getTarget();
				if (repoTarget.getLocation() != null) {
					repoTarget = new BlobTarget(repoTarget.getGroupKey().getName(), repoTarget.getLocation().normalize(lines));
					problem = new CodeProblem(problem.getSeverity(), repoTarget, problem.getMessage());
					int line = repoTarget.getLocation().getFromRow();
					var problemsAtLine = problemsByLine.computeIfAbsent(line, k -> new ArrayList<>());
					problemsAtLine.add(problem);
				}
			}
		}

		for (List<CodeProblem> value: problemsByLine.values()) {
			value.sort(comparingInt(o -> o.getSeverity().ordinal()));
		}

		return problemsByLine;
	}
	
	public static class GroupKey extends ProblemTarget.GroupKey {

		private static final long serialVersionUID = 1L;
		
		public GroupKey(String name) {
			super(name);
		}

		@Override
		public Component render(String componentId) {
			return new Label(componentId, getName()).add(AttributeAppender.append("class", "text-break"));
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof GroupKey))
				return false;
			if (this == other)
				return true;
			var otherKey = (GroupKey) other;
			return new EqualsBuilder()
					.append(getName(), otherKey.getName())
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(getName())
					.toHashCode();
		}

	}
	
}
