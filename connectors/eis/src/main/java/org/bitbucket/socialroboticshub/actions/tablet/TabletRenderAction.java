package org.bitbucket.socialroboticshub.actions.tablet;

import java.util.List;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class TabletRenderAction extends TabletAction {
	public final static String NAME = "renderPage";

	/**
	 * @param parameters A list of 1 identifier: the HTML content
	 */
	public TabletRenderAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "render_html";
	}

	@Override
	public String getData() {
		return ((Identifier) getParameters().get(0)).getValue();
	}
}
