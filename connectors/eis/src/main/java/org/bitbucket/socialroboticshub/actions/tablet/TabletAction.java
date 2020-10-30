package org.bitbucket.socialroboticshub.actions.tablet;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Parameter;

abstract class TabletAction extends RobotAction {
	TabletAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public String getExpectedEvent() {
		return null;
	}
}
