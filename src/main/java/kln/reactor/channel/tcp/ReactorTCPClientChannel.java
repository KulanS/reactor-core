package kln.reactor.channel.tcp;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import kln.reactor.channel.ReactorChannel;
import kln.reactor.channel.ReactorChannelPool.BroadcastListener;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class ReactorTCPClientChannel extends ReactorChannel implements BroadcastListener{
	
	protected ReactorTCPClientChannelListner reactorTCPClientChannelListner;
	private long reConnectDelay;
	private long reConnectPeriod;
	private boolean enableAutoReConnect;
	private volatile boolean isConnected;
	
	private ReactorTCPClientChannel(Factory factory) {
		this.channelId = factory.channelId;
		this.channelName = factory.channelName;
		this.masterThreads = factory.masterThreads;
		this.slaveThreads = factory.slaveThreads;
		this.ip = factory.ip;
		this.port = factory.port;
		this.enableChannelLog = factory.enableChannelLog;
		this.listnerClass = factory.listnerClass;
		this.reConnectDelay = factory.reConnectDelay;
		this.reConnectPeriod = factory.reConnectPeriod;
		this.enableAutoReConnect = factory.enableAutoReConnect;
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
			createChannel(Bootstrap.class);
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
		protected long reConnectDelay;
		protected long reConnectPeriod;
		protected boolean enableAutoReConnect;
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
		
		public Factory enableAutoReConnect(boolean value) {
			enableAutoReConnect = value;
			return this;
		}
		
		public Factory setReConnectDelay(long millies) {
			reConnectDelay = millies;
			return this;
		}
		
		public Factory setReConnectPeriod(long millies) {
			reConnectPeriod = millies;
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
		
		public ReactorTCPClientChannel create() {
			return new ReactorTCPClientChannel(this);
		}
	}
	
	private class ListnerOnConnectionClose implements ChannelFutureListener{
		@Override
		public void operationComplete(ChannelFuture channelFuture) throws Exception {
			if(!channelFuture.isSuccess()) {//if is not successful, reconnect
				isConnected = false;
				channelFuture.channel().close();
				if(channelFuture.channel().isActive()) {
					isConnected = true;
				}
				//clientBootstrap.connect(new InetSocketAddress(ip, port)).addListener(this);
			}else {	//good, the connection is ok
				channel = channelFuture.channel();
				isConnected = true;
				addCloseDetectListener(channel);	//add a listener to detect the connection lost
			}
		}
		
		private void addCloseDetectListener(Channel channel) {
			//if the channel connection is lost, the ChannelFutureListener.operationComplete() will be called
			channel.closeFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future )throws Exception {
					channel.pipeline().fireChannelInactive();
					isConnected = false;
					//timerTask.run();
					//connectOnSchedule(reConnectDelay);
				}
			});
		}
	}
	
	private void connectWithListnerOnConnectionClose() throws Exception {
		channelFuture = clientBootstrap.connect(new InetSocketAddress(ip, port));
		channelFuture.addListener(new ListnerOnConnectionClose());
	}
	
	private void connectOnSchedule(long delay, long period) throws Exception {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					if(!isConnected) {
						connectWithListnerOnConnectionClose();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		};
		masterGroup.scheduleAtFixedRate(r, delay, period, TimeUnit.MILLISECONDS);
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
	
	public boolean sendAndReceive(byte[] bytes) throws Exception {
		if(channel != null) {
			if(channel.isOpen()) {
				channel.writeAndFlush(channel.alloc().buffer().writeBytes(bytes));
				return true;
			}
			return false;
		}else {
			return false;
		}
	}
	
	@Override
	public void openChannel() throws Exception {
		setChannelListner(listnerClass);
		createChannel(Bootstrap.class);
		setChannelOptions();
		if(enableAutoReConnect) {
			connectOnSchedule(reConnectDelay, reConnectPeriod);
		}
	}

	@Override
	public void closeChannel() throws Exception {
		channel.close().sync();
	}

	@Override
	protected void setChannelOptions() throws Exception {
		clientBootstrap.channel(NioSocketChannel.class);
		//clientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		clientBootstrap.option(ChannelOption.TCP_NODELAY, true);
		clientBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
//                socketChannel.pipeline().addLast( new StringDecoder() ,new StringEncoder( )  , new LineBasedFrameDecoder(1024), new NettyClientHandler(){
//                
//                });
            	socketChannel.pipeline().addLast(new IdleStateHandler(30, 30, 0, TimeUnit.SECONDS));
            	//socketChannel.pipeline().addLast(new ReadTimeoutHandler(60));
            	socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 0));
            	// int lengthFieldLength, int lengthAdjustment
            	//socketChannel.pipeline().addLast(new LengthFieldPrepender(4, false));//added to organaize frame
            	socketChannel.pipeline().addLast(channelHandler);
            	//socketChannel.pipeline().addLast(new StringDecoder());
            	//socketChannel.pipeline().addLast(new StringEncoder());
            	
            }
        });
	}

	@Override
	protected void setChannelCacheToListner() throws Exception {
		reactorTCPClientChannelListner = (ReactorTCPClientChannelListner)channelHandler;
		reactorTCPClientChannelListner.reactorChannel = this;
		reactorTCPClientChannelListner.channelCache = this.channelCache;
	}

	@Override
	public void receiveBroadcast(Object message) {
		
	}
	
}
