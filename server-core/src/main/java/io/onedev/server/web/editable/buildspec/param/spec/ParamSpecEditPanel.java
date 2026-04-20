package io.onedev.server.web.editable.buildspec.param.spec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

abstract class ParamSpecEditPanel extends DrawCardBeanItemEditPanel<ParamSpec> implements InputContext {

	private static final long serialVersionUID = 1L;

	ParamSpecEditPanel(String id, List<ParamSpec> paramSpecs, int paramSpecIndex, EditCallback callback) {
		super(id, paramSpecs, paramSpecIndex, callback);
	}

	@Override
	protected ParamSpec newItem() {
		return null;
	}

	@Override
	protected String getTitle() {
		return _T("Parameter Definition");
	}

	@Override
	protected Serializable newEditingBean(ParamSpec item) {
		ParamSpecBean bean = new ParamSpecBean();
		bean.setParamSpec(item);
		return bean;
	}

	@Override
	protected ParamSpec extractItem(Serializable editingBean) {
		return ((ParamSpecBean) editingBean).getParamSpec();
	}

	@Override
	protected String additionalFormCssClass() {
		return "param-spec-edit input-spec-edit";
	}

	@Override
	protected void validateItem(BeanEditor editor, ParamSpec item) {
		int paramSpecIndex = getItemIndex();
		List<ParamSpec> paramSpecs = getItems();
		if (paramSpecIndex != -1) {
			ParamSpec oldParam = paramSpecs.get(paramSpecIndex);
			if (!item.getName().equals(oldParam.getName()) && getInputSpec(item.getName()) != null) {
				editor.error(new Path(new PathNode.Named("paramSpec"), new PathNode.Named("name")),
						"This name has already been used by another parameter");
			}
		} else if (getInputSpec(item.getName()) != null) {
			editor.error(new Path(new PathNode.Named("paramSpec"), new PathNode.Named("name")),
					"This name has already been used by another parameter");
		}
	}

	@Override
	public List<String> getInputNames() {
		List<String> paramNames = new ArrayList<>();
		int currentIndex = 0;
		int paramSpecIndex = getItemIndex();
		for (ParamSpec param : getItems()) {
			if (currentIndex != paramSpecIndex)
				paramNames.add(param.getName());
			currentIndex++;
		}
		return paramNames;
	}

	@Override
	public ParamSpec getInputSpec(String paramName) {
		for (ParamSpec param : getItems()) {
			if (paramName.equals(param.getName()))
				return param;
		}
		return null;
	}

}
