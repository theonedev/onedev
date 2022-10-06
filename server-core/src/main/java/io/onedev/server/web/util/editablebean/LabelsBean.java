package io.onedev.server.web.util.editablebean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.LabelManager;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.admin.labelmanagement.LabelManagementPage;

@Editable
public class LabelsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> labels = new ArrayList<>();

	@Editable(descriptionProvider="getLabelsDescription")
	@ChoiceProvider("getLabelChoices")
	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getLabelChoices() {
		var labels = OneDev.getInstance(LabelManager.class).query();
		return labels.stream().map(it->it.getName()).sorted().collect(Collectors.toList());
	}
	
	@SuppressWarnings("unused")
	private static String getLabelsDescription() {
		if (SecurityUtils.isAdministrator()) {
			CharSequence url = RequestCycle.get().urlFor(LabelManagementPage.class, new PageParameters());
			return String.format("Labels can be defined in <a href='%s' target='_blank'>Administration / Label Management</a>", url);
		} else {
			return "Labels can be defined in Administration / Label Management";
		}
	}

	public static LabelsBean of(LabelSupport<?> labelSupport) {
		LabelsBean bean = new LabelsBean();
		labelSupport.getLabels().stream()
				.map(it->it.getSpec().getName())
				.sorted()
				.forEach(it->bean.getLabels().add(it));
		return bean;
	}
	
}
