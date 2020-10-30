package org.bitbucket.socialroboticshub.actions.tablet;

import java.util.List;

import eis.iilang.Parameter;

public class TabletShowVideoAction extends TabletShowAction {
	public final static String NAME = "showVideo";

	/**
	 * @param parameters A list of 1 identifier: a video URL
	 */
	public TabletShowVideoAction(final List<Parameter> parameters) {
		super("video", parameters);
	}
}
