package org.bitbucket.socialroboticshub;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bitbucket.socialroboticshub.actions.CloseAction;
import org.bitbucket.socialroboticshub.actions.RobotAction;
import org.bitbucket.socialroboticshub.actions.audiovisual.LoadAudioAction;
import org.bitbucket.socialroboticshub.actions.audiovisual.PlayRawAudioAction;
import org.bitbucket.socialroboticshub.actions.audiovisual.SetLanguageAction;
import org.bitbucket.socialroboticshub.actions.tablet.TabletCloseAction;
import org.bitbucket.socialroboticshub.actions.tablet.TabletOpenAction;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

final class RedisProducerRunner extends RedisRunner {
	private static final String PROFILER_TYPE = "redis_send";
	private static final String[] cameraTopics = new String[] { "action_video" };
	private static final String[] microphoneTopics = new String[] { "action_audio", "dialogflow_language",
			"dialogflow_context", "dialogflow_key", "dialogflow_agent", "dialogflow_record", "dialogflow_webhook",
			"action_chat" };
	private static final String[] robotTopics = new String[] { "action_gesture", "action_eyecolour", "action_earcolour",
			"action_headcolour", "action_idle", "action_turn", "action_turn_small", "action_wakeup", "action_rest",
			"action_set_breathing", "action_posture", "action_stiffness", "action_play_motion", "action_record_motion",
			"memory_add_entry", "memory_user_session", "memory_set_user_data", "memory_get_user_data",
			"action_motion_file" };
	private static final String[] speakerTopics = new String[] { "audio_language", "action_say", "action_say_animated",
			"action_play_audio", "action_stop_talking", "action_load_audio", "action_clear_loaded_audio" };
	private static final String[] tabletTopics = new String[] { "tablet_control", "tablet_audio", "tablet_image",
			"tablet_video", "tablet_web", "render_html" };
	private static final Map<String, DeviceType> topicMap = new HashMap<>(
			speakerTopics.length + robotTopics.length + tabletTopics.length);
	static {
		for (final String topic : cameraTopics) {
			topicMap.put(topic, DeviceType.CAMERA);
		}
		for (final String topic : microphoneTopics) {
			topicMap.put(topic, DeviceType.MICROPHONE);
		}
		for (final String topic : robotTopics) {
			topicMap.put(topic, DeviceType.ROBOT);
		}
		for (final String topic : speakerTopics) {
			topicMap.put(topic, DeviceType.SPEAKER);
		}
		for (final String topic : tabletTopics) {
			topicMap.put(topic, DeviceType.TABLET);
		}
	}
	private final BlockingQueue<RobotAction> actionQueue;
	private final Profiler profiler;

	RedisProducerRunner(final CBSRenvironment parent, final Map<DeviceType, List<String>> devices) {
		super(parent, devices);
		this.actionQueue = new LinkedBlockingQueue<>();
		this.profiler = parent.getProfiler();
	}

	private void initialiseServices(final Jedis redis) {
		final Map<String, String> params = new HashMap<>();
		final Pipeline p1 = redis.pipelined();
		for (final String identifier : this.devices.get(DeviceType.CAMERA)) {
			p1.publish("people_detection", identifier);
			p1.publish("face_recognition", identifier);
			p1.publish("emotion_detection", identifier);
		}
		for (final String identifier : this.devices.get(DeviceType.MICROPHONE)) {
			initialiseDialogflow(p1, params, identifier); // voice input
		}
		for (final String identifier : this.devices.get(DeviceType.TABLET)) {
			initialiseDialogflow(p1, params, identifier); // chat input
		}
		for (final String identifier : this.devices.get(DeviceType.ROBOT)) {
			p1.publish("robot_memory", identifier);
		}
		p1.sync();
		// pass the parameters after giving the services some time to start
		final Pipeline p2 = redis.pipelined();
		for (final String key : params.keySet()) {
			p2.publish(key, params.get(key));
		}
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException ignore) {
		} finally {
			p2.sync();
		}
	}

	private void initialiseDialogflow(final Pipeline p, final Map<String, String> params, final String identifier) {
		if (!this.parent.flowKey.isEmpty() && !this.parent.flowAgent.isEmpty()) {
			p.publish("intent_detection", identifier);
			params.put(identifier + "_dialogflow_key", this.parent.flowKey);
			params.put(identifier + "_dialogflow_agent", this.parent.flowAgent);
			if (!this.parent.flowHook.isEmpty()) {
				params.put(identifier + "_dialogflow_webhook", this.parent.flowHook);
			}
			params.put(identifier + "_dialogflow_language", this.parent.flowLang);
		}
	}

	@Override
	public void run() {
		// process the action queue into outgoing messages
		final Jedis redis = getRedis();
		initialiseServices(redis);
		while (isRunning()) {
			try {
				System.out.println("Waiting for action...");
				final RobotAction next = this.actionQueue.take();
				if (next instanceof CloseAction) {
					super.shutdown();
				} else {
					System.out.println("Got " + next.getData() + " on " + next.getTopic());
					final DeviceType type = RedisProducerRunner.topicMap.get(next.getTopic());
					if (type == null) {
						throw new Exception("Unknown topic: " + next.getTopic());
					}
					byte[] data;
					if (next instanceof LoadAudioAction || next instanceof PlayRawAudioAction) {
						data = Files.readAllBytes(Paths.get(next.getData()));
					} else {
						data = next.getData().getBytes(UTF8);
					}
					final Pipeline pipe = redis.pipelined();
					for (final String identifier : this.devices.get(type)) {
						pipe.publish((identifier + "_" + next.getTopic()).getBytes(UTF8), data);
					}
					if (next instanceof SetLanguageAction) {
						for (final String identifier : this.devices.get(type)) {
							pipe.publish((identifier + "_dialogflow_language").getBytes(UTF8), data);
						}
					}
					this.profiler.start(PROFILER_TYPE);
					pipe.sync();
					this.profiler.end(PROFILER_TYPE);
					this.profiler.startRobotAction(next);
				}
				if (next instanceof TabletCloseAction) {
					this.parent.setTabletDisconnected();
				}
			} catch (final Exception e) {
				if (isRunning()) {
					e.printStackTrace();
				}
			}
		}

	}

	public void queueAction(final RobotAction action) {
		this.actionQueue.add(action);
		if (action instanceof TabletOpenAction) {
			// block-all until established
			while (!this.parent.isTabletConnected()) {
				try {
					Thread.sleep(1);
				} catch (final InterruptedException e) {
					break;
				}
			}
		}
	}

	@Override
	public void shutdown() {
		queueAction(new CloseAction());
	}
}