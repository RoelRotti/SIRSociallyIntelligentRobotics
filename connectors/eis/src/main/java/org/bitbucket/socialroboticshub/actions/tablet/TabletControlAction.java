package org.bitbucket.socialroboticshub.actions.tablet;

abstract class TabletControlAction extends TabletAction {
	private final boolean open;

	TabletControlAction(final boolean open) {
		super(null);
		this.open = open;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "tablet_control";
	}

	@Override
	public String getData() {
		return this.open ? "show" : "hide";
	}

}
