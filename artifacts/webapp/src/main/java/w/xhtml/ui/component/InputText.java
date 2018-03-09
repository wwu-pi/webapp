package w.xhtml.ui.component;

import java.io.Serializable;

import w.xhtml.ui.component.meta.UIComponent;

public class InputText extends UIComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final String id;
	protected final String value;
	protected final boolean required;
	
	public InputText(String id, String value, String required) {
		this.id = id;
		this.value = value;
		this.required = required != null && (required.toLowerCase().equals("true") || (required.length() > 2 && required.substring(1,required.length()-1).equals("true")))  ? true : false;
	}

	public String getId() {
		return id;
	}
	public String getValue() {
		return value;
	}
	public boolean isRequired() {
		return required;
	}
	@Override public String toString() {
		return "InputText(id="+id+", value="+value+")";
	}
	
}
