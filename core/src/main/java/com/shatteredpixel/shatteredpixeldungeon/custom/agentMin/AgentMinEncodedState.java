package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

/**
 * Fixed-shape numeric observation for model training/inference.
 *
 * <p>Shape convention:
 * <ul>
 *     <li>levelTensor: [channel][y][x]</li>
 *     <li>heroVector: [feature]</li>
 *     <li>inventoryMatrix/actionMatrix/mobMatrix: [row][feature]</li>
 * </ul>
 * The readable {@link AgentMinState} is kept so debugging and reward analysis
 * can still inspect names, classes, and raw counters.</p>
 */
public class AgentMinEncodedState {

	public static final int SCHEMA_VERSION = 1;

	public static final int LEVEL_CHANNELS = 18;
	public static final int HERO_BASE_FEATURES = 28;
	public static final int INVENTORY_ROWS = 80;
	public static final int INVENTORY_FEATURES = 32;
	public static final int ACTION_ROWS = 160;
	public static final int ACTION_FEATURES = 24;
	public static final int MOB_ROWS = 32;
	public static final int MOB_FEATURES = 20;
	public static final int HISTORY_ROWS = AgentMinHistoryTracker.HISTORY_ROWS;
	public static final int HISTORY_FEATURES = AgentMinHistoryTracker.HISTORY_FEATURES;

	public int schemaVersion = SCHEMA_VERSION;
	public int width;
	public int height;
	public int heroVectorSize;
	public int talentOffset;
	public int negativeTalentOffset;
	public float pendingReward;
	public float episodeReward;

	public float[][][] levelTensor;
	public float[] heroVector;
	public float[][] inventoryMatrix;
	public float[][] actionMatrix;
	public float[][] mobMatrix;
	public float[][] historyMatrix;
	public AgentMinActionSpace actionSpace;

	public AgentMinState source;

	public float[] flattenLevelTensorCHW() {
		if (levelTensor == null || width <= 0 || height <= 0) {
			return new float[0];
		}
		float[] flat = new float[levelTensor.length * height * width];
		int index = 0;
		for (int c = 0; c < levelTensor.length; c++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					flat[index++] = levelTensor[c][y][x];
				}
			}
		}
		return flat;
	}
}
