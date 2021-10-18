package kln.reactor.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static java.lang.System.out;

public class Log {
	
	public static boolean ENABLE_TIMESTAMP = true;
	public static boolean ENABLE_DB_POOL   = true;
	
	public static String timestampFormat = "yyyy-MM-dd HH:mm:ss";
	
	private static final String nullPattern = "%032d[%-9s]:%s%n";
	private static final String nullTimePattern = "%s|%032d[%-9s]:%s%n";
	private static final String notNullPattern = "%s[%-9s]:%s%n";
	private static final String notNullTimePattern = "%s|%s[%-9s]:%s%n";
	
	public static void debug(String sessionId, String msg) {
		if(sessionId == null) {
			if(ENABLE_TIMESTAMP) {
				out.printf(nullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), 0,"DEBUG", msg);
			}else {
				System.out.printf(nullPattern, 0,"DEBUG", msg);
			}
			
		}else {
			if(ENABLE_TIMESTAMP) {
				out.printf(notNullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), String.format("%32s", sessionId).replace(" ", "0"),"DEBUG", msg);
			}else {
				out.printf(notNullPattern, String.format("%32s", sessionId).replace(" ", "0"),"DEBUG", msg);
			}
			
		}
	}
	
	public static void dbPool(String sessionId, String msg) {
		if(sessionId == null) {
			if(ENABLE_TIMESTAMP) {
				out.printf(nullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), 0,"DBPOOL", msg);
			}else {
				out.printf(nullPattern, 0,"DBPOOL", msg);
			}
			
		}else {
			if(ENABLE_TIMESTAMP) {
				out.printf(notNullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), String.format("%32s", sessionId).replace(" ", "0"),"DBPOOL", msg);
			}else {
				out.printf(notNullPattern, String.format("%32s", sessionId).replace(" ", "0"),"DBPOOL", msg);
			}
			
		}
	}
	
	public static void exception(String sessionId, String msg) {
		if(sessionId == null) {
			if(ENABLE_TIMESTAMP) {
				out.printf(nullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), 0,"EXCEPTION", msg);
			}else {
				out.printf(nullPattern, 0,"EXCEPTION", msg);
			}
			
		}else {
			if(ENABLE_TIMESTAMP) {
				out.printf(notNullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), String.format("%32s", sessionId).replace(" ", "0"),"EXCEPTION", msg);
			}else {
				out.printf(notNullPattern, String.format("%32s", sessionId).replace(" ", "0"),"EXCEPTION", msg);
			}
			
		}
	}
	
	public static void trace(String sessionId, String msg) {
		if(sessionId == null) {
			if(ENABLE_TIMESTAMP) {
				out.printf(nullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), 0,"TRACE", msg);
			}else {
				out.printf(nullPattern, 0,"TRACE", msg);
			}
			
		}else {
			if(ENABLE_TIMESTAMP) {
				out.printf(notNullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), String.format("%32s", sessionId).replace(" ", "0"),"TRACE", msg);
			}else {
				out.printf(notNullPattern, String.format("%32s", sessionId).replace(" ", "0"),"TRACE", msg);
			}
			
		}
	}
	
	public static void alert(String sessionId, String msg) {
		if(sessionId == null) {
			if(ENABLE_TIMESTAMP) {
				out.printf(nullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), 0,"ALERT", msg);
			}else {
				out.printf(nullPattern, 0,"ALERT", msg);
			}
			
		}else {
			if(ENABLE_TIMESTAMP) {
				out.printf(notNullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), String.format("%32s", sessionId).replace(" ", "0"),"ALERT", msg);
			}else {
				out.printf(notNullPattern, String.format("%32s", sessionId).replace(" ", "0"),"ALERT", msg);
			}
			
		}
	}
	
	public static void warn(String sessionId, String msg) {
		if(sessionId == null) {
			if(ENABLE_TIMESTAMP) {
				out.printf(nullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), 0,"WARN", msg);
			}else {
				out.printf(nullPattern, 0,"WARN", msg);
			}
			
		}else {
			if(ENABLE_TIMESTAMP) {
				out.printf(notNullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), String.format("%32s", sessionId).replace(" ", "0"),"WARN", msg);
			}else {
				out.printf(notNullPattern, String.format("%32s", sessionId).replace(" ", "0"),"WARN", msg);
			}
			
		}
	}
	
	public static void info(String sessionId, String msg) {
		if(sessionId == null) {
			if(ENABLE_TIMESTAMP) {
				out.printf(nullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), 0,"INFO", msg);
			}else {
				out.printf(nullPattern, 0,"INFO", msg);
			}
			
		}else {
			if(ENABLE_TIMESTAMP) {
				out.printf(notNullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), String.format("%32s", sessionId).replace(" ", "0"),"INFO", msg);
			}else {
				out.printf(notNullPattern, String.format("%32s", sessionId).replace(" ", "0"),"INFO", msg);
			}
			
		}
	}
	
	public static void error(String sessionId, String msg) {
		if(sessionId == null) {
			if(ENABLE_TIMESTAMP) {
				out.printf(nullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), 0,"ERROR", msg);
			}else {
				out.printf(nullPattern, 0,"ERROR", msg);
			}
			
		}else {
			if(ENABLE_TIMESTAMP) {
				out.printf(notNullTimePattern,DateTimeFormatter.ofPattern(timestampFormat).format(LocalDateTime.now()), String.format("%32s", sessionId).replace(" ", "0"),"ERROR", msg);
			}else {
				out.printf(notNullPattern, String.format("%32s", sessionId).replace(" ", "0"),"ERROR", msg);
			}
			
		}
	}
}
