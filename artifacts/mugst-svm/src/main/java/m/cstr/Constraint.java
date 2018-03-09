package m.cstr;

import java.io.Serializable;

import m.state.StateElement;

public abstract class Constraint extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract Constraint negate();

}
