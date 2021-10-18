package kln.reactor.core;

import com.lmax.disruptor.RingBuffer;

public class ReactorEventProducer {
	
	private ReactorCore core;
	private final RingBuffer<ReactorEvent> ringBuffer;
	
	private ReactorEventProducer(Factory factory) {
		this.core = factory.rcore;
		ringBuffer = core.getQueue();
	}
	
	public static class Factory {
		
		ReactorCore rcore;
		
		public Factory setReactorCore(ReactorCore core) {
			rcore = core;
			return this;
		}
		
		public ReactorEventProducer create() {
			return new ReactorEventProducer(this);
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
