package kln.reactor.channel.http;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import kln.reactor.session.NetSession;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

@Sharable
public abstract class ReactorHTTPServerChannelListner 
extends SimpleChannelInboundHandler<Object>
implements RemovalListener<String, NetSession>
{
	public Cache<String, NetSession> channelCache = null;
	//private HttpRequest request;
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			NetSession session = new NetSession(channelCache, ctx);
			if (msg instanceof HttpRequest) {
	            HttpRequest request = (HttpRequest) msg;
	            session.setHttpRequest(request);
	            if (HttpUtil.is100ContinueExpected(request)) {
	                send100Continue(ctx);
	            }

	            HttpHeaders headers = request.headers();
	            if (!headers.isEmpty()) {
	                HashMap<String, String> hd = new HashMap<>();
	            	for (Map.Entry<String, String> h: headers) {
	                    hd.put(h.getKey(), h.getValue());
	                }
	                session.setHttpRequestHeaders(hd);
	            }
	        }

	        if (msg instanceof HttpContent) {
	            HttpContent httpContent = (HttpContent) msg;

	            ByteBuf content = httpContent.content();
	            if (content.isReadable()) {
	                session.setHttpContent(content.toString(CharsetUtil.UTF_8));
	                //ppendDecoderResult(request);
	            }

	            if (msg instanceof LastHttpContent) {
	                LastHttpContent trailer = (LastHttpContent) msg;
	                if (!trailer.trailingHeaders().isEmpty()) {
	                    HashMap<String, String> ht = new HashMap<>();
	                    for (String name: trailer.trailingHeaders().names()) {
	                        for (String value: trailer.trailingHeaders().getAll(name)) {
	                            ht.put(name, value);
	                        }
	                    }
	                    session.setHttpRequestTailers(ht);
	                }
	            }
	        }
	        channelReadHttpRequest(ctx, session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }
    
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
