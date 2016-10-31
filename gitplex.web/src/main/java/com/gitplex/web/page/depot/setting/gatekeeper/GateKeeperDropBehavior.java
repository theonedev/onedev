package com.gitplex.web.page.depot.setting.gatekeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.gatekeeper.AndGateKeeper;
import com.gitplex.core.gatekeeper.AndOrGateKeeper;
import com.gitplex.core.gatekeeper.CompositeGateKeeper;
import com.gitplex.core.gatekeeper.ConditionalGateKeeper;
import com.gitplex.core.gatekeeper.DefaultGateKeeper;
import com.gitplex.core.gatekeeper.GateKeeper;
import com.gitplex.core.gatekeeper.NotGateKeeper;
import com.gitplex.core.manager.DepotManager;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.gitplex.commons.wicket.behavior.dragdrop.DropBehavior;

abstract class GateKeeperDropBehavior extends DropBehavior {

	private static final long serialVersionUID = 1L;

	@Override
	public void onDropped(AjaxRequestTarget target, String dragData) {
		List<Integer> dragPosition = Splitter.on(":").splitToList(dragData)
				.stream()
				.mapToInt(Integer::parseInt)
				.boxed()
				.collect(Collectors.toList());
		List<Integer> dropPosition = getPosition();
		GateKeeperPage page = (GateKeeperPage) getComponent().getPage();
		Depot depot = page.getDepot();
		AndGateKeeper root = (AndGateKeeper) depot.getGateKeeper();
		GateKeeper dragParent = getParentGateKeeper(root, dragPosition);
		GateKeeper dropParent = getParentGateKeeper(root, dropPosition);
		int dragIndex = dragPosition.get(dragPosition.size()-1);
		int dropIndex = dropPosition.get(dropPosition.size()-1);
		GateKeeper drag = getGateKeeper(dragParent, dragIndex);
		GateKeeper drop = getGateKeeper(dropParent, dropIndex);
		if (drop instanceof DefaultGateKeeper) {
			if (dragParent instanceof AndOrGateKeeper) {
				AndOrGateKeeper andOrGateKeeper = (AndOrGateKeeper) dragParent;
				andOrGateKeeper.getGateKeepers().remove(dragIndex);
			} else if (dragParent instanceof ConditionalGateKeeper) {
				ConditionalGateKeeper conditionalGateKeeper = (ConditionalGateKeeper) dragParent;
				if (dragIndex == 0)
					conditionalGateKeeper.setIfGate(new DefaultGateKeeper());
				else
					conditionalGateKeeper.setThenGate(new DefaultGateKeeper());
			} else {
				NotGateKeeper notGateKeeper = (NotGateKeeper) dragParent;
				notGateKeeper.setGateKeeper(new DefaultGateKeeper());
			}
			if (dropParent instanceof AndOrGateKeeper) {
				AndOrGateKeeper andOrGateKeeper = (AndOrGateKeeper) dropParent;
				andOrGateKeeper.getGateKeepers().add(drag);
			} else if (dropParent instanceof ConditionalGateKeeper) {
				ConditionalGateKeeper conditionalGateKeeper = (ConditionalGateKeeper) dropParent;
				if (dropIndex == 0)
					conditionalGateKeeper.setIfGate(drag);
				else
					conditionalGateKeeper.setThenGate(drag);
			} else {
				NotGateKeeper notGateKeeper = (NotGateKeeper) dropParent;
				notGateKeeper.setGateKeeper(drag);
			}
		} else {
			if (dragParent instanceof AndOrGateKeeper) {
				AndOrGateKeeper andOrGateKeeper = (AndOrGateKeeper) dragParent;
				andOrGateKeeper.getGateKeepers().set(dragIndex, drop);
			} else if (dragParent instanceof ConditionalGateKeeper) {
				ConditionalGateKeeper conditionalGateKeeper = (ConditionalGateKeeper) dragParent;
				if (dragIndex == 0)
					conditionalGateKeeper.setIfGate(drop);
				else
					conditionalGateKeeper.setThenGate(drop);
			} else {
				NotGateKeeper notGateKeeper = (NotGateKeeper) dragParent;
				notGateKeeper.setGateKeeper(drop);
			}
			if (dropParent instanceof AndOrGateKeeper) {
				AndOrGateKeeper andOrGateKeeper = (AndOrGateKeeper) dropParent;
				andOrGateKeeper.getGateKeepers().set(dropIndex, drag);
			} else if (dropParent instanceof ConditionalGateKeeper) {
				ConditionalGateKeeper conditionalGateKeeper = (ConditionalGateKeeper) dropParent;
				if (dropIndex == 0)
					conditionalGateKeeper.setIfGate(drag);
				else
					conditionalGateKeeper.setThenGate(drag);
			} else {
				NotGateKeeper notGateKeeper = (NotGateKeeper) dropParent;
				notGateKeeper.setGateKeeper(drag);
			}
		}
		depot.setGateKeepers((ArrayList<GateKeeper>) root.getGateKeepers());
		GitPlex.getInstance(DepotManager.class).save(depot, null, null);
		page.onGateKeeperChanged(target);
	}
	
	private GateKeeper getParentGateKeeper(GateKeeper rootGateKeeper, List<Integer> position) {
		Preconditions.checkArgument(!position.isEmpty() && rootGateKeeper instanceof CompositeGateKeeper);
		if (position.size() == 1) {
			return rootGateKeeper;
		} else if (rootGateKeeper instanceof AndOrGateKeeper) {
			AndOrGateKeeper andOrGateKeeper = (AndOrGateKeeper) rootGateKeeper;
			return getParentGateKeeper(andOrGateKeeper.getGateKeepers().get(position.get(0)), position.subList(1, position.size()));
		} else if (rootGateKeeper instanceof ConditionalGateKeeper) {
			ConditionalGateKeeper conditionalGateKeeper = (ConditionalGateKeeper) rootGateKeeper;
			if (position.get(0) == 0)
				return getParentGateKeeper(conditionalGateKeeper.getIfGate(), position.subList(1, position.size()));
			else
				return getParentGateKeeper(conditionalGateKeeper.getThenGate(), position.subList(1, position.size()));
		} else {
			NotGateKeeper notGateKeeper = (NotGateKeeper) rootGateKeeper;
			return getParentGateKeeper(notGateKeeper.getGateKeeper(), position.subList(1, position.size()));
		}
	}

	private GateKeeper getGateKeeper(GateKeeper parentGateKeeper, int index) {
		if (parentGateKeeper instanceof AndOrGateKeeper) {
			AndOrGateKeeper andOrGateKeeper = (AndOrGateKeeper) parentGateKeeper;
			if (index < andOrGateKeeper.getGateKeepers().size())
				return andOrGateKeeper.getGateKeepers().get(index);
			else
				return new DefaultGateKeeper();
		} else if (parentGateKeeper instanceof ConditionalGateKeeper) {
			ConditionalGateKeeper conditionalGateKeeper = (ConditionalGateKeeper) parentGateKeeper;
			if (index == 0)
				return conditionalGateKeeper.getIfGate();
			else
				return conditionalGateKeeper.getThenGate();
		} else {
			NotGateKeeper notGateKeeper = (NotGateKeeper) parentGateKeeper;
			return notGateKeeper.getGateKeeper();
		}
	}
	
	protected abstract List<Integer> getPosition();

	@Override
	protected String getAccept() {
		return String.format("function(draggable) {"
				+ "	var $drag = $(draggable).parent().parent();"
				+ "	if ($drag.hasClass('gate-keeper')) {"
				+ "		var $drop = $('#%s');"
				+ "		return !jQuery.contains($drag[0], $drop[0]) && !jQuery.contains($drop[0], $drag[0]);"
				+ "	} else {"
				+ "		return false;"
				+ "	}"
				+ "}", getComponent().getMarkupId(true));
	}	
	
}
