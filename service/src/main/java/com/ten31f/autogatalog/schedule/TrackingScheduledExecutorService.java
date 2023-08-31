package com.ten31f.autogatalog.schedule;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ten31f.autogatalog.domain.Gat;
import com.ten31f.autogatalog.taskinterface.GatBased;

public class TrackingScheduledExecutorService implements ScheduledExecutorService {

	private ScheduledExecutorService scheduledExecutorService = null;

	private Map<String, ScheduledFuture<?>> futures = null;

	public TrackingScheduledExecutorService() {
		setScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
		setFutures(new HashMap<>());
	}

	@Override
	public void shutdown() {
		getScheduledExecutorService().shutdown();

	}

	@Override
	public List<Runnable> shutdownNow() {
		return getScheduledExecutorService().shutdownNow();

	}

	@Override
	public boolean isShutdown() {
		return getScheduledExecutorService().isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return getScheduledExecutorService().isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return getScheduledExecutorService().awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return getScheduledExecutorService().submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return getScheduledExecutorService().submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return getScheduledExecutorService().submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return getScheduledExecutorService().invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return getScheduledExecutorService().invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return getScheduledExecutorService().invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return getScheduledExecutorService().invokeAny(tasks, timeout, unit);
	}

	@Override
	public void execute(Runnable command) {
		getScheduledExecutorService().execute(command);

	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {

		ScheduledFuture<?> scheduledFuture = getScheduledExecutorService().schedule(command, delay, unit);

		if (command instanceof GatBased gatBased) {
			getFutures().put(gatBased.getGat().getGuid(), scheduledFuture);
		}

		return scheduledFuture;
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return getScheduledExecutorService().schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {

		ScheduledFuture<?> scheduledFuture = getScheduledExecutorService().scheduleAtFixedRate(command, initialDelay,
				period, unit);

		if (command instanceof GatBased gatBased) {
			getFutures().put(gatBased.getGat().getGuid(), scheduledFuture);
		}

		return scheduledFuture;
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {

		ScheduledFuture<?> scheduledFuture = getScheduledExecutorService().scheduleAtFixedRate(command, initialDelay,
				delay, unit);

		if (command instanceof GatBased gatBased) {
			getFutures().put(gatBased.getGat().getGuid(), scheduledFuture);
		}

		return scheduledFuture;
	}

	private ScheduledExecutorService getScheduledExecutorService() {
		return scheduledExecutorService;
	}

	private void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	private Map<String, ScheduledFuture<?>> getFutures() {
		return futures;
	}

	private void setFutures(Map<String, ScheduledFuture<?>> futures) {
		this.futures = futures;
	}

	public boolean handled(Gat gat) {

		if (!getFutures().containsKey(gat.getGuid()))
			return false;

		return !getFutures().get(gat.getGuid()).isDone();
	}

	public void cancel(Gat gat) {

		if (!getFutures().containsKey(gat.getGuid()))
			return;

		getFutures().get(gat.getGuid()).cancel(false);
	}

}
