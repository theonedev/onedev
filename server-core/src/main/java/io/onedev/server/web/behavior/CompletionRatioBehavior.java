package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

public abstract class CompletionRatioBehavior extends Behavior {
	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		super.onComponentTag(component, tag);

		long total = getTotal();
		long completed = getCompleted();
		var builder = new StringBuilder("width:20px;height:20px;border-radius:50%;");
		if (total == 0) {
			builder.append("border:1px solid #FFA800;");
		} else if (completed > total) {
			builder.append("border:1px solid #F64E60;background-image:conic-gradient(#F64E60 100%,transparent 0);");
		} else {
			long ratio = completed * 100 / total;
			if (ratio < 50)
				builder.append(String.format("border:1px solid #FFA800;background-image:conic-gradient(#FFA800 %s,transparent 0);", ratio + "%"));
			else
				builder.append(String.format("border:1px solid #1BC5BD;background-image:conic-gradient(#1BC5BD %s,transparent 0);", ratio + "%"));
		}
		tag.put("style", builder.toString());
	}

	protected abstract long getTotal();
	
	protected abstract long getCompleted();
	
}
