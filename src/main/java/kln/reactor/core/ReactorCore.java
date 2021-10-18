package kln.reactor.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import kln.reactor.core.ReactorCore.Factory.MODE;


public class ReactorCore {
	
	private int ringBufferSize;
	
	private ReactorThreadFactory reactorThreadFactory;
	private RingBuffer<ReactorEvent> ringBuffer;
	private ReactorEventFactory reactorEventFactory;
	private ThreadFactory threadFactory;
	private Disruptor<ReactorEvent> disruptor;
	private ReactorCoreExceptionListner reactorCoreExceptionListner;
	private WaitStrategy waitStrategy;
	private MODE rmode;
	
	
	
	private ReactorCore(Factory factory) {
		this.reactorThreadFactory = factory.reactorThreadFactory;
		this.ringBufferSize = factory.ringBufferSize;
		this.reactorEventFactory = factory.reactorEventFactory;
		this.reactorCoreExceptionListner = factory.reactorCoreExceptionListner;
		this.rmode = factory.rmode;
		try {
			createDisruptor();
			createRingBuffer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class Factory {
		
		private int ringBufferSize;
		private ReactorThreadFactory reactorThreadFactory;
		private ReactorEventFactory reactorEventFactory;
		private ReactorCoreExceptionListner reactorCoreExceptionListner;
		private MODE rmode;
		public enum MODE {YEILD_WAIT, BUSY_SPIN_WAIT, SLEEP_WAIT, BLOCK_WAIT}
		
		public Factory setReactorThreadFactory(ReactorThreadFactory factory) {
			reactorThreadFactory = factory;
			return this;
		}
		
		public Factory setQueueSize(int size) {
			ringBufferSize = size;
			return this;
		}
		
		public Factory setMode(MODE mode) {
			rmode = mode;
			return this;
		}
		
		public Factory setReactorEventFactory(ReactorEventFactory factory) {
			reactorEventFactory = factory;
			return this;
		}
		
		public Factory setReactorCoreExceptionListner(Class<?> exceptionListner) {
			try {
				reactorCoreExceptionListner = exceptionListner.asSubclass(ReactorCoreExceptionListner.class).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return this;
		}
		
		public ReactorCore create() {
			return new ReactorCore(this);
		}
		
	}
	
	private void createDisruptor() throws Exception {
		if(rmode == MODE.YEILD_WAIT) {
			waitStrategy = new YieldingWaitStrategy();
		}else if(rmode == MODE.BUSY_SPIN_WAIT){
			waitStrategy = new BusySpinWaitStrategy();
		}else if(rmode == MODE.SLEEP_WAIT) {
			waitStrategy = new SleepingWaitStrategy();
		}else if(rmode == MODE.BLOCK_WAIT) {
			waitStrategy = new BlockingWaitStrategy();
		}
		disruptor = new Disruptor<>(
				            reactorEventFactory, 
        				    ringBufferSize, 
        				    reactorThreadFactory,
        				    ProducerType.MULTI, 
        				    waitStrategy);
		 disruptor.setDefaultExceptionHandler(reactorCoreExceptionListner);
	}
	
	private void createRingBuffer() throws Exception {
		this.ringBuffer = disruptor.getRingBuffer();
	}
	
	public EventHandlerGroup<ReactorEvent> setEventListner(ReactorEventConsumer... consumer) throws Exception {
		return disruptor.handleEventsWith(consumer); 
	}
	 
	public ReactorCore start() throws Exception {
		disruptor.start();
		return this;
	}
	
	public void stop() throws Exception {
		disruptor.shutdown();
	}
	
	public void halt() throws Exception {
		disruptor.halt();
	}
	
	public void stop(long timeOut, TimeUnit timeUnit) throws Exception {
		disruptor.shutdown(timeOut, timeUnit);
	}
	
	public RingBuffer<ReactorEvent> getQueue() {
		return ringBuffer;
	}
	
	public ThreadFactory getThreadFactory() {
		return threadFactory;
	}
	
}
