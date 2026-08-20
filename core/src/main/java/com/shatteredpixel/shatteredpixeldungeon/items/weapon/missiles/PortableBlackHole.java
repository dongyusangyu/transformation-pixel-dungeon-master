package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import com.watabou.utils.PathFinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A tier-six missile that can relocate other PortableBlackHole-marked chars
 * when it lands on an empty, non-pit cell.
 */
public class PortableBlackHole extends MissileWeapon {

	public static final int TIER = 6;

	{
		image = EXItemSpriteSheet.PORTABLE_BLACK_HOLE;
		tier = TIER;
		baseUses = 5;
	}

	@Override
	public int min(int lvl) {
		return 10 + Math.max(0, lvl);
	}

	@Override
	public int max(int lvl) {
		return 25 + 6 * Math.max(0, lvl);
	}

	@Override
	public int defaultQuantity() {
		return 3;
	}

	/** Portable black holes are deliberately limited to a three-item stack. */
	@Override
	public Item quantity(int value) {
		return super.quantity(Math.max(0, Math.min(defaultQuantity(), value)));
	}

	@Override
	public Item merge(Item other) {
		if (other instanceof PortableBlackHole
				&& hasSameExtractionRaidOrigin(other)
				&& isSimilar(other)) {
			int space = Math.max(0, defaultQuantity() - quantity());
			int moved = Math.min(space, other.quantity());
			if (moved > 0) {
				super.quantity(quantity() + moved);
				other.quantity(other.quantity() - moved);
			}
			return this;
		}
		return super.merge(other);
	}

	/** Returns whether an actual empty landing should start the relocation pass. */
	public static boolean shouldTeleport(boolean hasChar, boolean pit) {
		return !hasChar && !pit;
	}

	@Override
	protected void onThrow(int cell) {
		Char occupant = Actor.findChar(cell);
		if (occupant != null) {
			super.onThrow(cell);
			return;
		}

		decrementDurability();
		if (Dungeon.level != null && shouldTeleport(false, Dungeon.level.pit[cell])) {
			teleportMarkedUnits(cell);
		}

		// decrementDurability clears parent as part of its source-stack handling.
		parent = null;
		triggerSeerShot(cell);
		if (durability > 0 && !spawnedForEffect) {
			dropTriggerAt(cell);
		}
	}

	private void dropTriggerAt(int cell) {
		Heap heap = Dungeon.level.drop(this, cell);
		if (!heap.isEmpty() && heap.sprite != null) {
			heap.sprite.drop(cell);
		}
	}

	public static boolean hasPortableBlackHoleMarker(Char ch) {
		if (ch == null) return false;
		PinCushion pinCushion = ch.buff(PinCushion.class);
		if (pinCushion == null) return false;
		for (MissileWeapon item : pinCushion.getStuckItems()) {
			if (item instanceof PortableBlackHole) return true;
		}
		return false;
	}

	public static boolean canBeTransported(Char ch) {
		return ch != null
				&& ch.isAlive()
				&& Actor.findById(ch.id()) == ch
				&& !Char.hasProp(ch, Char.Property.IMMOVABLE)
				&& !ch.isImmune(ScrollOfTeleportation.class);
	}

	private void teleportMarkedUnits(int landingCell) {
		if (Dungeon.level == null) return;

		ArrayList<Char> marked = new ArrayList<>();
		for (Char ch : Actor.chars()) {
			if (hasPortableBlackHoleMarker(ch) && canBeTransported(ch)) {
				marked.add(ch);
			}
		}
		marked.sort(Comparator
				.comparingInt((Char ch) -> Dungeon.level.distance(ch.pos, landingCell))
				.thenComparingInt(Actor::id));

		ArrayList<CellDistance> candidates = connectedCandidates(landingCell);
		LinkedHashMap<Char, Integer> destinations = assignDestinations(marked, candidates);
		for (Map.Entry<Char, Integer> entry : destinations.entrySet()) {
			Char ch = entry.getKey();
			int destination = entry.getValue();
			if (canBeTransported(ch)
					&& Actor.findChar(destination) == null
					&& silentlyMove(ch, destination)) {
				// movement is deliberately immediate and has no visual effect
			}
		}
	}

	private static ArrayList<CellDistance> connectedCandidates(int landingCell) {
		ArrayList<CellDistance> result = new ArrayList<>();
		Level level = Dungeon.level;
		if (level == null || !level.insideMap(landingCell) || level.pit[landingCell]) {
			return result;
		}

		boolean[] visited = new boolean[level.length()];
		ArrayDeque<CellDistance> queue = new ArrayDeque<>();
		if (isTraversable(level, landingCell)) {
			queue.add(new CellDistance(landingCell, 0));
		} else {
			for (int offset : PathFinder.NEIGHBOURS8) {
				int neighbour = landingCell + offset;
				if (level.insideMap(neighbour) && isTraversable(level, neighbour)) {
					queue.add(new CellDistance(neighbour, 1));
				}
			}
		}

		while (!queue.isEmpty()) {
			CellDistance current = queue.removeFirst();
			if (visited[current.cell]) continue;
			visited[current.cell] = true;
			result.add(current);
			for (int offset : PathFinder.NEIGHBOURS8) {
				int neighbour = current.cell + offset;
				if (level.insideMap(neighbour)
						&& !visited[neighbour]
						&& isTraversable(level, neighbour)) {
					queue.addLast(new CellDistance(neighbour, current.distance + 1));
				}
			}
		}

		result.sort(Comparator.comparingInt((CellDistance cell) -> cell.distance)
				.thenComparingInt(cell -> cell.cell));
		return result;
	}

	private static boolean isTraversable(Level level, int cell) {
		return (level.passable[cell] || level.avoid[cell]) && !level.pit[cell];
	}

	private static LinkedHashMap<Char, Integer> assignDestinations(
			List<Char> chars, List<CellDistance> candidates) {
		LinkedHashMap<Char, Integer> result = new LinkedHashMap<>();
		Set<Integer> reserved = new HashSet<>();
		for (Char ch : chars) {
			for (CellDistance candidate : candidates) {
				int cell = candidate.cell;
				if (reserved.contains(cell) || Actor.findChar(cell) != null) continue;
				if (Char.hasProp(ch, Char.Property.LARGE)
						&& (Dungeon.level.openSpace == null || !Dungeon.level.openSpace[cell])) {
					continue;
				}
				reserved.add(cell);
				result.put(ch, cell);
				break;
			}
		}
		return result;
	}

	private static boolean silentlyMove(Char ch, int destination) {
		if (!canBeTransported(ch) || Actor.findChar(destination) != null) return false;
		ch.pos = destination;
		Dungeon.level.occupyCell(ch);
		Buff.detach(ch, Roots.class);
		if (ch.sprite != null) ch.sprite.place(destination);
		if (ch == Dungeon.hero) {
			Dungeon.observe();
			GameScene.updateFog();
			Dungeon.hero.interrupt();
		}
		return true;
	}

	private static final class CellDistance {
		private final int cell;
		private final int distance;

		private CellDistance(int cell, int distance) {
			this.cell = cell;
			this.distance = distance;
		}
	}
}
