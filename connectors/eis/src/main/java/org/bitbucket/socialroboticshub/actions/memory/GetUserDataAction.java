package org.bitbucket.socialroboticshub.actions.memory;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class GetUserDataAction extends RobotAction {
	public final static String NAME = "getUserData";

	/**
	 * @param parameters A list of 2 identifiers: a (string) user id and a (string)
	 *                   user data key.
	 */
	public GetUserDataAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 2) && (getParameters().get(0) instanceof Identifier)
				&& (getParameters().get(1) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "memory_get_user_data";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0)) + ";" + EIStoString(getParameters().get(1));
	}

	@Override
	public String getExpectedEvent() {
		return null;
	}
}
