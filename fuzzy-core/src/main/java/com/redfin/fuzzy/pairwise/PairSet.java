package com.redfin.fuzzy.pairwise;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/*package*/ class PairSet {

	private final Map<Param, ParamPairs> allPairs = new HashMap<>();
	private int pairs;

	/*package*/ void register(Pair pair) {
		register(pair.p1.param, pair.p2.param, pair);
		register(pair.p2.param, pair.p1.param, pair);

		pairs++;
	}

	/*package*/ boolean isEmpty() {
		return pairs <= 0;
	}

	/*package*/ int size() {
		return pairs;
	}

	/*package*/ Set<Pair> toSet() {
		Set<Pair> set = new HashSet<>(pairs);
		for(ParamPairs paramPairs : allPairs.values())
			for(Pairs subset : paramPairs.pairs.values())
				set.addAll(subset);

		return set;
	}

	/*package*/ Pair consume(Param p1, Param p2) {
		Pair p = allPairs.get(p1).get(p2).consume();
		if(p == null) return null;

		// Consume this pair's twin as well.
		allPairs.get(p2).discard(p);

		pairs--;

		return p;
	}

	private void register(Param p1, Param p2, Pair p) {
		if(!allPairs.containsKey(p1))
			allPairs.put(p1, new ParamPairs());

		allPairs.get(p1).register(p2, p);
	}

	private static class ParamPairs {
		final Map<Param, Pairs> pairs = new HashMap<>();

		Pairs get(Param p2) {
			return pairs.get(p2);
		}

		void register(Param p2, Pair p) {
			if(!pairs.containsKey(p2))
				pairs.put(p2, new Pairs());

			pairs.get(p2).add(p);
		}

		void discard(Pair p) {
			Pairs subset = pairs.get(p.p2.param);
			if(subset != null) subset.remove(p);
		}
	}

	private static class Pairs extends LinkedHashSet<Pair> {
		private static final long serialVersionUID = 1;
		Pair consume() {
			if(size() == 0)
				return null;

			Iterator<Pair> i = iterator();
			Pair p = i.next();

			remove(p);
			return p;
		}
	}

}
