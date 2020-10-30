package org.bitbucket.socialroboticshub.actions.tablet;

import java.util.List;

import eis.iilang.Parameter;

public class TabletShowWebpageAction extends TabletShowAction {
	public final static String NAME = "showWebpage";

	/**
	 * @param parameters A list of 1 identifier: a URL
	 */
	public TabletShowWebpageAction(final List<Parameter> parameters) {
		super("web", parameters);
	}
}
