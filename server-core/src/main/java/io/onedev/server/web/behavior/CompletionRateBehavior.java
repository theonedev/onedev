package io.onedev.server.web.behavior;

import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

public abstract class CompletionRateBehavior extends Behavior {
	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		super.onComponentTag(component, tag);

		long total = getTotal();
		long completed = getCompleted();
		var builder = new StringBuilder("width:20px;height:20px;border-radius:50%;");
		if (total == 0) {
			var page = (BasePage) component.getPage();
			if (page.isDarkMode()) 
				builder.append("border:1px solid #535370;background-image:conic-gradient(#36364F 100%,transparent 0);");
			else
				builder.append("border:1px solid #D1D3E0;background-image:conic-gradient(#E4E6EF 100%,transparent 0);");
		} else if (completed > total) {
			builder.append("border:1px solid #F64E60;background-image:conic-gradient(#F64E60 100%,transparent 0);");
		} else {
			long ratio = completed * 100 / total;
			builder.append(String.format("border:1px solid #FFA800;background-image:conic-gradient(#FFA800 %s,transparent 0);", ratio + "%"));
		}
		tag.put("style", builder.toString());
	}

	protected abstract long getTotal();
	
	protected abstract long getCompleted();
	
}
