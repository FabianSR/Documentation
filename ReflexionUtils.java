package utils;

import static java.util.stream.Stream.of;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author fsanchez.ext
 *
 */
public final class ReflexionUtils {

	private static final String GET_CLASS = "getClass";
	private static final String REFLECTION_EXCEPTION = "NoSuchMethodException";
	private static final String IS = "is";
	private static final String SET = "set";
	private static final String GET = "get";

	private ReflexionUtils() {
	}

	/**
	 * 
	 * It only works if the output object has at least the same attributes and same
	 * getters/setters as the input object
	 * 
	 * @param object from
	 * @param object to be mapped
	 * 
	 */

	public static <I, O> O buildOutputFromInput(final I in, final O out) {

		getGetters(in.getClass()).stream()
				.filter(w -> existMethod(getSetterFromAttributeName(getAttributeNameFromGetter(w.getName())), out))
				.forEach(method -> {
					try {
						out.getClass()
								.getMethod(getSetterFromAttributeName(getAttributeNameFromGetter(method.getName())),
										method.getReturnType())
								.invoke(out, in.getClass().getMethod(method.getName()).invoke(in));
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						throw new RuntimeException(REFLECTION_EXCEPTION, e);
					}
				});

		return out;
	}

	private static <O> boolean existMethod(String string, O out) {
		return of(out.getClass().getMethods()).anyMatch(e -> string.equalsIgnoreCase(e.getName()));
	}

	private static Set<Method> getGetters(final Class<?> clazz) {
		return of(clazz.getMethods()).filter(ReflexionUtils::isGetter).collect(Collectors.toSet());
	}

	private static boolean isGetter(final Method method) {
		return (method.getName().startsWith(GET) || method.getName().startsWith(IS))
				&& !GET_CLASS.equals(method.getName()) && method.getParameterTypes().length == 0
				&& !void.class.equals(method.getReturnType());
	}

	private static String getSetterFromAttributeName(final String attributeNameFromGetter) {
		return SET + attributeNameFromGetter;
	}

	private static String getAttributeNameFromGetter(final String methodName) {
		return methodName.substring((methodName.startsWith(GET) ? GET : IS).length());
	}

}
