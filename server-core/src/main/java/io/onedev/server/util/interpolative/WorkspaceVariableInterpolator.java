package io.onedev.server.util.interpolative;

import static io.onedev.server.web.translation.Translation._T;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Workspace;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.workspace.WorkspaceVariable;

public class WorkspaceVariableInterpolator extends VariableInterpolator {
		
	public static final String PREFIX_SCRIPT = "script:";
			
	private final Function<String, String> variableResolver;
	
	public WorkspaceVariableInterpolator(Workspace workspace) {
		this(t -> {
			for (WorkspaceVariable var : WorkspaceVariable.values()) {
				if (var.name().toLowerCase().equals(t)) {
					String value = var.getValue(workspace);
					return value != null ? value : "";
				}
			}
			if (t.startsWith(PREFIX_SCRIPT) || t.startsWith("scripts:")) {
				String scriptName;
				if (t.startsWith(PREFIX_SCRIPT))
					scriptName = t.substring(PREFIX_SCRIPT.length());
				else
					scriptName = t.substring("scripts:".length());

				Map<String, Object> context = new HashMap<>();
				context.put("workspace", workspace);
				Object result = GroovyUtils.evalScriptByName(scriptName, context);
				if (result != null)
					return result.toString();
				else
					return "";
			} else {
				throw new ExplicitException("Unrecognized interpolation variable: " + t);
			}
		});
	}
	
	public WorkspaceVariableInterpolator(Function<String, String> variableResolver) {
		this.variableResolver = variableResolver;
	}

	@Override
	protected Function<String, String> getVariableResolver() {
		return variableResolver;
	}

	public static String getHelp() {
		return _T("<b>Tips: </b> Type <tt>@</tt> to <a href='https://docs.onedev.io/appendix/workspace-variables' target='_blank' tabindex='-1'>insert variable</a>. "
			+ "Use <tt>@@</tt> for literal <tt>@</tt>");
	}

}
