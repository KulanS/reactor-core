package kln.reactor.channel.tcp;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import kln.reactor.channel.ReactorChannel;
import kln.reactor.channel.ReactorChannelPool.BroadcastListener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ReactorTCPServerChannel extends ReactorChannel implements BroadcastListener{
	
	protected ReactorTCPServerChannelListner reactorTCPServerChannelListner;
	
	private ReactorTCPServerChannel(Factory factory) {
		
		this.channelId = factory.channelId;
		this.channelName = factory.channelName;
		this.masterThreads = factory.masterThreads;
		this.slaveThreads = factory.slaveThreads;
		this.ip = factory.ip;
		this.port = factory.port;
		this.enableChannelLog = factory.enableChannelLog;
		this.listnerClass = factory.listnerClass;
		this.initCacheSize = factory.initCacheSize;
		this.maxCacheSize = factory.maxCacheSize;
		this.cacheConcurrencyLevel = factory.cacheConcurrencyLevel;
		this.cacheExpireTime = factory.cacheExpireTime;
		this.cacheExpireTimeUnit = factory.cacheExpireTimeUnit;
		this.enableMetrics = factory.enableMetrics;
		this.metricsIntervalSeconds = factory.metricsIntervalSeconds;
		this.metricsFilePath = factory.metricsFilePath;
		
		try {
			setChannelListner(listnerClass);
			createChannel(ServerBootstrap.class);
			setChannelOptions();
			buildChannelCache();
			setChannelCacheToListner();
			if(enableChannelLog) {
				buildPerformanceMonitor();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class Factory {
		
		protected int channelId;
		protected String channelName;
		protected int masterThreads;
		protected int slaveThreads;
		protected int port;
		protected String ip;
		protected boolean enableChannelLog;
		Class<?> listnerClass;
		protected int initCacheSize;
		protected int maxCacheSize;
		protected int cacheConcurrencyLevel;
		protected int cacheExpireTime;
		protected TimeUnit cacheExpireTimeUnit;
		protected boolean enableMetrics;
		protected int metricsIntervalSeconds;
		protected String metricsFilePath;
		
		public Factory enableMetrics(boolean enable) {
			enableMetrics = enable;
			return this;
		}
		
		public Factory setMetricsIntervalSeconds(int seconds) {
			metricsIntervalSeconds = seconds;
			return this;
		}
		
		public Factory setMetricsFilePath(String path) {
			metricsFilePath = path;
			return this;
		}
		
		public Factory setInitCacheSize(int size) {
			initCacheSize = size;
			return this;
		}
		
		public Factory setCacheConcurrencyLevel(int level) {
			cacheConcurrencyLevel = level;
			return this;
		}
		
		public Factory setCacheExpireTime(int time, TimeUnit timeUnit) {
			cacheExpireTime = time;
			cacheExpireTimeUnit = timeUnit;
			return this;
		}
		
		public Factory setMaxCacheSize(int size) {
			maxCacheSize = size;
			return this;
		}
		
		public Factory setChannelId(int id) {
			channelId = id;
			return this;
		}
		
		public Factory setChannelName(String name) {
			channelName = name;
			return this;
		}
		
		public Factory setMasterThreads(int value) {
			masterThreads = value;
			return this;
		}
		
		public Factory setSlaveThreads(int value) {
			slaveThreads = value;
			return this;
		}
		
		public Factory setPort(int value) {
			port = value;
			return this;
		}
		
		public Factory setIP(String value) {
			ip = value;
			return this;
		}
		
		public Factory setListner(Class<?> clas) {
			listnerClass = clas;
			return this;
		}
		
		public Factory enableChannelLog(boolean value) {
			enableChannelLog = value;
			return this;
		}
		
		public ReactorTCPServerChannel create() {
			return new ReactorTCPServerChannel(this);
		}
	}
	
	public boolean send(byte[] bytes) throws Exception {
		if(channel != null) {
			if(channel.isOpen()) {
				
				//channel.writeAndFlush(channel.alloc().buffer().writeBytes(netSession.getOutBuffer()));
				@SuppressWarnings("unused")
				ChannelFuture future = channel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
				//future.addListener(ChannelFutureListener.CLOSE);
				return true;
			}
			return false;
		}else {
			return false;
		}
		
	}
	
	@Override
	public void openChannel() throws Exception {
		channel = serverBootstrap.bind().sync().channel();
	}

	@Override
	public void closeChannel() throws Exception {
		channel.disconnect();
	}

	@Override
	protected void setChannelOptions() throws Exception {
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.childHandler(channelHandler);
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 5);
		serverBootstrap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
		serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		serverBootstrap.localAddress(new InetSocketAddress(ip, port));
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
		    protected void initChannel(SocketChannel socketChannel) throws Exception {
		    	//socketChannel.pipeline().addLast(new IdleStateHandler(30, 30, 0, TimeUnit.SECONDS));
		    	//socketChannel.pipeline().addLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS));
		    	if(enableChannelLog) {
		    		socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
		    		socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
		    		socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.WARN));
		    		socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
				}
		    	socketChannel.pipeline().addLast(channelHandler);
		    }
		});
	}

	@Override
	protected void setChannelCacheToListner() throws Exception {
		reactorTCPServerChannelListner = (ReactorTCPServerChannelListner)channelHandler;
		reactorTCPServerChannelListner.reactorChannel = this;
		reactorTCPServerChannelListner.channelCache = this.channelCache;
	}

	@Override
	public void receiveBroadcast(Object message) {
		
	}
	
}
