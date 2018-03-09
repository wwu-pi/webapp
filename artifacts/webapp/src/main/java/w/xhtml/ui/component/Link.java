package w.xhtml.ui.component;

import java.io.Serializable;

import w.xhtml.ui.component.meta.UIComponent;

public class Link extends UIComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final String value;
	protected final String outcome;
	
	public Link(String value, String outcome) {
		this.value = value;
		this.outcome = outcome;
	}

	public String getValue() {
		return value;
	}
	public String getOutcome() {
		return outcome;
	}
	@Override public String toString() {
		return "Link(value="+value+", outcome="+outcome+")";
	}
}
