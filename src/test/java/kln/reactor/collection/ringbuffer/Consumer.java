package kln.reactor.collection.ringbuffer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import kln.reactor.collection.coalescing.CoalescingBuffer;


public class Consumer implements Runnable{
	
	Collection<StockPrice> prices = new ArrayList<StockPrice>();
	
	ArrayBlockingQueue<StockPrice> queue = new ArrayBlockingQueue<>(200000);
	
	CoalescingBuffer<String, StockPrice> buffer;
	
	
	public Consumer(CoalescingBuffer<String, StockPrice> buffer) {
		this.buffer = buffer;
//		Counsumer2 c = new Counsumer2(this.queue);
//		Thread t = new Thread(c);
//		t.start();
	}
	
	
	@Override
	public void run() {
		try {
			
			while (true) {
				if(!buffer.isEmpty()) {
					int p = buffer.poll(prices);
					
					
					//Thread.sleep(100);
					for (StockPrice price : prices) {
						System.out.println("poll:"+p+" size:"+prices.size()+" C1:->"+price + " sym:" + price.getSymbol());
					}

					prices.clear();
				}else {
					Thread.sleep(1);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
