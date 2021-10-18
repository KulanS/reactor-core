package kln.reactor.core;

import com.lmax.disruptor.EventFactory;

public class ReactorEventFactory implements EventFactory<ReactorEvent>{
	
	private ReactorEventFactory() {};
	
	public static ReactorEventFactory create() {
		return new ReactorEventFactory();
	}
	
	@Override
	public ReactorEvent newInstance() {
		return new ReactorEvent();
	}

}
