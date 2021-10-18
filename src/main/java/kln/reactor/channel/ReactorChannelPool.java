package kln.reactor.channel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kln.reactor.channel.tcp.ReactorTCPClientChannel;
import kln.reactor.channel.tcp.ReactorTCPServerChannel;

public class ReactorChannelPool implements Serializable{

	private static final long serialVersionUID = 1090896381996064935L;
	
	static ExecutorService executorService = Executors.newSingleThreadExecutor();
	
	private static LinkedList<ReactorChannel> channels = new LinkedList<ReactorChannel>();
	
	public interface BroadcastListener {
        void receiveBroadcast(Object message);
    }
	
	public static void register(ReactorChannel channel) {
		channels.add(channel);
    }
	
	public static void unregister(ReactorChannel channel) {
		channels.remove(channel);
    }
	
	public static synchronized void broadcast(final Object message) {
        for (final ReactorChannel channel: channels)
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                	if(channel instanceof ReactorTCPClientChannel || channel instanceof ReactorTCPServerChannel) {
                		 ((BroadcastListener) channel).receiveBroadcast(message);
                	}
                }
            });
    }
	
}
