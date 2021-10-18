package kln.reactor.cachepool;

import java.util.Properties;

public class ReactorCachePoolConfig {
	
	
static Properties props = new Properties();
	
	public static Properties getProps() {
		
		//DEFAULT CACHE REGION
		props.put("jcs.default","DC");
		props.put("jcs.default.cacheattributes","org.apache.commons.jcs.engine.CompositeCacheAttributes");
		props.put("jcs.default.cacheattributes.MaxObjects","2000");
		props.put("jcs.default.cacheattributes.MemoryCacheName","org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache");
		props.put("jcs.default.cacheattributes.UseMemoryShrinker","true");
		props.put("jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds","10");
		props.put("jcs.default.cacheattributes.ShrinkerIntervalSeconds","10");
		props.put("jcs.default.cacheattributes.DiskUsagePatternName","UPDATE");
		props.put("jcs.default.elementattributes","org.apache.commons.jcs.engine.ElementAttributes");
		props.put("jcs.default.elementattributes.IsEternal","false");
		props.put("jcs.default.elementattributes.MaxLife","10");
		props.put("jcs.default.elementattributes.IdleTime","10");
		props.put("jcs.default.elementattributes.IsSpool","true");
		props.put("jcs.default.elementattributes.IsRemote","true");
		props.put("jcs.default.elementattributes.IsLateral","true");

		//PRE-DEFINED CACHE REGIONS
		props.put("jcs.region.testCache1","DC");
		props.put("jcs.region.testCache1.cacheattributes","org.apache.commons.jcs.engine.CompositeCacheAttributes");
		props.put("jcs.region.testCache1.cacheattributes.MaxObjects","1000");
		props.put("jcs.region.testCache1.cacheattributes.MemoryCacheName","org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache");
		props.put("jcs.region.testCache1.cacheattributes.UseMemoryShrinker","true");
		props.put("jcs.region.testCache1.cacheattributes.MaxMemoryIdleTimeSeconds","10");
		props.put("jcs.region.testCache1.cacheattributes.ShrinkerIntervalSeconds","10");
		props.put("jcs.region.testCache1.cacheattributes.MaxSpoolPerRun","100");
		props.put("jcs.region.testCache1.elementattributes","org.apache.commons.jcs.engine.ElementAttributes");
		props.put("jcs.region.testCache1.elementattributes.IsEternal","false");

		//AVAILABLE AUXILIARY CACHES
		props.put("jcs.auxiliary.DC","org.apache.commons.jcs.auxiliary.disk.indexed.IndexedDiskCacheFactory");
		props.put("jcs.auxiliary.DC.attributes","org.apache.commons.jcs.auxiliary.disk.indexed.IndexedDiskCacheAttributes");
		//props.put("jcs.auxiliary.DC.attributes.DiskPath","${user.dir}/jcs_swap");
		props.put("jcs.auxiliary.DC.attributes.MaxPurgatorySize","10000000");
		props.put("jcs.auxiliary.DC.attributes.MaxKeySize","1000000");
		props.put("jcs.auxiliary.DC.attributes.OptimizeAtRemoveCount","300000");
		props.put("jcs.auxiliary.DC.attributes.ShutdownSpoolTimeLimit","60");
		
		props.put("log4j.logger.org.library","error");
		//props.put("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
		//props.put("org.apache.commons.logging.Log","org.apache.commons.logging.impl.NoOpLog");
		
		return props;
	}
}
