package kln.reactor.core;

import com.lmax.disruptor.EventHandler;

public class ReactorEventConsumer implements EventHandler<ReactorEvent>{
	
	private ReactorEventConsumerListner listner;
	
	private ReactorEventConsumer() {}
	
	public static class Factory {
		public ReactorEventConsumer create() {
			return new ReactorEventConsumer();
		}
	}
	
	
	public void createEventListner(Class<?> alistner) throws Exception {
		listner = alistner.asSubclass(ReactorEventConsumerListner.class).newInstance();
	}
	
	@Override
	public void onEvent(ReactorEvent reactorEvent, long sequence, boolean endOfBatch) throws Exception {
		listner.onReactorEvent(reactorEvent, sequence, endOfBatch);
	}
	
}
