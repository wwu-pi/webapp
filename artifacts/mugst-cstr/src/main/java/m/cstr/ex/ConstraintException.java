package m.cstr.ex;

public class ConstraintException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConstraintException(String m) {
		super(m);
	}
	
	public ConstraintException(String m, Throwable t) {
		super(m,t);
	}

}
