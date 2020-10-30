package org.bitbucket.socialroboticshub.actions.memory;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class SetUserDataAction extends RobotAction {
	public final static String NAME = "setUserData";

	/**
	 * @param parameters A list of 3 identifiers: a (string) user id, a (string)
	 *                   user data key and a corresponding user data value.
	 */
	public SetUserDataAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 3) && (getParameters().get(0) instanceof Identifier)
				&& (getParameters().get(1) instanceof Identifier); // 3rd arg can be anything
	}

	@Override
	public String getTopic() {
		return "memory_set_user_data";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0)) + ";" + EIStoString(getParameters().get(1)) + ';'
				+ EIStoString(getParameters().get(2));
	}

	@Override
	public String getExpectedEvent() {
		return "UserDataSet";
	}
}
