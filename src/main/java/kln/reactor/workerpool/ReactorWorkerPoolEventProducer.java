package kln.reactor.workerpool;

import com.lmax.disruptor.RingBuffer;
import kln.reactor.core.ReactorEvent;

public class ReactorWorkerPoolEventProducer {
	
	private ReactorWorkerPool reactorWorkerPool;
	private RingBuffer<ReactorEvent> ringBuffer;
	
	private ReactorWorkerPoolEventProducer(Factory factory) {
		this.reactorWorkerPool = factory.reactorWorkerPool;
		ringBuffer = reactorWorkerPool.getQueue();
	}
	
	public static class Factory {
		
		ReactorWorkerPool reactorWorkerPool;
		
		public Factory setReactorWorkerPool(ReactorWorkerPool pool) {
			reactorWorkerPool = pool;
			return this;
		}
		
		public ReactorWorkerPoolEventProducer create() {
			return new ReactorWorkerPoolEventProducer(this);
		}
	}
	
	
	public  ReactorEvent createReactorEvent() throws Exception {
		long sequence = ringBuffer.next();
		ReactorEvent event = ringBuffer.get(sequence);
		event.setSequence(sequence);
		return event;
	}
	
	public void publishReactorEvent(ReactorEvent reactorEvent) throws Exception {
		ringBuffer.publish(reactorEvent.getSequence());
	}
	
	public void directPublishReactorEvent(ReactorEvent reactorEvent) throws Exception {
		long sequence = ringBuffer.next();
		ReactorEvent newEvent = ringBuffer.get(sequence);
		newEvent.setSequence(sequence);
		newEvent.setPayload(reactorEvent.getPayload());
		ringBuffer.publish(newEvent.getSequence());
	}
}
