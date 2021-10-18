package kln.reactor.core;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class ReactorThreadFactory implements ThreadFactory{
	
	long stackSize;
    String pattern;
    ClassLoader ccl;
    ThreadGroup group;
    int priority;
    UncaughtExceptionHandler exceptionHandler;
    boolean daemon;
    private boolean configured;
   //if acc is present wrap or keep it
    private boolean wrapRunnable;
    protected final AccessControlContext acc;
    //thread creation counter
    protected final AtomicLong counter = new AtomicLong();
	
    public ReactorThreadFactory () {
    	final Thread t = Thread.currentThread();
        ClassLoader loader;
        AccessControlContext acc = null;
    	try {
    		loader =  t.getContextClassLoader();
            if (System.getSecurityManager()!=null){
                acc = AccessController.getContext();//keep current permissions             
                acc.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
		}catch (SecurityException _skip) {
			loader =null;
	        acc = null;
		}
    	this.ccl = loader;
        this.acc = acc;
        this.priority = t.getPriority();    
        this.daemon = true;//Executors have it false by default

        this.wrapRunnable = true;//by default wrap if acc is present (+SecurityManager)

        //default pattern - caller className
        StackTraceElement[] stack =  new Exception().getStackTrace();    
        pattern(stack.length>1?getOuterClassName(stack[1].getClassName()):"ReactorThreadFactory", true);     
    }
    
    public ReactorThreadFactory build(){
        configured = true;
        counter.addAndGet(0);//write fence "w/o" volatile
        return this;
    }
    
    public long getCreatedThreadsCount(){
        return counter.get();
    }
    
    private void applyCCL(final Thread t) {
        if (ccl!=null){//use factory creator ACC for setContextClassLoader
            AccessController.doPrivileged(new PrivilegedAction<Object>(){
                @Override
                public Object run() {
                    t.setContextClassLoader(ccl);
                    return null;
                }                               
            }, acc);        
        }
    }
    
    private Runnable wrapRunnable(final Runnable r){
        if (acc==null || !wrapRunnable){
            return r;
        }
        Runnable result = new Runnable(){
            public void run(){
                AccessController.doPrivileged(new PrivilegedAction<Object>(){
                    @Override
                    public Object run() {
                        r.run();
                        return null;
                    }                               
                }, acc);
            }
        };
        return result;      
    }
    
    protected String composeName(Runnable r) {
        return String.format(pattern, counter.incrementAndGet(), System.currentTimeMillis());
    }
    
    public ReactorThreadFactory daemon(boolean daemon){
        assertConfigurable();
        this.daemon = daemon;
        return this;
    }
    
    public ReactorThreadFactory priority(int priority){
        assertConfigurable();
        if (priority<Thread.MIN_PRIORITY || priority>Thread.MAX_PRIORITY){//check before actual creation
            throw new IllegalArgumentException("priority: "+priority);
        }
        this.priority = priority;
        return this;
    }
    
    public ReactorThreadFactory stackSize(long stackSize){
        assertConfigurable();
        this.stackSize = stackSize;
        return this;
    }
    
    public ReactorThreadFactory threadGroup(ThreadGroup group){
        assertConfigurable();
        this.group= group;
        return this;        
    }
    
    public ReactorThreadFactory exceptionHandler(UncaughtExceptionHandler exceptionHandler){
        assertConfigurable();
        this.exceptionHandler= exceptionHandler;
        return this;                
    }
    
    public ReactorThreadFactory wrapRunnable(boolean wrapRunnable){
        assertConfigurable();
        this.wrapRunnable= wrapRunnable;
        return this;                        
    }

    public ReactorThreadFactory ccl(ClassLoader ccl){
        assertConfigurable();
        this.ccl = ccl;
        return this;
    }
    
    private static String getOuterClassName(String className){
        int idx = className.lastIndexOf('.')+1;
        className = className.substring(idx);//remove package
        idx = className.indexOf('$');
        if (idx <= 0){
            return className;//handle classes starting w/ $
        }       
        return className.substring(0,idx);//assume inner class

    }
    
  //standard setters allowing chaining, feel free to add normal setXXX    
    public ReactorThreadFactory pattern(String patten, boolean appendFormat){
        assertConfigurable();
        if (appendFormat){
            //patten+=":%03d @ %tF %<tT";//counter + creation time
           patten+="%03d";
        	//patten+=":%03d %<tT";
        }
        this.pattern = patten;
        return this;
    }
    
    protected void assertConfigurable(){
        if (configured)
            throw new IllegalStateException("already configured");
    }
    
	@Override
	public Thread newThread(Runnable r) {
		configured = true;
        final Thread t = new Thread(group, wrapRunnable(r), composeName(r), stackSize);
        t.setPriority(priority);
        t.setDaemon(daemon);
        t.setUncaughtExceptionHandler(exceptionHandler);//securityException only if in the main group, shall be safe here
        //funny moment Thread.getUncaughtExceptionHandler() has a race.. badz (can throw NPE)
        applyCCL(t);
        return t;
	}

}
