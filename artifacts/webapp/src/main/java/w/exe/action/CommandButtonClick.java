package w.exe.action;

import java.io.Serializable;

import w.exe.action.meta.Action;
import w.xhtml.ui.component.CommandButton;
import w.xhtml.ui.component.Form;

public class CommandButtonClick extends Action implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final Form form;
	protected final CommandButton button;
	
	public CommandButtonClick(Form form, CommandButton button) {
		this.form = form;
		this.button = button;
	}
	
	public Form getForm() {
		return this.form;
	}
	
	public CommandButton getButton() {
		return this.button;
	}
	
	@Override public String toString() {
		return "Click on button=" + button;
	}

}
