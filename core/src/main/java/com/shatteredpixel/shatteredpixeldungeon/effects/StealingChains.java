package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

/**
 * A two-phase chain effect used by chain-shadow thieves.
 * The chain reaches the target, collects the stolen item, and retracts it
 * back to the thief while keeping the item sprite on the moving chain tip.
 */
public class StealingChains extends Group {

	private static final double DEGREES = 180 / Math.PI;
	private static final float ITEM_OFFSET_Y = -2f;

	public interface ItemProvider {
		Item provide();
	}

	private final PointF from;
	private final PointF to;
	private final Image[] chains;
	private final float legDuration;
	private final ItemProvider itemProvider;
	private final Callback completion;

	private float spent;
	private boolean contactCalled;
	private boolean completionCalled;
	private ItemSprite carriedSprite;

	public StealingChains(PointF from, PointF to, Effects.Type type,
						 ItemProvider itemProvider, Callback completion) {
		this.from = new PointF(from.x, from.y);
		this.to = new PointF(to.x, to.y);
		this.itemProvider = itemProvider;
		this.completion = completion;

		float dx = to.x - from.x;
		float dy = to.y - from.y;
		float distance = (float) Math.hypot(dx, dy);
		legDuration = distance / 320f + 0.2f;

		int count = Math.round(distance / 6f) + 1;
		chains = new Image[count];
		float rotation = (float) (Math.atan2(dy, dx) * DEGREES) + 90f;
		for (int i = 0; i < chains.length; i++) {
			chains[i] = new Image(Effects.get(type));
			chains[i].angle = rotation;
			chains[i].origin.set(chains[i].width() / 2f, chains[i].height());
			add(chains[i]);
		}
	}

	/**
	 * Returns the moving, free end of the chain for a total animation progress
	 * in [0, 1]. The first half travels to the target and the second half
	 * returns to the origin.
	 */
	public static PointF tipPosition(PointF from, PointF to, float progress) {
		float clamped = Math.max(0f, Math.min(1f, progress));
		float legProgress = clamped <= 0.5f
				? clamped * 2f
				: (1f - clamped) * 2f;
		return new PointF(
				from.x + (to.x - from.x) * legProgress,
				from.y + (to.y - from.y) * legProgress);
	}

	@Override
	public void update() {
		super.update();

		float progress = Math.min(1f, (spent += Game.elapsed) / (legDuration * 2f));
		if (!contactCalled && progress >= 0.5f) {
			contactCalled = true;
			Item carried = itemProvider == null ? null : itemProvider.provide();
			if (carried != null) {
				carriedSprite = new ItemSprite(carried);
				carriedSprite.originToCenter();
				add(carriedSprite);
			}
		}

		PointF tip = tipPosition(from, to, progress);
		layoutChains(tip);
		if (carriedSprite != null) {
			carriedSprite.center(new PointF(tip.x, tip.y + ITEM_OFFSET_Y));
		}

		if (progress >= 1f) finish();
	}

	private void layoutChains(PointF tip) {
		float currentDistance = (float) Math.hypot(tip.x - from.x, tip.y - from.y);
		int active = Math.min(chains.length,
				Math.max(1, Math.round(currentDistance / 6f) + 1));
		int denominator = Math.max(1, active - 1);

		for (int i = 0; i < chains.length; i++) {
			chains[i].visible = i < active;
			if (chains[i].visible) {
				float segment = i / (float) denominator;
				chains[i].center(new PointF(
						from.x + (tip.x - from.x) * segment,
						from.y + (tip.y - from.y) * segment));
			}
		}
	}

	private void finish() {
		if (completionCalled) return;
		completionCalled = true;
		killAndErase();
		if (completion != null) completion.call();
	}
}
