package kln.reactor.channel.http;

import java.util.concurrent.TimeUnit;

import kln.reactor.channel.ReactorChannel;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class ReactorHTTPServerChannel extends ReactorChannel{
	
	protected ReactorHTTPServerChannelListner reactorHTTPServerChannelListner;
	
	private static final boolean SSL         = System.getProperty("ssl") != null;
	private SslContext sslCtx                = null;
	private int httpBacklogSize;
	private boolean enableSSL;
	private boolean enableHttpRequestDecoder;
	private boolean enableHttpResponseEncoder;
	private boolean enableHttpObjectAggregator;
	
	private ReactorHTTPServerChannel(Factory factory) {
		
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
		
		this.httpBacklogSize = factory.httpBacklogSize;
		this.enableSSL = factory.enableSSL;
		this.enableHttpRequestDecoder = factory.enableHttpRequestDecoder;
		this.enableHttpResponseEncoder = factory.enableHttpResponseEncoder;
		this.enableHttpObjectAggregator = factory.enableHttpObjectAggregator;
		this.enableMetrics = factory.enableMetrics;
		this.metricsIntervalSeconds = factory.metricsIntervalSeconds;
		this.metricsFilePath = factory.metricsFilePath;
		
		try {
			setChannelListner(listnerClass);
			createChannel(ServerBootstrap.class);
			setChannelOptions();
			buildChannelCache();
			setChannelCacheToListner();
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
		
		private int httpBacklogSize;
		private boolean enableSSL;
		private boolean enableHttpRequestDecoder;
		private boolean enableHttpResponseEncoder;
		private boolean enableHttpObjectAggregator;
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
		
		public Factory setHttpBacklogSize(int value) {
			httpBacklogSize = value;
			return this;
		}
		
		public Factory enableSSL(boolean value) {
			enableSSL = value;
			return this;
		}
		
		public Factory enableHttpRequestDecoder(boolean value) {
			enableHttpRequestDecoder = value;
			return this;
		}
		
		public Factory enableHttpResponseEncoder(boolean value) {
			enableHttpResponseEncoder = value;
			return this;
		}
		
		public Factory enableHttpObjectAggregator(boolean value) {
			enableHttpObjectAggregator = value;
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
		
		public ReactorHTTPServerChannel create() {
			return new ReactorHTTPServerChannel(this);
		}
	}
	
	@Override
	public void openChannel() throws Exception {
		channel = serverBootstrap.bind(ip,port).sync().channel();
		channel.closeFuture().sync();
	}

	@Override
	public void closeChannel() throws Exception {
		channel.disconnect();
	}

	@Override
	protected void setChannelOptions() throws Exception {
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.option(ChannelOption.SO_BACKLOG, httpBacklogSize);
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				ChannelPipeline channelPipeline = socketChannel.pipeline();
				
				if(enableSSL) {
					if (SSL) {
				        SelfSignedCertificate ssc = new SelfSignedCertificate();
				        sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
				        //SslContextBuilder.forServer(new File(certPath), new File(keyPath), null).build();
				    } else {
				        sslCtx = null;
				    }
				}
				if (sslCtx != null) {
					channelPipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
		        }
				if(enableHttpRequestDecoder) {
					channelPipeline.addLast(new HttpRequestDecoder());
				}
				if(enableHttpResponseEncoder) {
					channelPipeline.addLast(new HttpResponseEncoder());
				}
				if(enableHttpObjectAggregator) {
					channelPipeline.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
				}
				if(enableChannelLog) {
					channelPipeline.addLast(new LoggingHandler(LogLevel.INFO));
					channelPipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
					channelPipeline.addLast(new LoggingHandler(LogLevel.WARN));
					channelPipeline.addLast(new LoggingHandler(LogLevel.TRACE));
				}
				channelPipeline.addLast(channelHandler);
			}
			
		});
	}

	@Override
	protected void setChannelCacheToListner() throws Exception {
		reactorHTTPServerChannelListner = (ReactorHTTPServerChannelListner)channelHandler;
		reactorHTTPServerChannelListner.channelCache = this.channelCache;
	}

}
