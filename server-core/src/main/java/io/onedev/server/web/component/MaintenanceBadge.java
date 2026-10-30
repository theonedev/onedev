package io.onedev.server.web.component;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

public class MaintenanceBadge extends Label {

	private final IModel<Boolean> maintenanceModel;

	public MaintenanceBadge(String id, IModel<Boolean> maintenanceModel) {
		super(id, _T("Maintenance"));
		this.maintenanceModel = maintenanceModel;
		add(AttributeAppender.append("class", "badge badge-sm badge-warning"));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(maintenanceModel.getObject());
	}

	@Override
	protected void onDetach() {
		maintenanceModel.detach();
		super.onDetach();
	}
}
