package org.bitbucket.socialroboticshub.actions.tablet;

import java.util.List;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

abstract class TabletShowAction extends TabletAction {
	protected final String type;

	TabletShowAction(final String type, final List<Parameter> parameters) {
		super(parameters);
		this.type = type;
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "tablet_" + this.type;
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}
}
