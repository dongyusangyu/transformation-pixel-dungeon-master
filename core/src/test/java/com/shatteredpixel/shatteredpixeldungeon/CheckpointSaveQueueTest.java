package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CheckpointSaveQueueTest {

	@Test
	public void latestPendingSnapshotReplacesOlderPendingSnapshot() throws Exception {
		List<Integer> writes = Collections.synchronizedList(new ArrayList<Integer>());
		CountDownLatch firstWriteStarted = new CountDownLatch(1);
		CountDownLatch releaseFirstWrite = new CountDownLatch(1);
		CheckpointSaveQueue<Integer> queue = new CheckpointSaveQueue<>("checkpoint-test", value -> {
			writes.add(value);
			if (value == 1) {
				firstWriteStarted.countDown();
				releaseFirstWrite.await(2, TimeUnit.SECONDS);
			}
		});

		try {
			queue.submit(1);
			assertTrue(firstWriteStarted.await(2, TimeUnit.SECONDS));
			queue.submit(2);
			queue.submit(3);
			releaseFirstWrite.countDown();

			assertTrue(queue.flush(2000));
			assertEquals(Arrays.asList(1, 3), writes);
		} finally {
			releaseFirstWrite.countDown();
			queue.shutdown();
		}
	}

	@Test
	public void flushTimesOutWhileWriterIsBusy() throws Exception {
		CountDownLatch writeStarted = new CountDownLatch(1);
		CountDownLatch releaseWrite = new CountDownLatch(1);
		CheckpointSaveQueue<Integer> queue = new CheckpointSaveQueue<>("checkpoint-test", value -> {
			writeStarted.countDown();
			releaseWrite.await(2, TimeUnit.SECONDS);
		});

		try {
			queue.submit(1);
			assertTrue(writeStarted.await(2, TimeUnit.SECONDS));
			assertFalse(queue.flush(20));
			releaseWrite.countDown();
			assertTrue(queue.flush(2000));
		} finally {
			releaseWrite.countDown();
			queue.shutdown();
		}
	}

	@Test
	public void flushReportsWriterFailure() throws Exception {
		CheckpointSaveQueue<Integer> queue = new CheckpointSaveQueue<>("checkpoint-test", value -> {
			throw new IOException("simulated checkpoint failure");
		});

		try {
			queue.submit(1);
			try {
				queue.flush(2000);
				fail("Expected the checkpoint failure to be reported");
			} catch (IOException e) {
				assertEquals("simulated checkpoint failure", e.getMessage());
			}
		} finally {
			queue.shutdown();
		}
	}
}
