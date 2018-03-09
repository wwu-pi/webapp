package w.exception;

public class SymbolicExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SymbolicExecutionException(String message) {
		super(message);
	}
	
	public SymbolicExecutionException(String message, Throwable t) {
		super(message, t);
	}

}
