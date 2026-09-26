package com.shatteredpixel.shatteredpixeldungeon.actors;

import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ActorCurrentRemovalTest {

    private final Field currentField = currentField();
    private Actor previousCurrent;

    @After
    public void restoreSchedulerState() throws IllegalAccessException {
        currentField.set(null, previousCurrent);
    }

    @Test
    public void removingCurrentActorClearsProcessingState() throws IllegalAccessException {
        Actor current = new Actor() {
            @Override protected boolean act() { return false; }
        };
        previousCurrent = (Actor) currentField.get(null);
        currentField.set(null, current);
        assertTrue(Actor.processing());

        Actor.remove(current);

        assertFalse("a removed actor must not keep the scheduler marked busy", Actor.processing());
    }

    private static Field currentField() {
        try {
            Field field = Actor.class.getDeclaredField("current");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }
}
