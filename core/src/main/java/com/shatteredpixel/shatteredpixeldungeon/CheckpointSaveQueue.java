package com.shatteredpixel.shatteredpixeldungeon;

import java.io.IOException;

/** Serializes checkpoint writes while retaining only the newest pending snapshot. */
final class CheckpointSaveQueue<T> {

	interface Writer<T> {
		void write(T value) throws Exception;
	}

	private final Object lock = new Object();
	private final Writer<T> writer;
	private final Thread worker;

	private T pending;
	private boolean writing;
	private boolean shutdown;
	private IOException failure;

	CheckpointSaveQueue(String threadName, Writer<T> writer) {
		this.writer = writer;
		worker = new Thread(this::run, threadName);
		worker.setDaemon(true);
		worker.start();
	}

	void submit(T value) {
		synchronized (lock) {
			if (shutdown) return;
			pending = value;
			lock.notifyAll();
		}
	}

	boolean flush(long timeoutMillis) throws IOException {
		long deadline = System.nanoTime() + Math.max(0L, timeoutMillis) * 1_000_000L;
		synchronized (lock) {
			while ((writing || pending != null) && !shutdown) {
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0L) return false;
				try {
					long millis = remaining / 1_000_000L;
					int nanos = (int) (remaining % 1_000_000L);
					lock.wait(millis, nanos);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IOException("Interrupted while waiting for checkpoint save", e);
				}
			}
			if (failure != null) {
				IOException result = failure;
				failure = null;
				throw result;
			}
			return !writing && pending == null;
		}
	}

	IOException pollFailure() {
		synchronized (lock) {
			IOException result = failure;
			failure = null;
			return result;
		}
	}

	void shutdown() {
		synchronized (lock) {
			shutdown = true;
			pending = null;
			lock.notifyAll();
		}
		worker.interrupt();
	}

	private void run() {
		while (true) {
			T value;
			synchronized (lock) {
				while (pending == null && !shutdown) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						if (shutdown) return;
					}
				}
				if (shutdown) return;
				value = pending;
				pending = null;
				writing = true;
			}

			try {
				writer.write(value);
			} catch (Exception e) {
				synchronized (lock) {
					failure = e instanceof IOException
							? (IOException) e
							: new IOException("Checkpoint save failed", e);
				}
			} finally {
				synchronized (lock) {
					writing = false;
					lock.notifyAll();
				}
			}
		}
	}
}
