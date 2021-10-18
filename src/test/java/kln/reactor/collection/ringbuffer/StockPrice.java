package kln.reactor.collection.ringbuffer;

public class StockPrice {
	
	public final String symbol;
    public final int price;

    public StockPrice(String symbol, int price) {
        this.symbol = symbol;
        this.price = price;
    }
    
    public String getSymbol() {
    	return symbol;
    }
    
    public int getPrice() {
    	return price;
    }
    
    @Override
    public String toString() {
        return "symbol :" + symbol +" count:" + price;
    }
}
