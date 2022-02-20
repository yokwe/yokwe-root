package yokwe.util;

import java.util.stream.Collectors;

public class StackUtil {
	public static Class<?> getCallerClass() {
		return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
	}
	
	public static StackWalker.StackFrame getCallerStackFrame() {
		var list = StackWalker.getInstance().walk(o -> o.collect(Collectors.toList()));
		return list.get(1);
	}
	
	public static String getCallerMethodName() {
		return getCallerStackFrame().getMethodName();
	}
}
