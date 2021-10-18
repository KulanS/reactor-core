package kln.reactor.cachepool;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

public class ReactorCachePool<K,V> implements Runnable{
	
	private boolean isCreated = false;
	private CacheAccess<K, V> cache = null;
	private ReactorCachePoolListner listner;
	private String jcsSwapPath = null;
	
	public void initPool(Class<?> cacheListner, String jcsSwapPath) throws Exception {
		this.jcsSwapPath = jcsSwapPath;
		listner = (ReactorCachePoolListner)cacheListner.newInstance();
		new Thread(this).start();
	}
	
	private void createPool() throws Exception {
		ReactorCachePoolConfig.getProps().put("jcs.auxiliary.DC.attributes.DiskPath",jcsSwapPath);
		JCS.setConfigProperties(ReactorCachePoolConfig.getProps());
		cache = JCS.getInstance("default");
		IElementAttributes attributes = cache.getDefaultElementAttributes();
		attributes.addElementEventHandler(listner);
	    cache.setDefaultElementAttributes(attributes);
	    isCreated = true;
	}
	
	public void addToCachePool(K key, V value) throws Exception {
		cache.put(key, value);
	}
	
	public boolean isKeyExists(K key) throws Exception {
		return CompositeCacheManager.getInstance().getCache("default").getMemoryCache().getKeySet().contains(key);
	}
	
	public V removeFromCachePool(K key) throws Exception {
		V value = cache.get(key);
		cache.remove(key);
		return value;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				if(!isCreated) {
					createPool();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
