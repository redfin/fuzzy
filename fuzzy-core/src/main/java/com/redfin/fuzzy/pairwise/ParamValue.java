package com.redfin.fuzzy.pairwise;

/*package*/ class ParamValue {

	/*package*/ final Param param;
	/*package*/ final int id;
	/*package*/ final Object value;

	/*package*/ ParamValue(Param param, int id, Object value) {
		this.param = param;
		this.id = id;
		this.value = value;
	}

}
