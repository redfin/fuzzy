package com.redfin.fuzzy;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

public class Util {

	@SafeVarargs
	public static <T> Set<T> union(Set<T>... sets) {
		Set<T> ret = new HashSet<>();

		if(sets != null)
			for(Set<T> set : sets)
				if(set != null)
					ret.addAll(set);

		return ret;
	}

	@SafeVarargs
	public static <T> Set<T> setOf(T... elements) {
		if(elements == null || elements.length == 0)
			return Collections.emptySet();

		Set<T> ret = new HashSet<>(elements.length);
		ret.addAll(Arrays.asList(elements));

		return ret;
	}

	public static Set<String> toCharSet(String chars) {
		if(chars == null || chars.length() == 0)
			return Collections.emptySet();

		Set<String> ret = new HashSet<>(chars.length());
		for(int i = 0; i < chars.length(); i++)
			ret.add(Character.toString(chars.charAt(i)));

		return ret;
	}

	public static String inspect(Object value) {
		StringBuilder sb = new StringBuilder();
		inspectTo(sb, value);
		return sb.toString();
	}

	private static final char[] HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	private static final Map<Integer, String> JAVA_ESCAPES;
	private static final int MAX_INSPECT_DEPTH = 3;
	private static final int MAX_COLLECTION_LENGTH = 50;
	static {
		Map<Integer, String> escapes = new HashMap<>();
		escapes.put((int)'\\', "\\\\");
		escapes.put((int)'\b', "\\b");
		escapes.put((int)'\n', "\\n");
		escapes.put((int)'\t', "\\t");
		escapes.put((int)'\f', "\\f");
		escapes.put((int)'\r', "\\r");
		escapes.put((int)'"', "\\\"");
		JAVA_ESCAPES = Collections.unmodifiableMap(escapes);
	}

	public static void inspectTo(StringBuilder sb, Object value) {
		Preconditions.checkNotNull(sb);
		inspectToInternal(sb, value, 1);
	}

	private static void inspectIterableTo(
		StringBuilder sb, char open, Iterable i, Consumer<Object> elementAppender, char close
	) {
		sb.append(open);
		boolean first = true;
		int idx = 0;
		for(Object o : i) {
			if(first)
				first = false;
			else if(++idx >= MAX_COLLECTION_LENGTH) {
				sb.append(" ...");
				break;
			}
			else
				sb.append(", ");

			elementAppender.accept(o);
		}
		sb.append(close);
	}

	private static void inspectToInternal(final StringBuilder sb, final Object value, int depth) {
		final Consumer<Integer> appendHex = i -> {
			sb
				.append("\\u")
				.append(HEX_DIGITS[(i >> 12) & 0b1111])
				.append(HEX_DIGITS[(i >> 8 ) & 0b1111])
				.append(HEX_DIGITS[(i >> 4 ) & 0b1111])
				.append(HEX_DIGITS[(i      ) & 0b1111])
			;
		};

		if(value == null)
			sb.append("{null}");
		else if(value instanceof CharSequence) {
			sb.append('"');
			((CharSequence) value).codePoints().forEachOrdered(i -> {
				if(JAVA_ESCAPES.containsKey(i))
					sb.append(JAVA_ESCAPES.get(i));
				else if(i > 0xFFFF) {
					// surrogate pair
					char[] pair = Character.toChars(i);
					for(char c : pair) appendHex.accept((int)c);
				}
				else if(i < 32 || i > 127)
					appendHex.accept(i);
				else
					sb.append((char)i);
			});
			sb.append('"');
		}
		else if(value instanceof Map) {
			if(depth > MAX_INSPECT_DEPTH) { sb.append("..."); return; }
			inspectIterableTo(
				sb,
				'{',
				((Map)value).entrySet(),
				o -> {
					Map.Entry e = (Map.Entry)o;
					inspectToInternal(sb, e.getKey(), depth + 1);
					sb.append(": ");
					inspectToInternal(sb, e.getValue(), depth + 1);
				},
				'}'
			);
		}
		else if(value.getClass().isArray()) {
			if(depth > MAX_INSPECT_DEPTH) { sb.append("..."); return; }
			inspectIterableTo(
				sb,
				'[',
				() -> new Iterator() {
					int i = 0;
					int len = Array.getLength(value);
					@Override public boolean hasNext() { return i < len; }
					@Override public Object next() {
						if(!hasNext()) throw new NoSuchElementException();
						else return Array.get(value, i++);
					}
				},
				o -> inspectToInternal(sb, o, depth + 1),
				']'
			);
		}
		else if(value instanceof Iterable) {
			if(depth > MAX_INSPECT_DEPTH) { sb.append("..."); return; }
			inspectIterableTo(
				sb, '[', (Iterable)value, o -> inspectToInternal(sb, o, depth + 1), ']'
			);
		}

		// TODO: other common types whose .toString isn't agreeable
		else
			sb.append(value.toString());
	}

}
