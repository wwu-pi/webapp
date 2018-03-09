package m.exe.ex;

public class SymbolicExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SymbolicExecutionException(String m) {
		super(m);
	}
	
	public SymbolicExecutionException(String m, Throwable t) {
		super(m,t);
	}
}
