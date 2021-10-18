package kln.reactor.channel;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import kln.reactor.channel.tcp.ReactorTCPClientChannelListner;
import kln.reactor.channel.tcp.ReactorTCPServerChannelListner;
import kln.reactor.session.NetSession;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public abstract class ReactorChannel {
	
	protected int channelId;
	protected String channelName;
	protected int masterThreads;
	protected int slaveThreads;
	protected int port;
	protected String ip;
	protected boolean enableChannelLog;
	protected Class<?> listnerClass;
	protected EventLoopGroup masterGroup;
	protected EventLoopGroup slaveGroup;
	protected Channel channel;
	protected ChannelFuture channelFuture;
	protected ServerBootstrap serverBootstrap;
	protected Bootstrap clientBootstrap;
	protected ChannelHandler channelHandler;
	protected OS_TYPE os;
	public enum OS_TYPE {WINDOWS, LINUX}
	//Cache parameters
	protected int initCacheSize;
	protected int maxCacheSize;
	protected int cacheConcurrencyLevel;
	protected int cacheExpireTime;
	protected TimeUnit cacheExpireTimeUnit;
	protected Cache<String, NetSession> channelCache = null;
	protected RemovalListener<String, NetSession> removalListner;
	protected ReactorTCPClientChannelListner reactorTCPClientChannelListner;
	protected ReactorTCPServerChannelListner reactorTCPServerChannelListner;
	//Console report
	protected boolean enableMetrics;
	protected int metricsIntervalSeconds;
	protected String metricsFilePath;
	
	public static final MetricRegistry metrics = new MetricRegistry();
	public static final Meter requests = metrics.meter("Messages");
	
	protected void buildPerformanceMonitor() throws Exception {
		metrics.register("cache eviction count on channel " + channelName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	return channelCache.stats().evictionCount();
		    }
		});
		metrics.register("cache hit count on channel "+ channelName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	return channelCache.stats().hitCount();
		    }
		});
		metrics.register("cache load count on channel "+ channelName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	return channelCache.stats().loadCount();
		    }
		});
		metrics.register("cache load exception count on channel "+ channelName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	return channelCache.stats().loadExceptionCount();
		    }
		});
		metrics.register("cache load success count on channel "+ channelName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	return channelCache.stats().loadSuccessCount();
		    }
		});
		metrics.register("cache miss count on channel "+ channelName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	return channelCache.stats().missCount();
		    }
		});
		metrics.register("cache request count on channel "+ channelName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	return channelCache.stats().requestCount();
		    }
		});
		metrics.register("cache total load time on channel "+ channelName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	return channelCache.stats().totalLoadTime();
		    }
		});
		metrics.register("cache avarage load panelty on channel "+ channelName, new Gauge<Double>() {
		    @Override
		    public Double getValue() {
		    	return channelCache.stats().averageLoadPenalty();
		    }
		});
		metrics.register("cache hit rate on channel "+ channelName, new Gauge<Double>() {
		    @Override
		    public Double getValue() {
		    	return channelCache.stats().hitRate();
		    }
		});
		metrics.register("cache load exception rate on channel "+ channelName, new Gauge<Double>() {
		    @Override
		    public Double getValue() {
		    	return channelCache.stats().loadExceptionRate();
		    }
		});
		metrics.register("cache miss rate on channel "+ channelName, new Gauge<Double>() {
		    @Override
		    public Double getValue() {
		    	return channelCache.stats().missRate();
		    }
		});
		if(enableMetrics) {
			ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
				       .convertRatesTo(TimeUnit.SECONDS)
				       .convertDurationsTo(TimeUnit.MILLISECONDS)
				       .outputTo(new PrintStream(new FileOutputStream(metricsFilePath)))
				       .build();
				   reporter.start(metricsIntervalSeconds, TimeUnit.SECONDS);
		}
	}
	
	
	protected void buildChannelCache() throws Exception {
		channelCache = CacheBuilder.newBuilder()
				.recordStats()
				.initialCapacity(initCacheSize) 							//Setting the initial capacity of the cache container
				.maximumSize(maxCacheSize)									// maximumSize Sets Cache Size
				.concurrencyLevel(cacheConcurrencyLevel)					//Set the concurrency level, which refers to the number of threads that can write caches at the same time.
				.expireAfterAccess(cacheExpireTime, cacheExpireTimeUnit)	// expireAfterWrite expires 8 seconds after the write cache is set
				.removalListener(removalListner)							//Setting Cache Removal Notification
				.recordStats()
				.build();
		
		Runnable cleanUp = new Runnable() {
			
			@Override
			public void run() {
				//System.out.println(channelCache.stats().toString());
				channelCache.cleanUp();
			}
		};
		masterGroup.scheduleAtFixedRate(cleanUp, 0, cacheExpireTime, cacheExpireTimeUnit);
	}
	
	protected void createChannel(Class<?> bootstrapClass) throws Exception {
		Object o = bootstrapClass.newInstance();
		if(o instanceof ServerBootstrap) {
			serverBootstrap = (ServerBootstrap)o;
		}else if(o instanceof Bootstrap) {
			clientBootstrap = (Bootstrap)o;
		}
		if(os == OS_TYPE.LINUX) {
			masterGroup = new EpollEventLoopGroup(masterThreads);
			slaveGroup = new EpollEventLoopGroup(slaveThreads);
		}else {
			masterGroup = new NioEventLoopGroup(masterThreads);
			slaveGroup = new NioEventLoopGroup(slaveThreads);
		}
		if(o instanceof ServerBootstrap) {
			serverBootstrap = (ServerBootstrap)o;
			serverBootstrap.group(masterGroup, slaveGroup);
		}else if(o instanceof Bootstrap){
			clientBootstrap = (Bootstrap)o;
			clientBootstrap.group(masterGroup);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void setChannelListner(Class<?> channelListnerClass) throws Exception {
		Object o = channelListnerClass.newInstance();
		if(o instanceof ReactorTCPClientChannelListner) {
			reactorTCPClientChannelListner = (ReactorTCPClientChannelListner) o;
			reactorTCPClientChannelListner.reactorChannel = this;
		}else if(o instanceof ReactorTCPServerChannelListner) {
			reactorTCPServerChannelListner = (ReactorTCPServerChannelListner)o;
			reactorTCPServerChannelListner.reactorChannel = this;
		}
		channelHandler = (ChannelHandler) o;
		removalListner = (RemovalListener<String, NetSession>) o;
	} 
	
	protected void shutdown() {
		try {
			if(channel != null) {
				channel.closeFuture().sync();
				channel.close();
			}
			if(masterGroup != null) {
				masterGroup.shutdownGracefully();
			}
			if(slaveGroup != null) {
				slaveGroup.shutdownGracefully();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Cache<String, NetSession> getChannelCache(){
		return channelCache;
	}
	
	//public abstract boolean send(byte[] bytes) throws Exception;
	
	public abstract void openChannel() throws Exception;
	
	public abstract void closeChannel() throws Exception;
	
	protected abstract void setChannelOptions() throws Exception;
	
	protected abstract void setChannelCacheToListner() throws Exception;
}
