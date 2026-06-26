package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin.curriculum;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.custom.agentMin.AgentMinBridgeConfig;
import com.shatteredpixel.shatteredpixeldungeon.custom.agentMin.AgentMinRuntimeLog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

public class AgentMinCurriculum {

	public static final int START_DEPTH = 30;

	public enum Course {
		NONE,
		EXIT_ROOM,
		DOOR,
		PICKUP,
		COMBAT,
		DESCEND
	}

	private static Course cachedCourse;
	private static String cachedRaw;

	private AgentMinCurriculum() {
	}

	public static boolean active() {
		return course() != Course.NONE;
	}

	public static Course course() {
		String raw = value("agentmin.curriculum", "AGENTMIN_CURRICULUM", "");
		if (cachedCourse != null && same(raw, cachedRaw)) {
			return cachedCourse;
		}
		cachedRaw = raw;
		cachedCourse = parse(raw);
		return cachedCourse;
	}

	public static void prepareInitialDepth() {
		if (!AgentMinBridgeConfig.ENABLED || !active()) {
			return;
		}
		if (Dungeon.branch != 0) {
			Dungeon.branch = 0;
		}
		if (Dungeon.depth < START_DEPTH) {
			Dungeon.depth = START_DEPTH;
			AgentMinRuntimeLog.log("curriculum depth prepared: depth=" + Dungeon.depth + ", course=" + courseName());
		}
	}

	public static Level createLevel(int depth, int branch) {
		if (!AgentMinBridgeConfig.ENABLED || branch != 0 || depth < START_DEPTH) {
			return null;
		}
		Course course = course();
		if (course == Course.NONE) {
			return null;
		}
		AgentMinRuntimeLog.log("creating curriculum level: depth=" + depth + ", course=" + courseName());
		return new AgentMinCurriculumLevel(course);
	}

	public static String courseName() {
		return nameFor(course());
	}

	private static Course parse(String raw) {
		if (raw == null) {
			return Course.NONE;
		}
		String normalized = raw.trim().toLowerCase().replace('-', '_').replace(' ', '_');
		switch (normalized) {
			case "exit":
			case "exit_room":
			case "leave_room":
			case "room":
				return Course.EXIT_ROOM;
			case "door":
			case "doors":
				return Course.DOOR;
			case "pickup":
			case "pick_up":
			case "item":
			case "items":
				return Course.PICKUP;
			case "combat":
			case "fight":
			case "battle":
				return Course.COMBAT;
			case "descend":
			case "stairs":
			case "downstairs":
				return Course.DESCEND;
			default:
				return Course.NONE;
		}
	}

	private static String nameFor(Course course) {
		switch (course) {
			case EXIT_ROOM:
				return "exit_room";
			case DOOR:
				return "door";
			case PICKUP:
				return "pickup";
			case COMBAT:
				return "combat";
			case DESCEND:
				return "descend";
			case NONE:
			default:
				return "none";
		}
	}

	private static boolean same(String a, String b) {
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
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
