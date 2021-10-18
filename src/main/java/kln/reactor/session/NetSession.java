package kln.reactor.session;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import kln.reactor.channel.ReactorChannel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.AsciiString;

public class NetSession implements AutoCloseable, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4259271033962966208L;
	
	private ReactorChannel reactorChannel;
	public enum NestSessionStatus {NET_SESSION_TIME_OUT, NET_SESSION_COMPLETED}
	public enum NestSessionDirection {NET_SESSION_OUTWARD, NET_SESSION_INWARD}
	private int channelId;
	private String channelName;
	private String netSessionId;
	private byte[] outBuffer;
	private byte[] inBuffer;
	private long outTime;
	private long inTime;
	private Object outMessage;
	private Object inMessage;
	private NestSessionStatus netSessionSatus;
	private NestSessionDirection netSessionDirection;
	private ChannelHandlerContext context;
	public Cache<String, NetSession> channelCache;
	private HttpRequest httpRequest;
	private FullHttpResponse httpResponse;
	private HashMap<String, String> httpRequestHeaders;
	private HashMap<String, String> httpRequestTailers;
	private HashMap<String, String> httpResponseHeaders;
	private HashMap<String, String> httpResponseTailers;
	private String httpContent;
	private int retryCount;
	private int messageCode;
	
	public NetSession() {}
	
	public NetSession(Cache<String, NetSession> channelCache, ChannelHandlerContext context, Object object) {
		try {
			this.channelCache = channelCache;
			ByteBuf byteBuf = (ByteBuf) object;
			this.context = context;
			inBuffer = extractByteArrayFromByteBuf(byteBuf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public NetSession(ChannelHandlerContext context, Object object) {
		try {
			ByteBuf byteBuf = (ByteBuf) object;
			this.context = context;
			inBuffer = extractByteArrayFromByteBuf(byteBuf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public NetSession(ReactorChannel reactorChannel, ChannelHandlerContext context, Object object) {
		try {
			this.setReactorChannel(reactorChannel);
			ByteBuf byteBuf = (ByteBuf) object;
			this.context = context;
			inBuffer = extractByteArrayFromByteBuf(byteBuf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public NetSession(Cache<String, NetSession> channelCache) {
			this.channelCache = channelCache;
	}
	
	public NetSession(Cache<String, NetSession> channelCache, ChannelHandlerContext context) {
		this.channelCache = channelCache;
		this.context = context;
	}
	
	@Override
	public void close() throws Exception {
		if(context != null) {context.close();context = null;}
		if(channelCache != null) {channelCache= null;}
		if(httpRequestHeaders != null) {httpRequestHeaders.clear();httpRequestHeaders = null;}
		if(httpRequestTailers != null) {httpRequestTailers.clear();httpRequestTailers = null;}
		if(httpResponseHeaders != null) {httpResponseHeaders.clear();httpResponseHeaders = null;}
		if(httpResponseTailers != null) {httpResponseTailers.clear();httpResponseTailers = null;}
		if(httpRequest != null) {httpRequest = null;}
		outBuffer   = null;
		inBuffer    = null;
		outMessage  = null;
		inMessage   = null;
		channelName = null;
		httpContent = null;
	}
	
	public Map<String, List<String>> getURIParamList() {
		return Arrays.stream(httpRequest.uri().split("&"))
	            .map(this::splitQueryParameter)
	            .collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}
	
	private SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
		SimpleImmutableEntry<String, String> entry = null;
		try {
	    	final int idx = it.indexOf("=");
		    final String key = idx > 0 ? it.substring(0, idx) : it;
		    final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
		   
		    if(key != null & value !=null) {
		    	entry = new SimpleImmutableEntry<>(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
		    }else if(value == null) {
		    	entry = new SimpleImmutableEntry<>(URLDecoder.decode(key, "UTF-8"), null);
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entry;
	}
	
	public byte[] getOutBuffer() {
		return outBuffer;
	}

	public void setOutBuffer(byte[] outBuffer) {
		this.outBuffer = outBuffer;
	}

	public byte[] getInBuffer() {
		return inBuffer;
	}

	public void setInBuffer(byte[] inBuffer) {
		this.inBuffer = inBuffer;
	}

	public long getOutTime() {
		return outTime;
	}

	public void setOutTime(long outTime) {
		this.outTime = outTime;
	}

	public long getInTime() {
		return inTime;
	}

	public void setInTime(long inTime) {
		this.inTime = inTime;
	}

	public Object getOutMessage() {
		return outMessage;
	}

	public void setOutMessage(Object outMessage) {
		this.outMessage = outMessage;
	}

	public Object getInMessage() {
		return inMessage;
	}

	public void setInMessage(Object inMessage) {
		this.inMessage = inMessage;
	}

	public NestSessionStatus getNetSessionSatus() {
		return netSessionSatus;
	}

	public void setNetSessionSatus(NestSessionStatus netSessionSatus) {
		this.netSessionSatus = netSessionSatus;
	}

	public String getNetSessionId() {
		return netSessionId;
	}

	public void setNetSessionId(String netSessionId) {
		this.netSessionId = netSessionId;
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public NestSessionDirection getNetSessionDirection() {
		return netSessionDirection;
	}

	public void setNetSessionDirection(NestSessionDirection netSessionDirection) {
		this.netSessionDirection = netSessionDirection;
	}
	
	public long getNetSessionCompletionTime() {
		if(NestSessionStatus.NET_SESSION_COMPLETED.equals(netSessionSatus)) {
			if(NestSessionDirection.NET_SESSION_OUTWARD.equals(netSessionDirection)) {
				return inTime - outTime;
			}else {
				return outTime - inTime;
			}
		}else {
			return -1;
		}
	}
	
	public void enqueueNetSession() throws Exception {
		this.channelCache.put(netSessionId, this);
	}
	
	public void dequeueNetSession() throws Exception {
		this.channelCache.invalidate(netSessionId);
	}
	
	private byte[] extractByteArrayFromByteBuf(ByteBuf buf) throws Exception{
		byte[] bytes;
		//int offset = 0;
		int length = buf.readableBytes();
		if (buf.hasArray()) {
		    bytes = buf.array();
		    //offset = buf.arrayOffset();
		} else {
		    bytes = new byte[length];
		    buf.getBytes(buf.readerIndex(), bytes);
		    //offset = 0;
		}
		return bytes;
	}
	
	public ChannelHandlerContext getContext() {
		return context;
	}

	public void setContext(ChannelHandlerContext context) {
		this.context = context;
	}

	public String getHttpContent() {
		return httpContent;
	}

	public void setHttpContent(String httpContent) {
		this.httpContent = httpContent;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}
	
	public boolean sendHttpResponse(int httpStatusCode, AsciiString contentType, byte[] responseBody) throws Exception {
		boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
		setHttpResponse(new DefaultFullHttpResponse(
                HTTP_1_1,
                HttpResponseStatus.valueOf(httpStatusCode),
                Unpooled.wrappedBuffer(responseBody)));
		getHttpResponse().headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
		Iterator<Entry<String, String>> i = null;
		Map.Entry<String, String> pair    = null;
		if(httpResponseHeaders != null && !httpResponseHeaders.isEmpty()) {
			i = httpResponseHeaders.entrySet().iterator();
			while(i.hasNext()) {
				pair = (Map.Entry<String, String>)i.next();
				getHttpResponse().headers().set(pair.getKey(), pair.getValue());
			}
		}
		if(httpResponseTailers != null && !httpResponseTailers.isEmpty()) {
			i = httpResponseTailers.entrySet().iterator();
			while(i.hasNext()) {
				pair = (Map.Entry<String, String>)i.next();
				getHttpResponse().trailingHeaders().set(pair.getKey(), pair.getValue());
			}
		}
		i    = null;
		pair = null;
		if (keepAlive) {
			getHttpResponse().headers().set(HttpHeaderNames.CONTENT_LENGTH, getHttpResponse().content().readableBytes());
			getHttpResponse().headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
		// Encode the cookie.
        String cookieString = httpRequest.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie: cookies) {
                	getHttpResponse().headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookie));
                }
            }
        } else {
            // Browser sent no cookie.  Add some.
        	getHttpResponse().headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode("key1", "value1"));
        	getHttpResponse().headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode("key2", "value2"));
        }
     // Write the response.
        context.write(getHttpResponse());
        context.flush();
		return keepAlive;
	}
	
	public void sendTcpResponse(byte[] response) throws Exception {
		context.writeAndFlush(Unpooled.wrappedBuffer(response));
		
	}
	
	public HashMap<String, String> getHttpRequestHeaders() {
		return httpRequestHeaders;
	}

	public void setHttpRequestHeaders(HashMap<String, String> httpRequestHeaders) {
		this.httpRequestHeaders = httpRequestHeaders;
	}

	public HashMap<String, String> getHttpRequestTailers() {
		return httpRequestTailers;
	}

	public void setHttpRequestTailers(HashMap<String, String> httpRequestTailers) {
		this.httpRequestTailers = httpRequestTailers;
	}

	public HashMap<String, String> getHttpResponseHeaders() {
		return httpResponseHeaders;
	}

	public void setHttpResponseHeaders(HashMap<String, String> httpResponseHeaders) {
		this.httpResponseHeaders = httpResponseHeaders;
	}

	public HashMap<String, String> getHttpResponseTailers() {
		return httpResponseTailers;
	}

	public void setHttpResponseTailers(HashMap<String, String> httpResponseTailers) {
		this.httpResponseTailers = httpResponseTailers;
	}

	public FullHttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(FullHttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public ReactorChannel getReactorChannel() {
		return reactorChannel;
	}

	public void setReactorChannel(ReactorChannel reactorChannel) {
		this.reactorChannel = reactorChannel;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public int getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(int messageCode) {
		this.messageCode = messageCode;
	}
}
