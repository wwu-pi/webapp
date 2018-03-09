package w.exe.action.meta;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import m.state.StateElement;
import w.exe.action.assertion.meta.Assertion;

public abstract class Action  extends StateElement implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected Set<Assertion> assertions;
	
	public Action() {
		this.assertions = new HashSet<>();
	}
	
	public void addAssertion(Assertion a) {
		this.assertions.add(a);
	}
	
	public Set<Assertion> getAssertions() {
		return this.assertions;
	}
}
