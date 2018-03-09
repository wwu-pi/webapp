package w.xhtml.ui.component;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import w.xhtml.ui.component.meta.UIComponent;

public class DataTableColumn extends UIComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final Set<UIComponent> uiComponents;
	
	public DataTableColumn() {
		this.uiComponents = new HashSet<>();
	}

	public void addComponent(UIComponent component) {
		this.uiComponents.add(component);
	}
	
	public Set<UIComponent> getUiComponents() {
		return uiComponents;
	}	
}
