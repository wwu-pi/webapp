package m.var;

public class IntConstant extends IntVariable {

	private static final long serialVersionUID = 1L;

	protected int value;
	
	public IntConstant(int value) {
		super(""+value);
		this.value = value;
	}

	public int getIntValue() {
		if(this.negate) {
			return -this.value;
		}
		return this.value;
	}

}
