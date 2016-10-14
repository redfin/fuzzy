package com.redfin.fuzzy.pairwise;

import com.redfin.fuzzy.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*package*/ class Param {

	/*package*/ final int id;
	/*package*/ final List<ParamValue> values;

	public Param(int id, List<Object> values) {
		Preconditions.checkNotNullAndContainsNoNulls(values);

		this.id = id;

		int[] paramId = new int[] { 0 };
		this.values = Collections.unmodifiableList(values.stream()
			.map(p -> new ParamValue(this, paramId[0]++, p))
			.collect(Collectors.toList())
		);
	}

}
