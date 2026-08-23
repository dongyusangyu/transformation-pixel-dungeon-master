package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Drunkenness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Exhilaration;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElfIllusion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.ElfWineCup;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/** Single owner for the gentleman encounter's global clock and derived entities. */
public class GentlemanElfArena extends Actor implements ElfWineCup.Listener {
	public interface Host {
		GentlemanElf boss();
		Iterable<Char> characters();
		void warnBanquet(int turnsUntilResolution);
		void resolveBanquet(Iterable<Char> targets);
		boolean respawnCup();
		default Actor actorById(int id) { return Actor.findById(id); }
		default void onCupDestroyed(Char lastHit) { }
		default void spawnIllusions() { }
		default void showTrueBodyHint() { }
	}
	private static final String BANQUET = "gentleman_banquet_turns";
	private static final String ACTIVE = "gentleman_arena_active";
	private static final String CUP = "gentleman_cup_id";
	private static final String CUP_RESPAWN = "gentleman_cup_respawn_turns";
	private static final String BOSS = "gentleman_boss_id";
	private static final String ILLUSIONS = "gentleman_illusion_ids";
	private int banquetTurns;
	private boolean active = true;
	private int cupId = -1;
	private int cupRespawnTurns = -1;
	private int bossId = -1;
	private int[] illusionIds = new int[0];
	private transient Host host;
	private transient boolean recoverMissingIllusions;

	public GentlemanElfArena() { actPriority = -25; }
	public GentlemanElfArena(Host host) { this(); bind(host); }
	public void bind(Host value) {
		host = value;
		bossId = value == null || value.boss() == null ? bossId : value.boss().id();
		rebindDerivedEntities();
	}
	public int banquetTurns() { return banquetTurns; }
	/** True once the global banquet timer is charged; the boss owns release timing. */
	public boolean banquetReady() { return banquetTurns >= 20; }
	/** Called only after the boss has completed the banquet skill. */
	public void banquetResolved() { banquetTurns = 0; }
	public void warnBanquetNow() { if (host != null) host.warnBanquet(1); }
	public void resolveBanquetNow() {
		if (host == null || !banquetReady()) return;
		LinkedHashSet<Char> targets = new LinkedHashSet<>();
		GentlemanElf boss = host.boss();
		for (Char ch : host.characters()) {
			if (ch != null && ch.isAlive() && ch != host.boss()
					&& !(ch instanceof GentlemanElfIllusion)
					&& !(ch instanceof ElfWineCup)
					&& (boss == null || ch.alignment != boss.alignment)) targets.add(ch);
		}
		host.resolveBanquet(targets);
	}
	public int cupId() { return cupId; }
	public ElfWineCup cup() {
		Actor actor = actorById(cupId);
		return actor instanceof ElfWineCup && ((ElfWineCup) actor).isAlive()
				? (ElfWineCup) actor : null;
	}
	public void cupId(int id) { cupId = id; if (id >= 0) cupRespawnTurns = -1; }
	public int cupRespawnTurns() { return cupRespawnTurns; }
	public int bossId() { return bossId; }
	public int[] illusionIds() { return illusionIds.clone(); }
	public void illusionIds(int[] ids) { illusionIds = ids == null ? new int[0] : ids.clone(); }
	public int livingIllusionCount() {
		int count = 0;
		for (int id : illusionIds) {
			Actor actor = actorById(id);
			if (actor instanceof GentlemanElfIllusion && ((GentlemanElfIllusion) actor).isAlive()) count++;
		}
		return count;
	}
	public boolean hasIllusions() { return livingIllusionCount() > 0; }
	public void pruneIllusions() {
		ArrayList<Integer> valid = new ArrayList<>();
		for (int id : illusionIds) {
			Actor actor = actorById(id);
			if (actor instanceof GentlemanElfIllusion && ((GentlemanElfIllusion) actor).isAlive()) valid.add(id);
		}
		illusionIds = new int[valid.size()];
		for (int i = 0; i < valid.size(); i++) illusionIds[i] = valid.get(i);
	}
	public void spawnCupNow() {
		if (cupId < 0 && host != null) cupRespawnTurns = host.respawnCup() ? -1 : 0;
	}
	public void spawnIllusionsNow() { if (host != null) host.spawnIllusions(); }
	public void stop() { active = false; }
	public boolean active() { return active; }
	@Override protected boolean act() {
		if (!active) { diactivate(); return true; }
		if (host == null || host.boss() == null) { active = false; diactivate(); return true; }
		if (!host.boss().isAlive()) { cleanup(); diactivate(); return true; }
		if (banquetTurns < 20) banquetTurns++;
		if (host.boss().phase() == GentlemanElf.Phase.MIRROR_TEST) host.showTrueBodyHint();
		if (cupRespawnTurns > 0) cupRespawnTurns--;
		if (cupRespawnTurns == 0 && cupId < 0 && host.respawnCup()) cupRespawnTurns = -1;
		if (recoverMissingIllusions) {
			host.spawnIllusions();
			recoverMissingIllusions = host.boss().phase() == GentlemanElf.Phase.MIRROR_TEST
					&& livingIllusionCount() == 0;
		}
		spend(TICK);
		return true;
	}
	public boolean actForTest() { return act(); }
	@Override public void onCupDestroyed(ElfWineCup cup, Char lastHit) {
		if (cupRespawnTurns >= 0) return;
		if (cupId >= 0 && (cup == null || cup.id() != cupId)) return;
		cupId = -1;
		cupRespawnTurns = 20;
		if (host != null) host.onCupDestroyed(lastHit);
	}
	public void cancelCup() {
		Actor actor = actorById(cupId);
		if (actor instanceof ElfWineCup) ((ElfWineCup) actor).dismiss();
		cupId = -1;
		cupRespawnTurns = -1;
	}
	public void clearIllusions() {
		for (int id : illusionIds) {
			Actor actor = actorById(id);
			if (actor instanceof GentlemanElfIllusion) ((GentlemanElfIllusion) actor).dismiss();
		}
		illusionIds = new int[0];
	}
	public void cleanup() {
		if (host != null) {
			for (Char character : host.characters()) {
				if (character == null) continue;
				Buff.detach(character, Drunkenness.class);
				Buff.detach(character, Exhilaration.class);
			}
		}
		cancelCup(); clearIllusions(); stop();
	}
	private void rebindDerivedEntities() {
		Actor cupActor = actorById(cupId);
		if (cupActor instanceof ElfWineCup) {
			((ElfWineCup) cupActor).listener(this);
			((ElfWineCup) cupActor).rebindLastHit(actorById(((ElfWineCup) cupActor).lastHitId()));
		}
		else {
			if (cupId >= 0) cupId = -1;
			if (host != null && host.boss() != null
					&& host.boss().phase() == GentlemanElf.Phase.CUP_CONTEST && cupRespawnTurns < 0) {
				cupRespawnTurns = 0;
			}
		}
		GentlemanElf boss = host == null ? null : host.boss();
		ArrayList<Integer> valid = new ArrayList<>();
		for (int id : illusionIds) {
			Actor actor = actorById(id);
			if (actor instanceof GentlemanElfIllusion && actor != null) {
				((GentlemanElfIllusion) actor).owner(boss);
				valid.add(id);
			}
		}
		illusionIds = new int[valid.size()];
		for (int i = 0; i < valid.size(); i++) illusionIds[i] = valid.get(i);
		recoverMissingIllusions = host != null && host.boss() != null
				&& host.boss().phase() == GentlemanElf.Phase.MIRROR_TEST
				&& illusionIds.length == 0;
	}
	private Actor actorById(int id) {
		Actor actor = host == null ? null : host.actorById(id);
		return actor == null ? Actor.findById(id) : actor;
	}
	@Override public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BANQUET, banquetTurns);
		bundle.put(ACTIVE, active);
		bundle.put(CUP, cupId);
		bundle.put(CUP_RESPAWN, cupRespawnTurns);
		bundle.put(BOSS, bossId);
		bundle.put(ILLUSIONS, illusionIds);
	}
	@Override public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		banquetTurns = Math.max(0, Math.min(20, bundle.getInt(BANQUET)));
		active = !bundle.contains(ACTIVE) || bundle.getBoolean(ACTIVE);
		cupId = bundle.contains(CUP) ? bundle.getInt(CUP) : -1;
		cupRespawnTurns = bundle.contains(CUP_RESPAWN) ? Math.max(-1, Math.min(20, bundle.getInt(CUP_RESPAWN))) : -1;
		bossId = bundle.contains(BOSS) ? bundle.getInt(BOSS) : -1;
		illusionIds = bundle.getIntArray(ILLUSIONS);
		if (illusionIds == null) illusionIds = new int[0];
	}
}
