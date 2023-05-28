package io.onedev.server.web.util.editablebean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.model.support.LabelSupport;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class LabelsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> labels = new ArrayList<>();

	@Editable(description="Labels can be defined in Administration / Label Management")
	@ChoiceProvider("getLabelChoices")
	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getLabelChoices() {
		var labels = OneDev.getInstance(LabelSpecManager.class).query();
		return labels.stream().map(it->it.getName()).sorted().collect(Collectors.toList());
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
