package kln.reactor.channel.tcp;
import com.google.common.cache.Cache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import kln.reactor.channel.ReactorChannel;
import kln.reactor.session.NetSession;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public abstract class ReactorTCPClientChannelListner 
extends SimpleChannelInboundHandler<Object>
implements RemovalListener<String, NetSession>
{
	public Cache<String, NetSession> channelCache = null;
	
	public ReactorChannel reactorChannel = null;
	
	@Override
	public void onRemoval(RemovalNotification<String, NetSession> notification) {
    	
    	if(RemovalCause.EXPIRED == notification.getCause()) {
    		onExpireRemoval(notification.getKey(), notification.getValue());
    	}
    	if(RemovalCause.COLLECTED == notification.getCause()) {
    		onAutoRemoval(notification.getKey(), notification.getValue());
    	}
    	if(RemovalCause.EXPLICIT == notification.getCause()) {
    		onManualRemoval(notification.getKey(), notification.getValue());
    	}
    	if(RemovalCause.REPLACED == notification.getCause()) {
    		onUpdate(notification.getKey(), notification.getValue());
    	}
    	if(RemovalCause.SIZE == notification.getCause()) {
    		onMaxCapacity(notification.getKey(), notification.getValue());
    	}
    	
    }
	
    //The entry's expiration timestamp has passed.
    public abstract void onExpireRemoval(String key, NetSession netSession);
    //The entry was removed automatically because its key or value was garbage-collected.
    public abstract void onAutoRemoval(String key, NetSession netSession);
    //The entry was manually removed by the user.
    public abstract void onManualRemoval(String key, NetSession netSession);
    //The entry itself was not actually removed, but its value was replaced by the user.
    public abstract void onUpdate(String key, NetSession netSession);
    //The entry was evicted due to size constraints.
    public abstract void onMaxCapacity(String key, NetSession netSession); 
    
	public abstract void channelReadHttpRequest(ChannelHandlerContext ctx, NetSession msg) throws Exception;
}
