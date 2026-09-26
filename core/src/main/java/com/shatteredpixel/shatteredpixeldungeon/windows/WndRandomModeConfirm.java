package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;

/** Confirmation shown whenever random mode is turned on from hero selection. */
public class WndRandomModeConfirm extends WndTitledMessage {

	public WndRandomModeConfirm(Runnable onEnable) {
		super(Icons.get(Icons.SHUFFLE_SLIVER),
				Messages.get(HeroSelectScene.class, "random_mode"),
				Messages.get(HeroSelectScene.class, "random_mode_desc"));

		RedButton enable = new RedButton(Messages.get(HeroSelectScene.class, "random_mode_enable")) {
			@Override
			protected void onClick() {
				hide();
				onEnable.run();
			}
		};
		RedButton cancel = new RedButton(Messages.get(HeroSelectScene.class, "random_mode_cancel")) {
			@Override
			protected void onClick() {
				hide();
			}
		};

		enable.setRect(0, 0, width, 18);
		cancel.setRect(0, 20, width, 18);
		addToBottom(4, 0, enable, cancel);
	}
}
