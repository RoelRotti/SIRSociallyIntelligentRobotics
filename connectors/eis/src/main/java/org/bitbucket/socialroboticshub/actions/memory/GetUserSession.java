package org.bitbucket.socialroboticshub.actions.memory;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class GetUserSession extends RobotAction {
	public final static String NAME = "getUserSession";

	/**
	 * @param parameters id of user.
	 */
	public GetUserSession(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "memory_user_session";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}

	@Override
	public String getExpectedEvent() {
		return null;
	}
}
