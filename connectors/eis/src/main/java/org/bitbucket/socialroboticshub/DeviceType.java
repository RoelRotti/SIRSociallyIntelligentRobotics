package org.bitbucket.socialroboticshub;

enum DeviceType {
	CAMERA("cam"), MICROPHONE("mic"), ROBOT("robot"), SPEAKER("speaker"), TABLET("tablet");

	private final String name;

	DeviceType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static DeviceType fromString(final String string) {
		switch (string) {
		case "cam":
			return CAMERA;
		case "mic":
			return MICROPHONE;
		case "robot":
			return ROBOT;
		case "speaker":
			return SPEAKER;
		case "tablet":
			return TABLET;
		default:
			return null;
		}
	}

	public static int size() {
		return 5;
	}
}
