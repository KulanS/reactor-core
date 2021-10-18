package kln.reactor.cachepool;

import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.control.event.ElementEvent;
import org.apache.commons.jcs.engine.control.event.behavior.ElementEventType;
import org.apache.commons.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.commons.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.commons.jcs.engine.control.event.behavior.IElementEventQueue;

public abstract class ReactorCachePoolListner implements IElementEventHandler, IElementEventQueue{
	@Override
	public <T> void handleElementEvent(IElementEvent<T> event) {
		ElementEvent<T> elementEvent = (ElementEvent<T>)event;
		CacheElement<?, ?> element = (CacheElement<?, ?>)elementEvent.getSource();
		
		if(event.getElementEvent() == ElementEventType.EXCEEDED_IDLETIME_BACKGROUND) {
			exceededIdleTimeBackground(element);
		}
		
        if(event.getElementEvent() == ElementEventType.EXCEEDED_IDLETIME_ONREQUEST) {
        	exceededIdleTimeOnRequest(element);
		}
        
        if(event.getElementEvent() == ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND) {
        	exceededIdleTimeBackground(element);
    		
		}
        
        if(event.getElementEvent() == ElementEventType.EXCEEDED_MAXLIFE_ONREQUEST) {
        	exceededMaxLifeOnRequest(element);
    		
		}
        
        if(event.getElementEvent() == ElementEventType.SPOOLED_DISK_AVAILABLE) {
        	spooledDiskAvailable(element);
		}
        
        if(event.getElementEvent() == ElementEventType.SPOOLED_DISK_NOT_AVAILABLE) {
        	spooledDiskNotAvailable(element);
		}
        
        if(event.getElementEvent() == ElementEventType.SPOOLED_NOT_ALLOWED) {
        	spooledDiskNotAllowed(element);
		}
	}
	
	abstract public void exceededIdleTimeOnRequest(CacheElement<?, ?> element);
	abstract public void exceededIdleTimeBackground(CacheElement<?, ?> element);
	abstract public void exceededMaxLifeBackground(CacheElement<?, ?> element);
	abstract public void exceededMaxLifeOnRequest(CacheElement<?, ?> element);
	abstract public void spooledDiskAvailable(CacheElement<?, ?> element);
	abstract public void spooledDiskNotAvailable(CacheElement<?, ?> element);
	abstract public void spooledDiskNotAllowed(CacheElement<?, ?> element);
}
