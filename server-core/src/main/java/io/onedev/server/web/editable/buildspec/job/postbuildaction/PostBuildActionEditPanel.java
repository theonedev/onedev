package io.onedev.server.web.editable.buildspec.job.postbuildaction;

import java.io.Serializable;
import java.util.List;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

public abstract class PostBuildActionEditPanel extends DrawCardBeanItemEditPanel<PostBuildAction>
		implements BuildSpecAware, JobAware {

	private static final long serialVersionUID = 1L;

	public PostBuildActionEditPanel(String id, List<PostBuildAction> actions, int actionIndex, EditCallback callback) {
		super(id, actions, actionIndex, callback);
	}

	@Override
	protected PostBuildAction newItem() {
		return null;
	}

	@Override
	protected String getTitle() {
		return _T("Post Build Action");
	}

	@Override
	protected Serializable newEditingBean(PostBuildAction item) {
		PostBuildActionBean bean = new PostBuildActionBean();
		bean.setAction(item);
		return bean;
	}

	@Override
	protected PostBuildAction extractItem(Serializable editingBean) {
		return ((PostBuildActionBean) editingBean).getAction();
	}

}
