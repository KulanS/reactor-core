package kln.reactor.collection.ringbuffer;

import java.util.concurrent.BlockingQueue;


public class ArrayBlockingCounsumer implements Runnable{
	
	BlockingQueue<StockPrice> queue;
	
	public ArrayBlockingCounsumer(BlockingQueue<StockPrice> queue) {
		this.queue = queue;
	}
	
	
	@Override
	public void run() {
		try {
			while (true) {
				if (!queue.isEmpty()) {
					StockPrice price = queue.poll();

					System.out.println("C1:->" + price);
				}else {
					Thread.sleep(1);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
