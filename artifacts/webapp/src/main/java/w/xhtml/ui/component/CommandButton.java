package w.xhtml.ui.component;

import java.io.Serializable;

import w.xhtml.ui.component.meta.UIComponent;

public class CommandButton extends UIComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final String action;
	protected final String value;
	protected final String disabled;

	
	public CommandButton(String action, String value, String disabled) {
		this.action = action;
		this.value = value;
		this.disabled = disabled;
	}
	public String getAction() {
		return action;
	}
	public String getValue() {
		return value;
	}
	public String getDisabled() {
		return this.disabled;
	}
	@Override public String toString() {
		return "CommandButton(action="+action+", value="+value+")";
	}
	
	
}

