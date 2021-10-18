package kln.reactor.channel.tcp;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SyncFuture<T> implements Future<T> {

    // Because the request and response are one-to-one, the CountDownLatch value is initialized to 1.
    private CountDownLatch latch = new CountDownLatch(1);
    
    // Response results that need to respond to thread settings
    private T response;
    
    // Futrue request time, used to calculate whether Future timeout
    private long beginTime = System.currentTimeMillis();

    public SyncFuture() {
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        if (response != null) {
            return true;
        }
        return false;
    }

    // Get the response result, and do not return until there is a result.
    @Override
    public T get() throws InterruptedException {
        latch.await();
        return this.response;
    }

    // Get the response result, and return it until the result is available or beyond the specified time.
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException {
        if (latch.await(timeout, unit)) {
            return this.response;
        }
        return null;
    }

    // Used to set the response result and do countDown to notify the requesting thread
    public void setResponse(T response) {
        this.response = response;
        latch.countDown();
    }

    public long getBeginTime() {
        return beginTime;
    }
}
