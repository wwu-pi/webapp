package m.var;

public class CharConstant extends IntConstant {

	private static final long serialVersionUID = 1L;

	public CharConstant(char value) {
		super((int)value);;
	}
	
	@Override
	public String toString() {
		return (char)this.value+"";
	}

}
