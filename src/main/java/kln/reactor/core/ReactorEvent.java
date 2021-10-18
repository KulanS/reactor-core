package kln.reactor.core;

public class ReactorEvent implements ReactorEventInterface{
	
	private long sequence;
	private Object payload;
	
	@Override
	public void clearData() throws Exception {
		payload = null;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}
	
}
