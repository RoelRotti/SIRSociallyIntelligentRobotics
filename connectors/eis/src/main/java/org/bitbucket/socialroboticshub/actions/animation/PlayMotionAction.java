package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class PlayMotionAction extends RobotAction {
	public final static String NAME = "playMotion";

	/**
	 * @param parameters A list with one identifier, being a motion with the format
	 *                   "{'robot': 'nao/pepper', 'joints': {'joint1': { 'angles':
	 *                   [...], 'times': [...]}, 'joint2': {...}}}"
	 */
	public PlayMotionAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return getParameters().size() == 1 && (getParameters().get(0) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "action_play_motion";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}

	@Override
	public String getExpectedEvent() {
		return "PlayMotionStarted";
	}
}
