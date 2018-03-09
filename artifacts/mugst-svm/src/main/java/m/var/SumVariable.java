package m.var;

public class SumVariable extends IntVariable {

	private static final long serialVersionUID = 1L;
	
	protected final IntVariable val1;
	protected final IntVariable val2;
	
	public SumVariable(IntVariable val1, IntVariable val2) {
		super(val1+"+"+val2);
		this.val1 = val1;
		this.val2 = val2;
	}

	public IntVariable getVal1() {
		return val1;
	}

	public IntVariable getVal2() {
		return val2;
	}

}