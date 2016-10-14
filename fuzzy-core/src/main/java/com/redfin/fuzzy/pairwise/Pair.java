package com.redfin.fuzzy.pairwise;

/*package*/ class Pair {

	/*package*/ final ParamValue p1;
	/*package*/ final ParamValue p2;

	public Pair(ParamValue p1, ParamValue p2) {
		this.p1 = p1.param.id < p2.param.id ? p1 : p2;
		this.p2 = p1.param.id < p2.param.id ? p2 : p1;
	}

	@Override
	public int hashCode() {
		return
			(p1.param.id * 1000 + p1.id) +
			(p2.param.id * 100 + p2.id);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		else if(!(obj instanceof Pair))
			return false;
		else {
			Pair other = (Pair)obj;
			return
				p1.param.id == other.p1.param.id &&
				p2.param.id == other.p2.param.id &&
				p1.id == other.p1.id &&
				p2.id == other.p2.id
			;
		}
	}

}
