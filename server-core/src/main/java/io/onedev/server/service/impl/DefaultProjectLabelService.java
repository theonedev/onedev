package io.onedev.server.service.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.service.ProjectLabelService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;
import io.onedev.server.persistence.annotation.Sessional;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;

@Singleton
public class DefaultProjectLabelService extends BaseEntityLabelService<ProjectLabel> implements ProjectLabelService {

	@Override
	protected ProjectLabel newEntityLabel(AbstractEntity entity, LabelSpec spec) {
		var label = new ProjectLabel();
		label.setProject((Project) entity);
		label.setSpec(spec);
		return label;
	}

	@Override
	public void create(ProjectLabel projectLabel) {
		Preconditions.checkState(projectLabel.isNew());
		dao.persist(projectLabel);
	}

	@Sessional
	@Override
	public void populateLabels(Collection<Project> projects) {
		var builder = getSession().getCriteriaBuilder();
		CriteriaQuery<ProjectLabel> labelQuery = builder.createQuery(ProjectLabel.class);
		Root<ProjectLabel> labelRoot = labelQuery.from(ProjectLabel.class);
		labelQuery.select(labelRoot);
		labelQuery.where(labelRoot.get(ProjectLabel.PROP_PROJECT).in(projects));

		for (var project: projects)
			project.setLabels(new ArrayList<>());

		for (var label: getSession().createQuery(labelQuery).getResultList())
			label.getProject().getLabels().add(label);			
	}

}