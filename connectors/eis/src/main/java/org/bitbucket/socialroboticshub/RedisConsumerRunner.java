package org.bitbucket.socialroboticshub;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bitbucket.socialrobotics.DetectionResultProto.DetectionResult;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

final class RedisConsumerRunner extends RedisRunner {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH-mm-ss");
	private static final String[] topics = new String[] { "events", "tablet_connection", "tablet_answer",
			"detected_person", "recognised_face", "audio_language", "audio_intent", "audio_newfile",
			"robot_audio_loaded", "picture_newfile", "detected_emotion", "memory_data", "robot_posture_changed",
			"robot_awake_changed", "robot_stiffness_changed", "robot_battery_charge_changed", "robot_charging_changed",
			"robot_hot_device_detected", "robot_motion_recording" };

	RedisConsumerRunner(final CBSRenvironment parent, final Map<DeviceType, List<String>> devices) {
		super(parent, devices);
	}

	private byte[][] getAllTopics() {
		final List<byte[]> result = new LinkedList<>();
		for (final List<String> identifiers : this.devices.values()) {
			for (final String identifier : identifiers) {
				for (final String topic : topics) { // FIXME: brute-force
					result.add((identifier + "_" + topic).getBytes(UTF8));
				}
			}
		}
		return result.toArray(new byte[result.size()][]);
	}

	@Override
	public void run() {
		final CBSRenvironment env = RedisConsumerRunner.this.parent;
		final Jedis redis = getRedis();
		while (isRunning()) {
			try {
				redis.subscribe(new BinaryJedisPubSub() { // process incoming messages (into percepts)
					@Override
					public void onMessage(final byte[] rawchannel, final byte[] message) {
						final String channel = new String(rawchannel, UTF8);
						final int split = channel.indexOf('_');
						// TODO: provide source-device information where relevant?
						// final String device = channel.substring(0, split);
						switch (channel.substring(split + 1)) {
						case "events":
							env.addEvent(new String(message, UTF8));
							break;
						case "tablet_connection":
							env.setTabletConnected();
							break;
						case "tablet_answer":
							env.addAnswer(new String(message, UTF8));
							break;
						case "detected_person":
							env.addDetectedPerson();
							break;
						case "recognised_face":
							env.addRecognizedFace(new String(message, UTF8));
							break;
						case "audio_language":
							env.setAudioLanguage(new String(message, UTF8));
							break;
						case "audio_intent":
							try {
								final DetectionResult result = DetectionResult.parseFrom(message);
								env.addIntent(result);
							} catch (final Exception e) {
								e.printStackTrace();
							}
							break;
						case "audio_newfile":
							final String audiofile = dateFormat.format(new Date()) + ".wav";
							try (FileOutputStream out = new FileOutputStream(audiofile)) {
								out.write(message);
								env.addAudioRecording(audiofile);
							} catch (final Exception e) {
								e.printStackTrace();
							}
							break;
						case "robot_audio_loaded":
							env.addLoadedAudioID(Integer.parseInt(new String(message, UTF8)));
							break;
						case "picture_newfile":
							final String imagefile = dateFormat.format(new Date()) + ".jpg";
							try {
								Files.write(Paths.get(imagefile), message);
								env.addPicture(imagefile);
							} catch (final Exception e) {
								e.printStackTrace();
							}
							break;
						case "detected_emotion":
							env.addDetectedEmotion(new String(message, UTF8));
							break;
						case "memory_data":
							final String[] memoryData = new String(message, UTF8).split(";");
							if (memoryData.length == 2) {
								env.addMemoryData(memoryData[0], memoryData[1]);
							} else {
								System.err.println("Mismatch in memory_data format. Format should be key;value.");
							}
							break;
						case "robot_posture_changed":
							env.addPostureChanged(new String(message, UTF8));
							break;
						case "robot_awake_changed":
							env.addIsAwake(new String(message, UTF8).equals("1"));
							break;
						case "robot_stiffness_changed":
							try {
								final int stiffness = Integer.parseInt(new String(message, UTF8));
								env.addStiffness(stiffness);
							} catch (final NumberFormatException e) {
								e.printStackTrace();
							}
							break;
						case "robot_battery_charge_changed":
							try {
								final int batteryCharge = Integer.parseInt(new String(message, UTF8));
								env.addBatteryCharge(batteryCharge);
							} catch (final NumberFormatException e) {
								e.printStackTrace();
							}
							break;
						case "robot_charging_changed":
							env.addIsCharging(new String(message, UTF8).equals("1"));
							break;
						case "robot_hot_device_detected":
							final String[] hotDevices = new String(message, UTF8).split(";");
							env.addHotDevice(hotDevices);
							break;
						case "robot_motion_recording":
							env.addMotionRecording(new String(message, UTF8));
							break;
						}
					}
				}, getAllTopics());
			} catch (final Exception e) {
				if (isRunning()) {
					e.printStackTrace();
				}
			}
		}
	}
}