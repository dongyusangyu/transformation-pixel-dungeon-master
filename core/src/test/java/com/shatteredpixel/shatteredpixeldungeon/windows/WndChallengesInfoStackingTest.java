package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WndChallengesInfoStackingTest {

	@Test
	public void challengeInfoWindowAppearsAboveListWhenSceneHasAnErasedSlot() {
		Group scene = new Group();
		Gizmo closedShopWindow = new Gizmo();
		Gizmo challengeList = new Gizmo();
		Gizmo challengeInfo = new Gizmo();

		scene.add(closedShopWindow);
		scene.addToFront(challengeList);
		scene.erase(closedShopWindow);

		WndChallenges.addInfoWindowToFront(scene, challengeInfo);

		assertTrue(scene.indexOf(challengeInfo) > scene.indexOf(challengeList));
	}
}
