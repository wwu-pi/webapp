package w.exe.action;

import java.io.Serializable;

import m.var.ObjectReference;
import w.exe.action.meta.Action;
import w.xhtml.ui.component.Form;
import w.xhtml.ui.component.InputText;

public class TextInput extends Action implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final InputText input;
	protected final Form form;
	protected final ObjectReference stringRef;
	
	public TextInput(InputText input, Form form, ObjectReference stringRef) {
		this.input = input;
		this.form = form;
		this.stringRef = stringRef;
	}
	
	public InputText getInput() {
		return this.input;
	}
	public Form getForm() {
		return this.form;
	}
	public ObjectReference getStringRef() {
		return this.stringRef;
	}
	
	@Override public String toString() {
		return "Input text of reference=("+stringRef+") in text field: " +input;
	}	

}
