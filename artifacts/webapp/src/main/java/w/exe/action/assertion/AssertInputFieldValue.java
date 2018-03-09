package w.exe.action.assertion;

import java.io.IOException;

import m.var.ObjectReference;
import m.vm.heap.CopiedSymbolicObject;
import w.exception.SymbolicExecutionException;
import w.exe.action.assertion.meta.Assertion;
import w.xhtml.ui.component.InputText;

public class AssertInputFieldValue extends Assertion  {

	private static final long serialVersionUID = 1L;
	
	protected final InputText inputText;
	protected final CopiedSymbolicObject stringValue;
	
	public AssertInputFieldValue(InputText inputText, ObjectReference stringValue) {
		this.inputText = inputText;
		try {
			this.stringValue = stringValue.getReferencedObject().copy();
		} catch (IOException e) {
			throw new SymbolicExecutionException("Could not copy state of string value", e);
		}
	}

	public InputText getInputText() {
		return inputText;
	}
	public CopiedSymbolicObject getStringValue() {
		return stringValue;
	}
}
