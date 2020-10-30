package org.bitbucket.socialroboticshub.actions.tablet;

import java.util.List;

import eis.iilang.Parameter;

public class TabletShowImageAction extends TabletShowAction {
	public final static String NAME = "showImage";

	/**
	 * @param parameters A list of 1 identifier: an image URL
	 */
	public TabletShowImageAction(final List<Parameter> parameters) {
		super("image", parameters);
	}
}
