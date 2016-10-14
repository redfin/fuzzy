package com.redfin.fuzzy;

import com.redfin.fuzzy.cases.LiteralCase;

public class Literal {

	public static <T> Case<T> value(T value) {
		return new LiteralCase<>(value);
	}

	public static <T> Case<T> nil() { return new LiteralCase<>(null); }

}
