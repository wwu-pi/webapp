package w.xhtml.ui.component;

import java.io.Serializable;

import w.xhtml.ui.component.meta.UIComponent;

public class OutputText extends UIComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final String id;
	protected final String value;
	
	public OutputText(String id, String value) {
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}
	public String getValue() {
		return value;
	}
	@Override public String toString() {
		return "InputText(id="+id+", value="+value+")";
	}
	
}
