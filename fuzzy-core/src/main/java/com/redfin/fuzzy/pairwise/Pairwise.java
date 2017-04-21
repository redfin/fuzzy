package com.redfin.fuzzy.pairwise;

import com.redfin.fuzzy.FuzzyPreconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Pairwise<S extends Collection> {

	private final List<Param> params;

	public Pairwise(List<S> parameters) {
		FuzzyPreconditions.checkNotNull(parameters);

		List<Param> params = new ArrayList<>();
		int i = 0;
		for(Collection<?> parameter : parameters) {
			FuzzyPreconditions.checkNotNullAndContainsNoNulls(parameter);
			params.add(new Param(i++, new ArrayList<>(parameter)));
		}

		this.params = Collections.unmodifiableList(params);
	}

	/*package*/ PairSet generatePairs() {
		PairSet pairs = new PairSet();

		for(int i = 0; i < params.size() - 1; i++) {
			for(ParamValue p1 : params.get(i).values) {
				for(int j = i + 1; j < params.size(); j++) {
					for(ParamValue p2 : params.get(j).values) {
						pairs.register(new Pair(p1, p2));
					}
				}
			}
		}

		return pairs;
	}

	public Stack<List<Object>> generate() {
		// Special case: if there's only one parameter, then there are obviously no pairs. Just return all the parameter
		// values.
		if(params.size() == 1) {
			Stack<List<Object>> ret = new Stack<>();
			for(ParamValue value : params.get(0).values) {
				ret.push(Collections.singletonList(value.value));
			}
			return ret;
		}

		// Step 1: build some round-robin selectors for all of the parameters.
		Map<Param, Selector> selectors = new HashMap<>(params.size());
		for(Param p : params) selectors.put(p, new Selector(p));

		// Step 2: compute all of the expected pairs in our input set.
		PairSet pairs = generatePairs();

		// Step 3: start consuming pairs one at a time until each pair has been used.
		Stack<List<Object>> testCases = new Stack<>();
		while(!pairs.isEmpty()) {
			// Step a: perform an exhaustive search of any pairs we can add to this iteration.
			Map<Param, Object> chosenValues = new HashMap<>(params.size());
			for(int i = 0; i < params.size() - 1; i++) {
				for(int j = i + 1; j < params.size(); j++) {
					Param p1 = params.get(i);
					Param p2 = params.get(j);

					if(!chosenValues.containsKey(p1) && !chosenValues.containsKey(p2)) {
						Pair p = pairs.consume(p1, p2);
						if(p != null) {
							chosenValues.put(p.p1.param, p.p1.value);
							chosenValues.put(p.p2.param, p.p2.value);
						}
					}
				}
			}

			// Step 2: convert our map to a list of output values, filling in any missing parameters from our selectors
			List<Object> values = new ArrayList<>(params.size());
			for(Param p : params) {
				if(chosenValues.containsKey(p)) {
					values.add(chosenValues.get(p));
				}
				else {
					values.add(selectors.get(p).next());
				}
			}

			testCases.add(values);
		}

		return testCases;
	}

	private static class Selector {
		private final Param p;
		private int i;

		Selector(Param p) {
			this.p = p;
		}

		Object next() {
			if(i >= p.values.size()) i = 0;
			return p.values.get(i++).value;
		}
	}

}
