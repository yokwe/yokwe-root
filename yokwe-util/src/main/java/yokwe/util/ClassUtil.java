package yokwe.util;

import java.io.File;
import java.io.IOException;
import java.lang.StackWalker.StackFrame;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class ClassUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class MethodReference {
	    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
	    
	    public static <E> E toMethodReference(Class<E> target, Method method) {
			Method targetMethod;
	    	
	    	// Sanity check
	    	{
	    		if (!target.isInterface()) {
	    			logger.error("target is not interface");
	    			logger.error("  target {}", target.getName());
	    			throw new UnexpectedException("target is not interface");
	    		}
	    		if (!target.isAnnotationPresent(FunctionalInterface.class)) {
	    			logger.error("target is not FunctionalInterface");
	    			logger.error("  target {}", target.getName());
	    			throw new UnexpectedException("target is not FunctionalInterface");
	    		}
	    		{
					Method[] methods = target.getDeclaredMethods();
					if (methods.length == 1) {
						targetMethod = methods[0];
					} else {
		    			logger.error("target has more than one method");
		    			logger.error("  target {}", target.getName());
		    			for(int i = 0; i < methods.length; i++) {
			    			logger.error("  method {} {}", i, methods[i].getName());
		    			}
		    			throw new UnexpectedException("target has more than one method");
					}
	    		}
	    	}
	    	{
	    		int modifiers = method.getModifiers();
		    	if (!Modifier.isStatic(modifiers)) return null;
		    	if (!Modifier.isPublic(modifiers)) return null;
	    	}
	    	
	    	// build metodType
	    	MethodType methodType;
	    	{
	    		Class<?> returnType = targetMethod.getReturnType();
	    		Parameter[] parameters = targetMethod.getParameters();
	    		
	    		switch(parameters.length) {
	    		case 0:
	    			methodType = MethodType.methodType(returnType);
	    			break;
	    		case 1:
	    		{
	    			Class<?> parameterType0 = parameters[0].getClass();
	    			methodType = MethodType.methodType(returnType, parameterType0);
	    		}
	    			break;
	    		default:
	    		{
	    			Class<?> parameterType0 = parameters[0].getClass();
	    			Class<?>[] parameterTypes = new Class<?>[parameters.length - 1];
	    			for(int i = 1; i < parameters.length; i++) {
	    				parameterTypes[i - 1] = parameters[i].getClass();
	    			}
	    			methodType = MethodType.methodType(returnType, parameterType0, parameterTypes);
	    		}
	    			break;
	    		}
	    	}
	    	
	    	// build invokeType
			MethodType invokeType = MethodType.methodType(target);
			try {
				MethodHandle methodHandle = lookup.unreflect(method);
				if (methodHandle.type().equals(methodType)) {
					CallSite callSite = LambdaMetafactory.metafactory(lookup, targetMethod.getName(), invokeType, methodType, methodHandle, methodType);
					E e = (E) callSite.getTarget().invoke();
					return e;
				} else {
					return null;
				}
			} catch (Throwable e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
	    }
	}
	
	// Get method reference Runnable from Method if applicable
    public static Runnable toRunnable(Method method) {
    	return MethodReference.toMethodReference(Runnable.class, method);
    }
    

	// class file enumeration using classLoader
    public static class FindClass {
        private final static char DOT = '.';
        private final static char SLASH = '/';
        private final static String CLASS_SUFFIX = ".class";

        public final static List<Class<?>> find(final String packageName) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final String scannedPath = packageName.replace(DOT, SLASH);
            final Enumeration<URL> resources;
            try {
                resources = classLoader.getResources(scannedPath);
            } catch (IOException e) {
    			String exceptionName = e.getClass().getSimpleName();
    			logger.error("{} {}", exceptionName, e);
    			throw new UnexpectedException(exceptionName, e);
            }
            final List<Class<?>> classes = new LinkedList<Class<?>>();
            while (resources.hasMoreElements()) {
                final File file = new File(resources.nextElement().getFile());
                classes.addAll(findInFile(file, packageName));
            }
            return classes;
        }

        private final static List<Class<?>> findInFile(final File file, final String scannedPackage) {
            final List<Class<?>> classes = new LinkedList<Class<?>>();
            if (file.isDirectory()) {
                for (File nestedFile : file.listFiles()) {
                 	if (nestedFile.isDirectory()) {
                        classes.addAll(findInFile(nestedFile, scannedPackage + DOT + nestedFile.getName()));
                	} else {
                		classes.addAll(findInFile(nestedFile, scannedPackage));
                	}
                }
            } else if (file.getName().endsWith(CLASS_SUFFIX)) {
    	        final String resource = scannedPackage + DOT + file.getName();

    	        final int beginIndex = 0;
                final int endIndex = resource.length() - CLASS_SUFFIX.length();
                final String className = resource.substring(beginIndex, endIndex);
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                	//logger.warn("e = {}", e);
                }
            }
            return classes;
        }
    }
    
    public final static List<Class<?>> findClass(final String packageName) {
    	return FindClass.find(packageName);
    }
    
	public static final int OFFSET_CALLER = 2;
	public static StackFrame getCallerStackFrame(int offset) {
		int size = offset + 1;
		StackFrame[] array = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.limit(size).toArray(StackFrame[]::new));
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		if (array.length != size) {
			logger.error("Unexpected array length");
			logger.error("  size   {}", size);
			logger.error("  array  {}", array.length);
			for(int i = 0; i < array.length; i++) {
				logger.error("  {}  {}", i, array[i].toString());
			}
			throw new UnexpectedException("Unexpected array length");
		}
		
		return array[offset];
	}
	public static StackFrame getCallerStackFrame() {
		// offset 0 -- getCallerStackFrame(offset)
		// offset 1 -- getCallerStackFrame()
		// offset 2 -- caller
		return getCallerStackFrame(OFFSET_CALLER);
	}
	public static String getCallerMethodName() {
		// offset 0 -- getCallerStackFrame(offset)
		// offset 1 -- getCallerStackFrame()
		// offset 2 -- caller
		return getCallerStackFrame(OFFSET_CALLER).getMethodName();
	}
//	public static Class<?> getCallerClass() {
//		//Class<?> callerClass = java.lang.invoke.MethodHandles.lookup().lookupClass();
//		Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
//		return callerClass;
//	}
	public static Class<?> getCallerClass() {
		// offset 0 -- getCallerStackFrame(2)
		// offset 1 -- getCallerStackFrame()
		// offset 2 -- caller
		return getCallerStackFrame(OFFSET_CALLER).getDeclaringClass();
	}

	
	// create instance of clazz using args
	public static class EnumInfo {
		static private Map<String, EnumInfo> map = new TreeMap<>();
		//                 typename

		public static EnumInfo getEnumInfo(Class<?> clazz) {
			String name = clazz.getTypeName();
			if (map.containsKey(name)) {
				return map.get(name);
			} else {
				EnumInfo enumInfo = new EnumInfo(clazz);
				map.put(name, enumInfo);
				return enumInfo;
			}
		}
		
		record Entry (String string, Object value) {}
		//                   result of toString()
		final Entry[] entries;
	
		EnumInfo(Class<?> clazz) {
			if (!clazz.isEnum()) new UnexpectedException("Unexpected");
			Object[] values = clazz.getEnumConstants();
			
			entries = new Entry[values.length];
			for(int i = 0; i < values.length; i++) {
				Object value = values[i];
				entries[i] = new Entry(value.toString(), value);
			}
		}
		
		public Object getEnumObject(String string) {
			for(var e: entries) {
				if (e.string.equals(string)) return e.value;
			}
			throw new UnexpectedException("Unexpected");
		}
	}
	public static Object getEnumObject(Class<?> clazz, String value) {
		EnumInfo enumInfo = EnumInfo.getEnumInfo(clazz);
		return enumInfo.getEnumObject(value);
	}
	
	public static class ClassInfo {
		static private Map<String, ClassInfo> map = new TreeMap<>();
		//                 typename

		public static ClassInfo getClassInfo(Class<?> clazz) {
			String name = clazz.getTypeName();
			if (map.containsKey(name)) {
				return map.get(name);
			} else {
				ClassInfo classInfo = new ClassInfo(clazz);
				map.put(name, classInfo);
				return classInfo;
			}
		}

		final Class<?>       clazz;
		final String         name;
		final Field[]        fields;
		final Constructor<?> constructor;
		final Class<?>[]     parameterTypes;
		final Constructor<?> constructor0;
		
		ClassInfo(Class<?> clazz_) {
			clazz  = clazz_;
			name   = clazz.getTypeName();
			
			{
				List<Field> list = new ArrayList<>();
				for(var field: clazz.getDeclaredFields()) {
					if (Modifier.isStatic(field.getModifiers())) continue;
					field.setAccessible(true);
					list.add(field);
				}
				fields = list.toArray(new Field[0]);
			}
			
			{
				Constructor<?> cntr       = null;
				Class<?>[]     paramTypes = null;
				Constructor<?> cntr0      = null;
				Constructor<?>[] list = clazz.getDeclaredConstructors();
				for(var e: list) {
					int parameterCount = e.getParameterCount();
					if (parameterCount == 0) {
						cntr0 = e;
						cntr0.setAccessible(true);
					}
					if (parameterCount == fields.length) {
						paramTypes = e.getParameterTypes();
						boolean hasSameType = true;
						for(int i = 0; i < fields.length; i++) {
							if (!paramTypes[i].equals(fields[i].getType())) {
								hasSameType = false;
							}
						}
						if (hasSameType) {
							cntr = e;
							cntr.setAccessible(true);
						}
					}
				}
				constructor    = cntr;
				parameterTypes = paramTypes;
				constructor0   = cntr0;
			}
		}
		
		boolean isCompatible(Class<?> a, Class<?> b) {
			if (a.equals(b)) return true;
			// Byte
			if (a.equals(Byte.TYPE) && b.equals(Byte.class)) return true;
			if (a.equals(Byte.class) && b.equals(Byte.TYPE)) return true;
			// Short
			if (a.equals(Short.TYPE) && b.equals(Short.class)) return true;
			if (a.equals(Short.class) && b.equals(Short.TYPE)) return true;
			// Integer
			if (a.equals(Integer.TYPE) && b.equals(Integer.class)) return true;
			if (a.equals(Integer.class) && b.equals(Integer.TYPE)) return true;
			// Long
			if (a.equals(Long.TYPE) && b.equals(Long.class)) return true;
			if (a.equals(Long.class) && b.equals(Long.TYPE)) return true;
			// Float
			if (a.equals(Float.TYPE) && b.equals(Float.class)) return true;
			if (a.equals(Float.class) && b.equals(Float.TYPE)) return true;
			// Double
			if (a.equals(Double.TYPE) && b.equals(Double.class)) return true;
			if (a.equals(Double.class) && b.equals(Double.TYPE)) return true;
			// Char
			if (a.equals(Character.TYPE) && b.equals(Character.class)) return true;
			if (a.equals(Character.class) && b.equals(Character.TYPE)) return true;
			// Boolean
			if (a.equals(Boolean.TYPE) && b.equals(Boolean.class)) return true;
			if (a.equals(Boolean.class) && b.equals(Boolean.TYPE)) return true;
			
			return false;
		}
		Object getInstance(Object... args) {
			if (args.length == 0) {
				if (constructor0 != null) {
					try {
						Object o = constructor0.newInstance();
						return o;
					} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						String exceptionName = e.getClass().getSimpleName();
						logger.error("{} {}", exceptionName, e);
						throw new UnexpectedException(exceptionName, e);
					}
				} else {
					throw new UnexpectedException("Unexpected");
				}
			} else {
				// sanity check
				if (fields.length != args.length) {
					logger.info("clazz   {}", this.name);
					logger.info("fields  {}", fields.length);
					logger.info("args    {}", args.length);
					logger.info("fields  {}", Arrays.stream(fields).map(o -> {return o.getType().getName();}).collect(Collectors.toList()));
					logger.info("args    {}", Arrays.stream(args)  .map(o -> {return o.getClass().getName();}).collect(Collectors.toList()));
					throw new UnexpectedException("Unexpected");
				}
				for(int i = 0; i < fields.length; i++) {
					if (!isCompatible(fields[i].getType(), args[i].getClass())) {
						logger.info("clazz   {}", this.name);
						logger.info("fields  {}", Arrays.stream(fields).map(o -> {return o.getType().getName();}).collect(Collectors.toList()));
						logger.info("args    {}", Arrays.stream(args)  .map(o -> {return o.getClass().getName();}).collect(Collectors.toList()));
						
						logger.info("class   {}  -  {}  -  {}", i, fields[i].getType().getTypeName(), args[i].getClass().getTypeName());
						
						throw new UnexpectedException("Unexpected");
					}
				}
				
				if (constructor != null) {
					try {
						Object o = constructor.newInstance(args);
						return o;
					} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						String exceptionName = e.getClass().getSimpleName();
						logger.error("{} {}", exceptionName, e);
						throw new UnexpectedException(exceptionName, e);
					}
				} else if (constructor0 != null) {
					try {
						Object o = constructor0.newInstance();
						for(int i = 0; i < fields.length; i++) {
							fields[i].set(o, args[i]);
						}
						return o;
					} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						String exceptionName = e.getClass().getSimpleName();
						logger.error("{} {}", exceptionName, e);
						throw new UnexpectedException(exceptionName, e);
					}
				} else {
					throw new UnexpectedException("Unexpected");
				}
			}
		}
	}
	
	public static Object getInstance(Class<?> clazz, Object... args) {
		ClassInfo classInfo = ClassInfo.getClassInfo(clazz);
		return classInfo.getInstance(args);
	}

}
