package w.exception;

public class AnalyzeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AnalyzeException(String message) {
		super(message);
	}
	
	public AnalyzeException(String message, Throwable t) {
		super(message, t);
	}
}
