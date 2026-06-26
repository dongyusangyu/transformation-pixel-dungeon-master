package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

public class AgentMinBridgeConfig {

	public static boolean ENABLED = Boolean.parseBoolean(value("agentmin.enabled", "AGENTMIN_ENABLED", "false"));
	public static String HOST = value("agentmin.host", "AGENTMIN_HOST", "127.0.0.1");
	public static int PORT = Integer.parseInt(value("agentmin.port", "AGENTMIN_PORT", "8765"));
	public static int TIMEOUT_MS = Integer.parseInt(value("agentmin.timeout_ms", "AGENTMIN_TIMEOUT_MS", "10000"));
	public static boolean LOGGING = Boolean.parseBoolean(value("agentmin.logging", "AGENTMIN_LOGGING", "true"));
	public static int GLOG_STEP_INTERVAL = Math.max(1, Integer.parseInt(value("agentmin.glog_step_interval", "AGENTMIN_GLOG_STEP_INTERVAL", "10")));
	// Debug-only probe: while testing a Mage run, select a random legal wand
	// target whenever one is visible. Normal training leaves this disabled.
	public static boolean WAND_PROBE = Boolean.parseBoolean(value("agentmin.wand_probe", "AGENTMIN_WAND_PROBE", "false"));

	private AgentMinBridgeConfig() {
	}

	private static String value(String property, String env, String fallback) {
		String result = System.getProperty(property);
		if (result != null && result.length() > 0) {
			return result;
		}
		result = System.getenv(env);
		if (result != null && result.length() > 0) {
			return result;
		}
		return fallback;
	}
}
