package m.var;

public class IntVariable extends Variable {

	private static final long serialVersionUID = 1L;

	protected boolean negate;
	
	public IntVariable(String name) {
		super(name);
	}

	public void negate() {
		this.negate = !negate;
	}

	public boolean isNegated() {
		return this.negate;
	}
	
	@Override
	public String toString() {
		if(isNegated()) return "-"+name;
		return name;
	}
}
