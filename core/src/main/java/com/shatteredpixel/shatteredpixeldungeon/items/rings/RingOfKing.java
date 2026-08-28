package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GreatImp;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.RoyalJewelry;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.RuneString;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class RingOfKing extends Ring{
    {
        image = ItemSpriteSheet.RING_KING;
        buffClass = RingOfKing.King.class;
        icon = ItemSpriteSheet.Icons.RING_KING;
        bones = false;
        unique = true;
    }
    @Override
    public int value() {
        return 0;
    }

    public boolean curseInfusionBonus = false;
    public Weapon.Enchantment enchantment;
    public Armor.Glyph glyph;
    public Weapon weapon=new Sword();
    public Armor armor=new ClothArmor();

    public void applyEnchantment(Weapon.Enchantment enchantment) {
        this.enchantment = enchantment;
        if (enchantment != null) {
            Catalog.setSeen(enchantment.getClass());
            Statistics.discoverItemType(enchantment.getClass());
        }
    }

    public void applyGlyph(Armor.Glyph glyph) {
        this.glyph = glyph;
        if (glyph != null) {
            Catalog.setSeen(glyph.getClass());
            Statistics.discoverItemType(glyph.getClass());
        }
    }

    @Override
    public int buffedLvl() {
        int lvl = super.buffedLvl();
        if(hero!=null && hero.buff(Talent.RoyalMeal.class) != null){
            lvl+=hero.pointsInTalent(Talent.ROYAL_MEAL);
        }
        return lvl;
    }

    public Weapon getWeapon() {
        if(weapon==null){
            weapon=new Sword();
        }
        weapon.level(truelevel());
        return weapon;
    }
    public Armor getArmor() {
        if(armor==null){
            armor=new ClothArmor();
        }
        armor.level(truelevel());
        return armor;
    }
    @Override
    public boolean doEquip(Hero hero) {
        if (super.doEquip(hero)){
            hero.updateHT( false );
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if (super.doUnequip(hero, collect, single)){
            hero.updateHT( false );
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean isKnown(){
        return true;
    }
    @Override
    public boolean isUpgradable(){
        return false;
    }
    public void enchant(Item item) {
        if(item instanceof ScrollOfEnchantment){
            GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.RING_KING),
                    Messages.titleCase(this.name()),
                    Messages.get(this, "desc7"),
                    Messages.get(this, "desc8"),
                    Messages.get(this, "desc9") ) {
                @Override
                protected void onSelect( int index ) {
                    switch (index) {
                        case 0:
                            final Weapon.Enchantment enchants[] = new Weapon.Enchantment[3];
                            Class<? extends Weapon.Enchantment> existing = enchantment != null ? enchantment.getClass() : null;
                            enchants[0] = Weapon.Enchantment.randomCommon( existing );
                            enchants[1] = Weapon.Enchantment.randomUncommon( existing );
                            enchants[2] = Weapon.Enchantment.random( existing, enchants[0].getClass(), enchants[1].getClass());
                            GameScene.show(new WndEnchantSelect(RingOfKing.this, enchants[0], enchants[1], enchants[2]));
                            break;
                        case 1:
                            final Armor.Glyph glyphs[] = new Armor.Glyph[3];
                            Class<? extends Armor.Glyph> existing1 = glyph != null ? glyph.getClass() : null;
                            glyphs[0] = Armor.Glyph.randomCommon( existing1 );
                            glyphs[1] = Armor.Glyph.randomUncommon( existing1 );
                            glyphs[2] = Armor.Glyph.random( existing1, glyphs[0].getClass(), glyphs[1].getClass());
                            GameScene.show(new WndGlyphSelect(RingOfKing.this, glyphs[0], glyphs[1], glyphs[2]));
                            break;
                    }
                }
                public void onBackPressed() {}
            } );
        }else{
            GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.RING_KING),
                    Messages.titleCase(item.name()),
                    Messages.get(this, "desc7"),
                    Messages.get(this, "desc8"),
                    Messages.get(this, "desc9") ) {
                @Override
                protected void onSelect( int index ) {
                    switch (index) {
                        case 0:
                            Class<? extends Weapon.Enchantment> existing =RingOfKing.this.enchantment != null ? RingOfKing.this.enchantment.getClass() : null;
                            if((item instanceof StoneOfEnchantment || item instanceof RuneString) && hero.hasTalent(Talent.STR_RUNE)){
                                final Weapon.Enchantment enchants[] = new Weapon.Enchantment[3];
                                enchants[0] = Weapon.Enchantment.randomCommon( existing );
                                enchants[1] = Weapon.Enchantment.randomUncommon( existing );
                                enchants[2] = Weapon.Enchantment.random( existing, enchants[0].getClass(), enchants[1].getClass());
                                GameScene.show(new WndEnchantSelect1(RingOfKing.this, enchants[0], enchants[1], enchants[2]));
                            }else{
                                applyEnchantment(Weapon.Enchantment.random(existing));
                                GLog.p(Messages.get(StoneOfEnchantment.class, "ring"));
                                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                                Enchanting.show( curUser, RingOfKing.this );
                            }

                            break;
                        case 1:
                            Class<? extends Armor.Glyph> existing1 =RingOfKing.this.glyph != null ? RingOfKing.this.glyph.getClass() : null;
                            if((item instanceof StoneOfEnchantment || item instanceof RuneString) && hero.hasTalent(Talent.STR_RUNE)){
                                final Armor.Glyph glyphs[] = new Armor.Glyph[3];
                                glyphs[1] = Armor.Glyph.randomCommon( existing1 );
                                glyphs[2] = Armor.Glyph.randomUncommon( existing1 );
                                glyphs[0] = Armor.Glyph.random( existing1, glyphs[1].getClass(), glyphs[2].getClass());
                                GameScene.show(new WndGlyphSelect1(RingOfKing.this, glyphs[0], glyphs[1], glyphs[2]));
                            }else{
                                applyGlyph(Armor.Glyph.random(existing1));
                                GLog.p(Messages.get(StoneOfEnchantment.class, "ring"));
                                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                                Enchanting.show( curUser, RingOfKing.this );
                            }
                            break;
                    }
                }
                public void onBackPressed() {}
            } );
        }

    }
    public String statsInfo() {
        if (isIdentified()){
            String info = Messages.get(this, "desc1", truelevel());
            if (isEquipped(Dungeon.hero) && !cursed){
                if(enchantment != null){
                    info += "\n\n" + Messages.get(this, "desc2", enchantment.name());
                    if(Text.shouldShowRunemarkText(hero.subClass, enchantment)){
                        info += "\n\n" + Messages.get(enchantment, "runemark");
                    }
                }
                if(glyph != null){
                    info += "\n\n" + Messages.get(this, "desc3", glyph.name());
                    if(Text.shouldShowCombatMasterText(hero.subClass, glyph)){
                        info += "\n\n" + Messages.get(glyph, "combat");
                    }
                }
            }else if(isEquipped(Dungeon.hero) && cursed){
                info += "\n\n" + Messages.get(this, "desc4");
            }
            return info;
        } else {
            return Messages.get(this, "stats");
        }
    }

    static class Text {

        static boolean shouldShowRunemarkText(HeroSubClass subClass, Weapon.Enchantment enchantment) {
            return subClass != null && subClass.is(HeroSubClass.RUNEMAGE) && enchantment != null && !enchantment.curse();
        }

        static boolean shouldShowCombatMasterText(HeroSubClass subClass, Armor.Glyph glyph) {
            return subClass != null && subClass.is(HeroSubClass.COMBATMASTER) && glyph != null && !glyph.curse();
        }
    }

    private static final String GLYPH			= "glyph";
    private static final String CURSE_INFUSION_BONUS = "curse_infusion_bonus";
    private static final String ENCHANT			= "enchant";
    @Override
    public int level() {
        int level = Dungeon.hero == null ? 0 : Dungeon.hero.lvl/5;
        if (curseInfusionBonus) level += 1 + level/6;
        return level;
    }
    @Override
    public void resetUpgradeStateForNewCycle() {
        curseInfusionBonus = false;
        super.resetUpgradeStateForNewCycle();
    }
    public int truelevel() {
        int level = Dungeon.hero == null ? 0 : Dungeon.hero.lvl*2/5+(buffedLvl()-level())*2;
        if (curseInfusionBonus) level += 1 + level/6;
        return level;
    }
    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( ENCHANT, enchantment );
        bundle.put( GLYPH, glyph );
        bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );

    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        enchantment = (Weapon.Enchantment) bundle.get(ENCHANT);
        glyph=(Armor.Glyph) bundle.get(GLYPH);
        curseInfusionBonus = bundle.getBoolean( CURSE_INFUSION_BONUS );
    }
    @Override
    protected RingBuff buff( ) {
        return new King();
    }
    public class King extends RingBuff {

    }


    public static class WndEnchantSelect extends WndOptions {

        private static RingOfKing ring;
        private static Weapon.Enchantment[] enchantments;

        //used in PixelScene.restoreWindows
        public WndEnchantSelect(){
            this(ring, enchantments[0], enchantments[1], enchantments[2]);
        }

        public WndEnchantSelect(RingOfKing ring, Weapon.Enchantment ench1,
                                Weapon.Enchantment ench2, Weapon.Enchantment ench3){
            super(new ItemSprite(new ScrollOfEnchantment()),
                    Messages.titleCase(new ScrollOfEnchantment().name()),
                    Messages.get(ScrollOfEnchantment.class, "ring1"),
                    ench1.name(),
                    ench2.name(),
                    ench3.name(),
                    Messages.get(ScrollOfEnchantment.class, "cancel"));
            this.ring = ring;
            enchantments = new Weapon.Enchantment[3];
            enchantments[0] = ench1;
            enchantments[1] = ench2;
            enchantments[2] = ench3;
        }

        @Override
        protected void onSelect(int index) {
            if (index < 3) {
                ring.applyEnchantment(enchantments[index]);
                GLog.p(Messages.get(StoneOfEnchantment.class, "ring"));
                ((ScrollOfEnchantment)curItem).readAnimation();

                Sample.INSTANCE.play( Assets.Sounds.READ );
                Enchanting.show(curUser, ring);
                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );

            } else {
                GameScene.show(new WndConfirmCancel());
            }
        }

        @Override
        protected boolean hasInfo(int index) {
            return index < 3;
        }

        @Override
        protected void onInfo( int index ) {
            GameScene.show(new WndTitledMessage(
                    Icons.get(Icons.INFO),
                    Messages.titleCase(enchantments[index].name()),
                    enchantments[index].desc()));
        }

        @Override
        public void onBackPressed() {
            //do nothing, reader has to cancel
        }

    }

    public static class WndGlyphSelect extends WndOptions {

        private static RingOfKing ring;
        private static Armor.Glyph[] glyphs;

        //used in PixelScene.restoreWindows
        public WndGlyphSelect() {
            this(ring, glyphs[0], glyphs[1], glyphs[2]);
        }

        public WndGlyphSelect(RingOfKing ring, Armor.Glyph glyph1,
                              Armor.Glyph glyph2, Armor.Glyph glyph3) {
            super(new ItemSprite(new ScrollOfEnchantment()),
                    Messages.titleCase(new ScrollOfEnchantment().name()),
                    Messages.get(ScrollOfEnchantment.class, "ring2"),
                    glyph1.name(),
                    glyph2.name(),
                    glyph3.name(),
                    Messages.get(ScrollOfEnchantment.class, "cancel"));
            this.ring = ring;
            glyphs = new Armor.Glyph[3];
            glyphs[0] = glyph1;
            glyphs[1] = glyph2;
            glyphs[2] = glyph3;
        }

        @Override
        protected void onSelect(int index) {
            if (index < 3) {
                ring.applyGlyph(glyphs[index]);
                GLog.p(Messages.get(StoneOfEnchantment.class, "ring"));
                ((ScrollOfEnchantment) curItem).readAnimation();

                Sample.INSTANCE.play(Assets.Sounds.READ);
                Enchanting.show(curUser, ring);
                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
            } else {
                GameScene.show(new WndConfirmCancel());
            }
        }

        @Override
        protected boolean hasInfo(int index) {
            return index < 3;
        }

        @Override
        protected void onInfo(int index) {
            GameScene.show(new WndTitledMessage(
                    Icons.get(Icons.INFO),
                    Messages.titleCase(glyphs[index].name()),
                    glyphs[index].desc()));
        }

        @Override
        public void onBackPressed() {
            //do nothing, reader has to cancel
        }

    }

    public static class WndConfirmCancel extends WndOptions{

        public WndConfirmCancel(){
            super(new ItemSprite(new ScrollOfEnchantment()),
                    Messages.titleCase(new ScrollOfEnchantment().name()),
                    Messages.get(ScrollOfEnchantment.class, "cancel_warn"),
                    Messages.get(ScrollOfEnchantment.class, "cancel_warn_yes"),
                    Messages.get(ScrollOfEnchantment.class, "cancel_warn_no"));
        }

        @Override
        protected void onSelect(int index) {
            super.onSelect(index);
            if (index == 1){
                if (WndEnchantSelect.ring != null) {
                    GameScene.show(new WndEnchantSelect());
                } else{
                    GameScene.show(new WndGlyphSelect());
                }
            } else {
                WndEnchantSelect.ring = null;
                WndEnchantSelect.enchantments = null;
                WndGlyphSelect.glyphs = null;
            }
        }

        @Override
        public void onBackPressed() {
            //do nothing
        }
    }

    public static class WndEnchantSelect1 extends WndOptions {

        private static RingOfKing ring;
        private static Weapon.Enchantment[] enchantments;

        //used in PixelScene.restoreWindows
        public WndEnchantSelect1(){
            this(ring, enchantments[0], enchantments[1], enchantments[2]);
        }

        public WndEnchantSelect1(RingOfKing ring, Weapon.Enchantment ench1,
                                Weapon.Enchantment ench2, Weapon.Enchantment ench3){
            super(new ItemSprite(new StoneOfEnchantment()),
                    Messages.titleCase(new StoneOfEnchantment().name()),
                    Messages.get(ScrollOfEnchantment.class, "ring1"),
                    ench1.name(),
                    ench2.name(),
                    ench3.name());
            this.ring = ring;
            enchantments = new Weapon.Enchantment[3];
            enchantments[0] = ench1;
            enchantments[1] = ench2;
            enchantments[2] = ench3;
        }

        @Override
        protected void onSelect(int index) {
            if (index < 3) {
                ring.applyEnchantment(enchantments[index]);
                GLog.p(Messages.get(StoneOfEnchantment.class, "ring"));
                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                Enchanting.show( curUser, ring );

            }
        }
        @Override
        protected boolean enabled(int index) {
            if(hero.pointsInTalent(Talent.STR_RUNE)>2){
                return true;
            }else if(index==0){
                return true;
            }else if(hero.pointsInTalent(Talent.STR_RUNE)==index){
                return true;
            }else{
                return false;
            }
        }

        @Override
        protected boolean hasInfo(int index) {
            return index < 3;
        }

        @Override
        protected void onInfo( int index ) {
            GameScene.show(new WndTitledMessage(
                    Icons.get(Icons.INFO),
                    Messages.titleCase(enchantments[index].name()),
                    enchantments[index].desc()));
        }

        @Override
        public void onBackPressed() {
            //do nothing, reader has to cancel
        }

    }

    public static class WndGlyphSelect1 extends WndOptions {

        private static RingOfKing ring;
        private static Armor.Glyph[] glyphs;

        //used in PixelScene.restoreWindows
        public WndGlyphSelect1() {
            this(ring, glyphs[0], glyphs[1], glyphs[2]);
        }

        public WndGlyphSelect1(RingOfKing ring, Armor.Glyph glyph1,
                              Armor.Glyph glyph2, Armor.Glyph glyph3) {
            super(new ItemSprite(new StoneOfEnchantment()),
                    Messages.titleCase(new StoneOfEnchantment().name()),
                    Messages.get(ScrollOfEnchantment.class, "ring2"),
                    glyph1.name(),
                    glyph2.name(),
                    glyph3.name());
            this.ring = ring;
            glyphs = new Armor.Glyph[3];
            glyphs[0] = glyph1;
            glyphs[1] = glyph2;
            glyphs[2] = glyph3;
        }

        @Override
        protected void onSelect(int index) {
            if (index < 3) {
                ring.applyGlyph(glyphs[index]);
                GLog.p(Messages.get(StoneOfEnchantment.class, "ring"));
                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                Enchanting.show( curUser, ring );
            }
        }
        @Override
        protected boolean enabled(int index) {
            if(hero.pointsInTalent(Talent.STR_RUNE)>2){
                return true;
            }else if(index==0){
                return true;
            }else if(hero.pointsInTalent(Talent.STR_RUNE)==index){
                return true;
            }else{
                return false;
            }
        }

        @Override
        protected boolean hasInfo(int index) {
            return index < 3;
        }

        @Override
        protected void onInfo(int index) {
            GameScene.show(new WndTitledMessage(
                    Icons.get(Icons.INFO),
                    Messages.titleCase(glyphs[index].name()),
                    glyphs[index].desc()));
        }

        @Override
        public void onBackPressed() {
            //do nothing, reader has to cancel
        }

    }
}
