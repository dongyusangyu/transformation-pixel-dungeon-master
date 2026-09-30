package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Infection;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlagueGuardTest {

    private static HeadlessItemSprites sprites;

    @BeforeClass
    public static void installHeadlessSprites() {
        sprites = new HeadlessItemSprites();
    }

    @AfterClass
    public static void restoreHeadlessSprites() {
        sprites.close();
    }

    @Test
    public void plagueGuardUsesStatuePresentationAndCanRecoverAfterDefeat() {
        PlagueGuard guard = new PlagueGuard();
        assertEquals(StatueSprite.class, guard.spriteClass);
        assertTrue(guard.properties().contains(Property.BOSS));
        assertTrue(guard.isImmune(Corruption.class));
        assertTrue(guard.isActiveGuard());

        guard.HP = 0;
        assertTrue(guard.isAlive());
        assertTrue(guard.recovering());
        assertFalse(guard.isActiveGuard());

        guard.reviveAfterPurifier();
        assertFalse(guard.recovering());
        assertTrue(guard.isActiveGuard());
        assertEquals(guard.HT, guard.HP);
    }

    @Test
    public void encounterDismissalIsPermanentAndDoesNotUseRecovery() {
        PlagueGuard guard = new PlagueGuard();
        guard.sprite = new CharSprite();
        guard.HP = 0;
        assertTrue(guard.isAlive());
        assertTrue(guard.recovering());

        guard.dismissAfterEncounter();
        guard.reviveAfterPurifier();

        assertFalse(guard.isAlive());
        assertFalse(guard.recovering());
        assertEquals(0, guard.HP);
        assertFalse(guard.sprite.alive);
        guard.dismissAfterEncounter();
        assertFalse(guard.isAlive());
    }

    @Test
    public void guardMeleeHitAddsOneInfectionStack() {
        PlagueGuard guard = new PlagueGuard();
        Gnoll target = new Gnoll();
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero oldHero = Dungeon.hero;
        try {
            Dungeon.hero = TestHeroFactory.create();
            Dungeon.hero.subClass = HeroSubClass.BERSERKER;
            guard.weapon().enchant(null);
            guard.attackProc(target, 10, DamageTag.PHYSICAL, DamageTag.MELEE);
            assertEquals(1, Infection.stacks(target));
        } finally {
            Dungeon.hero = oldHero;
        }
    }
}
