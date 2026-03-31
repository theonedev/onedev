package io.onedev.server.util.interpolative;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import io.onedev.server.util.interpolative.Interpolative.Segment;
import io.onedev.server.util.interpolative.Interpolative.Segment.Type;
import io.onedev.server.web.editable.EditableStringTransformer;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.WicketUtils;

public abstract class VariableInterpolator {

	private final EditableStringTransformer beanPropertyTransformer;
	
	public VariableInterpolator() {
		this.beanPropertyTransformer = new EditableStringTransformer(this::interpolate);
	}
	
	@Nullable
	public String interpolate(@Nullable String value) {
		if (value != null) {
			Interpolative interpolative = Interpolative.parse(value);
			StringBuilder builder = new StringBuilder();
			for (Segment segment: interpolative.getSegments(null)) {
				if (segment.getType() == Type.LITERAL) {
					builder.append(segment.getContent());
				} else {
					String interpolated = getVariableResolver().apply(segment.getContent()); 
					if (interpolated != null)
						builder.append(interpolated);
				}
			}
			// Should not return null here even if result is empty in order not to 
			// surprise caller. For instance command step may have empty line in commands
			// and we should not convert them to null after interpolation
			return builder.toString();
		} else {
			return null;
		}
	}
	
	public <T> T interpolateProperties(T object) {
		return beanPropertyTransformer.transformProperties(
				object, 
				io.onedev.server.annotation.Interpolative.class);
	}	

	protected abstract Function<String, String> getVariableResolver();

	public static String getHelp() {
		if (WicketUtils.getPage() instanceof ProjectBlobPage)
			return JobVariableInterpolator.getHelp();
		else
			return WorkspaceVariableInterpolator.getHelp();
	}

}
