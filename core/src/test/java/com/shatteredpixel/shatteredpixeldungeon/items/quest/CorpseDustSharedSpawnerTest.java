package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CorpseDustSharedSpawnerTest {

    @After
    public void clearDungeonHero() {
        Dungeon.hero = null;
    }

    public static final class TrackingSpawner extends CorpseDust.DustGhostSpawner {
        boolean dispelled;

        @Override
        public void dispel() {
            dispelled = true;
        }
    }

    @Test
    public void sourcePredicateDistinguishesDustAndCursedEquippedBook() {
        Hero hero = TestHeroFactory.create();
        assertFalse(CorpseDust.hasGhostSpawnerSource(hero));

        CorpseDust dust = TestHeroFactory.allocateItem(CorpseDust.class);
        dust.quantity(1);
        hero.belongings.backpack.items.add(dust);
        assertTrue(CorpseDust.hasGhostSpawnerSource(hero));

        hero.belongings.backpack.items.clear();
        Necronomicon book = new Necronomicon();
        book.cursed = true;
        hero.belongings.artifact = book;
        assertTrue(CorpseDust.hasCursedBookSource(hero));
        assertTrue(CorpseDust.hasGhostSpawnerSource(hero));
    }

    @Test
    public void alchemyTransferDoesNotDispelSpawner() {
        Hero hero = TestHeroFactory.create();
        Dungeon.hero = hero;
        CorpseDust dust = TestHeroFactory.allocateItem(CorpseDust.class);
        dust.quantity(1);
        hero.belongings.backpack.items.add(dust);
        TrackingSpawner spawner = Buff.affect(hero, TrackingSpawner.class);
        assertNotNull(spawner);

        CorpseDust detached = dust.detachForAlchemy(hero.belongings.backpack);

        assertSame(dust, detached);
        assertFalse(spawner.dispelled);
        assertFalse(CorpseDust.hasDustSource(hero));
    }

    @Test
    public void permanentDetachStillDispelsSpawner() {
        Hero hero = TestHeroFactory.create();
        Dungeon.hero = hero;
        CorpseDust dust = TestHeroFactory.allocateItem(CorpseDust.class);
        dust.quantity(1);
        hero.belongings.backpack.items.add(dust);
        TrackingSpawner spawner = Buff.affect(hero, TrackingSpawner.class);
        assertNotNull(spawner);

        dust.detachAll(hero.belongings.backpack);

        assertTrue(spawner.dispelled);
    }

    @Test
    public void collectingDustRestoresMissingSpawner() {
        Hero hero = TestHeroFactory.create();
        Dungeon.hero = hero;
        CorpseDust dust = TestHeroFactory.allocateItem(CorpseDust.class);
        dust.quantity(1);

        assertTrue(dust.collect(hero.belongings.backpack));

        assertNotNull(hero.buff(CorpseDust.DustGhostSpawner.class));
    }
}
