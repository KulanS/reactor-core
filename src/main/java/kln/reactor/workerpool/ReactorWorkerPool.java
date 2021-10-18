package kln.reactor.workerpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WorkerPool;
import kln.reactor.core.ReactorCore;
import kln.reactor.core.ReactorEvent;
import kln.reactor.core.ReactorEventFactory;
import kln.reactor.core.ReactorThreadFactory;

public class ReactorWorkerPool {
	
	private int workerHandlers;
	private int minWorkers;
	private int maxWorkers;
	private int workerPoolTimeout;
	private int queueSize;
	
	Class<? extends ReactorEventWorkHandler> c;
	private final ReactorThreadFactory reactorThreadFactory;
	private final ReactorEventFactory reactorEventFactory;
	private RingBuffer<ReactorEvent> ringBuffer;
	private SequenceBarrier sequenceBarrier;
	private ReactorEventWorkHandler[] workHandlers;
	private WorkerPool<ReactorEvent> workerPool;
	private ExecutorService executorService;
	
	private ReactorWorkerPool(Factory factory) {
		this.reactorThreadFactory = factory.reactorThreadFactory;
		this.reactorEventFactory = factory.reactorEventFactory;
		this.workerHandlers = factory.workerHandlers;
		this.minWorkers = factory.minWorkers;
		this.maxWorkers = factory.maxWorkers;
		this.workerPoolTimeout = factory.workerPoolTimeout;
		this.queueSize = factory.queueSize;
		
		this.c = factory.c;
		try {
			createExecutorService();
			createPool();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class Factory{
		private int workerHandlers;
		private int minWorkers;
		private int maxWorkers;
		private int workerPoolTimeout;
		private int queueSize;
		private ReactorThreadFactory reactorThreadFactory;
		private ReactorEventFactory reactorEventFactory;
		
		ReactorCore reactorCore;
		Class<? extends ReactorEventWorkHandler> c;
		
		public Factory setReactorEventFactory(ReactorEventFactory factory) {
			reactorEventFactory = factory;
			return this;
		}
		
		public Factory setReactorThreadFactory(ReactorThreadFactory factory) {
			reactorThreadFactory = factory;
			return this;
		}
		
		public Factory setQueueSize(int value) {
			queueSize = value;
			return this;
		}
		
		public Factory setWorkerHandlers(int value) {
			workerHandlers = value;
			return this;
		}
		
		public Factory setMinWorkers(int value) {
			minWorkers = value;
			return this;
		}
		
		public Factory setMaxWorkers(int value) {
			maxWorkers = value;
			return this;
		}
		
		public Factory setWorkerPoolTimeout(int value) {
			workerPoolTimeout = value;
			return this;
		}
		
		public Factory setWorkHandlerClass(Class<? extends ReactorEventWorkHandler> cls) {
			c = cls;
		    return this;
		}
		
		public ReactorWorkerPool create() {
			return new ReactorWorkerPool(this);
		}
	}
	
	private void createExecutorService() throws Exception {
		executorService = new ThreadPoolExecutor(
							minWorkers,
							maxWorkers,
							workerPoolTimeout,
							TimeUnit.SECONDS,
							new DisruptorBlockingQueue<Runnable>(maxWorkers),
							reactorThreadFactory,
							new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	private void createPool() throws Exception {
		ringBuffer  = RingBuffer.createMultiProducer(
				            reactorEventFactory, 
							queueSize);
		sequenceBarrier = ringBuffer.newBarrier();
		workHandlers = new ReactorEventWorkHandler[workerHandlers];
		for (int i = 0; i < workerHandlers; i++) {
			workHandlers[i] = c.asSubclass(ReactorEventWorkHandler.class).newInstance();
		}
		workerPool = new WorkerPool<ReactorEvent>(ringBuffer,
							sequenceBarrier, 
							new FatalExceptionHandler(),
				            workHandlers);
		ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
	}
	
	public void start() throws Exception {
		ringBuffer = workerPool.start(executorService);
	}
	
	public void stop() throws Exception {
		workerPool.halt();
		executorService.shutdown();
	}
	
	public void halt() throws Exception {
		workerPool.halt();
	}
	
	public void drainAndHalt() throws Exception {
		workerPool.drainAndHalt();
	}
	
	public RingBuffer<ReactorEvent> getQueue() {
		return ringBuffer;
	}
}
