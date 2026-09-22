package io.onedev.server.event.project.build;

import java.text.MessageFormat;
import java.util.Date;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;

public class BuildLabelAdded extends BuildEvent {

	private static final long serialVersionUID = 1L;

	private final String label;

	public BuildLabelAdded(@Nullable User user, Date date, Build build, String label) {
		super(user, date, build);
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String getActivity() {
		return MessageFormat.format("added label \"{0}\"", label);
	}

	@Override
	public boolean isMinor() {
		return true;
	}

}
