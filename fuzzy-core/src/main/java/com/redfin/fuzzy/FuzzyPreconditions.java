package com.redfin.fuzzy;

import java.util.Collection;

public class FuzzyPreconditions {

	public static <T> T checkNotNull(T t) {
		if(t == null) throw new NullPointerException();
		return t;
	}

	public static <T> T checkNotNull(String message, T t) {
		if(t == null) throw new NullPointerException(message);
		return t;
	}

	public static <T> T[] checkNotNullAndContainsNoNulls(T[] array) {
		checkNotNull(array);
		for(T t : array) {
			if(t == null) throw  new IllegalArgumentException("Array contains a null value.");
		}
		return array;
	}

	public static <T, C extends Collection<T>> C checkNotNullAndContainsNoNulls(C c) {
		checkNotNull(c);
		for(T t : c) {
			if(t == null) throw new IllegalArgumentException("Collection contains a null value.");
		}
		return c;
	}

	public static <T> T[] checkNotEmpty(T[] array) {
		checkNotNull(array);
		if(array.length == 0) throw new IllegalArgumentException("Array does not contain any values.");
		return array;
	}

}
