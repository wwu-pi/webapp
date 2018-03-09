package w.xhtml.ui.component;

import java.io.Serializable;

import w.xhtml.ui.component.meta.UIComponent;

public class Form extends UIComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final String id;
	
	public Form(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	@Override public String toString() {
		return "Form(id="+id+")";
	}
	
}
