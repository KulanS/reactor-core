package kln.reactor.collection.ringbuffer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;

import kln.reactor.collection.coalescing.CoalescingBuffer;
import kln.reactor.collection.coalescing.CoalescingRingBuffer;

public class TestMain {
	
	public static int getRand(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
	
	public static void main(String[] args) {
		CoalescingBuffer<String, StockPrice> buffer = new CoalescingRingBuffer<String, StockPrice>(16);
//		ArrayBlockingQueue<StockPrice> buffer = new ArrayBlockingQueue<>(1000);
//		BlockingQueue<StockPrice> buffer = new DisruptorBlockingQueue<>(1000);
		
		Consumer con = new Consumer(buffer);
//		ArrayBlockingCounsumer con = new ArrayBlockingCounsumer(buffer);
		
		Thread t = new Thread(con);
		t.start();
		String symbol = "RHT";
		String symbol2 = "ABC";
		
		int count = 0;
		int load = TestMain.getRand(100000, 10000000);
		int sleep = 0;
		
		try {
			while(true) {
				count++;
				StockPrice stockPrice = new StockPrice(symbol, count);
				StockPrice stockPrice2 = new StockPrice(symbol2, count);
				buffer.offer(symbol, stockPrice);
				buffer.offer(symbol2, stockPrice2);
				//buffer.offer(stockPrice);
				
				if(count == load) {
					
					sleep = TestMain.getRand(1000, 10000);
					System.out.println("sleeping :" + sleep + "count :" + count);
					Thread.sleep(sleep);
					count = 0;
					load = TestMain.getRand(100000, 10000000);
				}
				
				
				
				//System.out.println("buffer capasity :" + buffer.capacity());
//				if(count %1000 == 0) {
//					Thread.sleep(10000);
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
