package kln.reactor.core;

public abstract class ReactorEventConsumerListner {
	
	public abstract void onReactorEvent(ReactorEvent reactorEvent, long sequence, boolean endOfBatch);

}
