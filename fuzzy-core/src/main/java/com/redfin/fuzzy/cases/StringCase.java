package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Cases;
import com.redfin.fuzzy.Literal;
import com.redfin.fuzzy.FuzzyPreconditions;
import com.redfin.fuzzy.Suppliers;
import com.redfin.fuzzy.FuzzyUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class StringCase implements Case<String> {

	/*package*/ static final Set<String> ALPHABET_CHARS = Collections.unmodifiableSet(
		FuzzyUtil.toCharSet("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
	);
	/*package*/ static final Set<String> DIGIT_CHARS = Collections.unmodifiableSet(FuzzyUtil.toCharSet("0123456789"));
	/*package*/ static final Set<String> HEX_CHARS = Collections.unmodifiableSet(FuzzyUtil.toCharSet("0123456789abcdef"));

	/*package*/ static final Set<String> UNICODE_CHARS = Collections.unmodifiableSet(FuzzyUtil.toCharSet("アあ語ΔᎣ‰✈"));
	/*package*/ static final Set<String> EMOJI_CHARS = Collections.unmodifiableSet(FuzzyUtil.setOf("\uD83C\uDF35", "\uD83C\uDF54", "\uD83D\uDC17"));
	/*package*/ static final Set<String> WHITESPACE_CHARS = Collections.unmodifiableSet(FuzzyUtil.toCharSet(" \t\r\n\f"));

	// Note: we're using a single string here instead of a character set because we want to increase the chances of each
	// of these characters being included. TODO: break each character into a separate case?
	/*package*/ static final Set<String> INJECTION_STRINGS = Collections.unmodifiableSet(
		Collections.singleton("--'\",$%\\<&")
	);

	private static final Set<String> STANDARD_CHARS = Collections.unmodifiableSet(FuzzyUtil.union(
		ALPHABET_CHARS,
		DIGIT_CHARS,
		WHITESPACE_CHARS,
		FuzzyUtil.toCharSet("~!@#$%^&*()_+`-=[]\\{}|;':\",./<>?")
	));

	private Case<Integer> length = Any.of(
		Literal.value(0),
		Any.integer().inRange(1, 256)
	);

	private Case<Set<String>> sourceStrings = Any.of(
		Literal.value(STANDARD_CHARS),
		Literal.value(WHITESPACE_CHARS),
		Literal.value(UNICODE_CHARS),
		Literal.value(EMOJI_CHARS),
		Literal.value(INJECTION_STRINGS)
	);

	public StringCase withLengthOf(Case<Integer> length) {
		this.length = FuzzyPreconditions.checkNotNull(length);
		return this;
	}

	public StringCase withLength(int length) {
		return withLengthOf(Literal.value(length));
	}

	public StringCase nonEmpty() { return withLengthOf(Any.integer().inRange(1, 256)); }

	public StringCase withSourceStringsOf(Case<Set<String>> sourceChars) {
		sourceStrings = FuzzyPreconditions.checkNotNull(sourceChars);
		return this;
	}

	public StringCase withSourceStrings(Set<String> sourceStrings) {
		return withSourceStringsOf(Literal.value(sourceStrings));
	}

	public StringCase withSourceStrings(String... sourceStrings) {
		FuzzyPreconditions.checkNotNullAndContainsNoNulls(sourceStrings);

		Set<String> sourceStringSet = new HashSet<>(sourceStrings.length);
		sourceStringSet.addAll(Arrays.asList(sourceStrings));

		return withSourceStrings(sourceStringSet);
	}

	public StringCase withSourceCharsOf(Case<String> sourceChars) {
		return withSourceStringsOf(Cases.map(sourceChars, FuzzyUtil::toCharSet));
	}

	public StringCase withSourceChars(String sourceChars) {
		FuzzyPreconditions.checkNotNull(sourceChars);
		return withSourceStrings(FuzzyUtil.toCharSet(sourceChars));
	}

	public StringCase withOnlyAlphabetChars() {
		return withSourceStrings(ALPHABET_CHARS);
	}

	public StringCase withOnlyDigitChars() {
		return withSourceStrings(DIGIT_CHARS);
	}

	public StringCase withOnlyAlphanumericChars() {
		return withSourceStrings(FuzzyUtil.union(ALPHABET_CHARS, DIGIT_CHARS));
	}

	public StringCase withOnlyHexChars() {
		return withSourceStrings(HEX_CHARS);
	}

	@Override
	public Set<Function<Random, String>> getSuppliers() {
		// TODO: this is wasteful with regards to the case of string length of zero, which always results in ""
		return Suppliers.pairwisePermutations(
			length.getSuppliers(),
			sourceStrings.getSuppliers(),

			(rnd, length, strings) -> {
				if(length == null || length <= 0)
					return "";
				else if(length > 1024)
					length = 1024;

				if(strings == null || strings.isEmpty())
					strings = Collections.singleton("X");

				String[] stringsArr = strings.toArray(new String[strings.size()]);

				StringBuilder sb = new StringBuilder(length);
				while(sb.length() < length) {
					String chr = stringsArr[rnd.nextInt(stringsArr.length)];
					if(chr.length() == 0)
						chr = "X";

					sb.append(chr);
					if(sb.length() > length) {
						sb.delete(length, sb.length());
					}
				}

				return sb.toString();
			}
		);
	}
}
