package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.watabou.utils.Bundle;

import java.util.List;

final class MobPlacementState {

    static final String CATEGORY_KEY = "category_key";
    static final String MOB_CLASS = "mob_class";
    static final String ELITE_OPTIONS = "elite_options";

    String categoryKey;
    String mobClassName;
    int eliteOptions;

    MobPlacementState(String categoryKey, String mobClassName, int eliteOptions) {
        this.categoryKey = categoryKey;
        this.mobClassName = mobClassName;
        this.eliteOptions = eliteOptions;
    }

    void normalize(List<MobPlacementCatalog.Page> pages) {
        MobPlacementCatalog.Page page = MobPlacementCatalog.findPage(pages, categoryKey);
        if (page == null) return;
        categoryKey = page.key();
        Class<?> type = MobPlacementCatalog.resolveClass(page, mobClassName);
        mobClassName = type == null ? null : type.getName();
    }

    void storeInBundle(Bundle bundle) {
        bundle.put(CATEGORY_KEY, categoryKey);
        bundle.put(MOB_CLASS, mobClassName == null ? "" : mobClassName);
        bundle.put(ELITE_OPTIONS, eliteOptions);
    }

    static MobPlacementState restoreMob(Bundle bundle) {
        if (bundle.contains(CATEGORY_KEY)) {
            return new MobPlacementState(bundle.getString(CATEGORY_KEY),
                    bundle.getString(MOB_CLASS), bundle.getInt(ELITE_OPTIONS));
        }
        int eliteOptions = bundle.contains("elite_ops")
                ? bundle.getInt("elite_ops") : bundle.getInt("eliteTags");
        return new MobPlacementState("regional", null, eliteOptions);
    }

    static MobPlacementState restoreBoss(Bundle bundle) {
        if (bundle.contains(CATEGORY_KEY)) {
            return new MobPlacementState(bundle.getString(CATEGORY_KEY),
                    bundle.getString(MOB_CLASS), 0);
        }
        return new MobPlacementState("test_bosses", null, 0);
    }
}
