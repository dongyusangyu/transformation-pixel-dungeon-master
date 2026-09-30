package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.GameSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Map;
import static org.junit.Assert.*;

public class RaidDroneCallbacksTest {
	private HeadlessItemSprites sheets;
	private Application oldApp;
	private Preferences oldPrefs;
	private Level oldLevel;
	private boolean animations = true;
	private boolean oldSoundEnabled;
	private final ArrayDeque<Runnable> callbacks = new ArrayDeque<>();
	private Map<Object, Sound> sounds;
	private Sound oldDeathSound;
	private int deathSounds;

	@Before @SuppressWarnings("unchecked") public void setUp() throws Exception {
		sheets = RaidDroneSpritesTest.sheets();
		oldApp = Gdx.app;
		oldLevel = Dungeon.level;
		Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
				new Class[]{Application.class}, (p, m, a) -> {
					if (m.getName().equals("postRunnable")) callbacks.add((Runnable) a[0]);
					return null;
				});
		Field prefs = GameSettings.class.getDeclaredField("prefs");
		prefs.setAccessible(true);
		oldPrefs = (Preferences) prefs.get(null);
		GameSettings.set((Preferences) Proxy.newProxyInstance(Preferences.class.getClassLoader(),
				new Class[]{Preferences.class}, (p, m, a) -> {
					if (m.getName().equals("getBoolean") && SPDSettings.KEY_CHAR_ANIMATIONS.equals(a[0])) return animations;
					if (m.getName().startsWith("get") && a != null && a.length == 2) return a[1];
					if (m.getReturnType() == boolean.class) return false;
					return null;
				}));
		Dungeon.level = new TestLevel();
		Dungeon.level.setSize(7, 7);
		Field ids = Sample.class.getDeclaredField("ids");
		ids.setAccessible(true);
		sounds = (Map<Object, Sound>) ids.get(Sample.INSTANCE);
		oldDeathSound = sounds.put(Assets.Sounds.DRONEDIED, (Sound) Proxy.newProxyInstance(
				Sound.class.getClassLoader(), new Class[]{Sound.class}, (p, m, a) -> {
					if (m.getName().equals("play")) { deathSounds++; return 1L; }
					return null;
				}));
		oldSoundEnabled = Sample.INSTANCE.isEnabled();
		Sample.INSTANCE.enable(true);
	}

	@After public void tearDown() {
		if (oldDeathSound == null) sounds.remove(Assets.Sounds.DRONEDIED);
		else sounds.put(Assets.Sounds.DRONEDIED, oldDeathSound);
		Sample.INSTANCE.enable(oldSoundEnabled);
		GameSettings.set(oldPrefs);
		Gdx.app = oldApp;
		Dungeon.level = oldLevel;
		sheets.close();
	}

	@Test public void sixAnimatedShotsEachCompleteExactlyOnceWithIronGunBullets() {
		verifySixShots(true);
	}
	@Test public void sixShotsStillCompleteWithAnimationsDisabled() {
		verifySixShots(false);
	}
	private void verifySixShots(boolean animated) {
		animations = animated;
		CountingMob mob = new CountingMob();
		RaidDroneSprites.Gunner sprite = new RaidDroneSprites.Gunner();
		sprite.ch = mob;
		ShotGroup group = new ShotGroup();
		group.add(sprite);
		for (int shot = 1; shot <= 6; shot++) {
			sprite.attack(45);
			if (animated) {
				sprite.onComplete(sprite.zap);
				assertEquals(shot, group.shots);
				group.missile.onComplete((com.watabou.noosa.tweeners.Tweener) null);
				sprite.onComplete(sprite.zap);
			}
			drain();
			assertEquals(shot, group.shots);
			assertTrue(group.item instanceof DronesSprite.Bullet);
			assertEquals(shot, mob.attacks);
		}
	}

	@Test public void detachedOrDeadShooterDoesNotCompleteAStaleShot() {
		for (boolean detached : new boolean[]{false, true}) {
			animations = false;
			CountingMob mob = new CountingMob();
			RaidDroneSprites.Gunner sprite = new RaidDroneSprites.Gunner();
			sprite.ch = mob;
			ShotGroup group = new ShotGroup();
			group.add(sprite);
			sprite.attack(45);
			if (detached) sprite.ch = null; else mob.HP = 0;
			drain();
			assertEquals(0, group.shots);
			assertEquals(0, mob.attacks);
		}
	}

	@Test public void visibleDeathsUseOneDroneSoundWithEitherAnimationSetting() {
		for (boolean animated : new boolean[]{true, false}) {
			animations = animated;
			for (Mob mob : new Mob[]{new VaultArmoredStatue(), new VeilbreakerEye(), new ChronoSuccubus(), new DeferredScorpio()}) {
				CharSprite sprite = mob.sprite();
				sprite.visible = true;
				sprite.die();
				sprite.die();
				drain();
			}
		}
		assertEquals(8, deathSounds);
	}

	@Test public void cleanupAndOffscreenDeathAreSilent() {
		CharSprite sprite = new RaidDroneSprites.Gunner();
		sprite.visible = false;
		sprite.die();
		sprite.kill();
		new RaidDroneSprites.Siren().kill();
		assertEquals(0, deathSounds);
	}

	private void drain() { while (!callbacks.isEmpty()) callbacks.remove().run(); }
	private static class CountingMob extends Mob {
		int attacks;
		CountingMob() { pos = 17; HP = HT = 20; }
		@Override public void onAttackComplete() { attacks++; }
	}
	private static class ShotGroup extends Group {
		int shots;
		Item item;
		MissileSprite missile;
		@Override public synchronized Gizmo recycle(Class<? extends Gizmo> type) {
			if (type != MissileSprite.class) return super.recycle(type);
			missile = new MissileSprite() {
				@Override public void reset(Visual from, int to, Item projectile, Callback callback) {
					shots++; item = projectile;
					super.reset(from, to, projectile, callback);
				}
			};
			return add(missile);
		}
	}
	private static class TestLevel extends Level {
		@Override protected boolean build() { return true; }
		@Override protected void createMobs() {}
		@Override protected void createItems() {}
		@Override public int randomRespawnCell(com.shatteredpixel.shatteredpixeldungeon.actors.Char ch) { return 1; }
	}
}
