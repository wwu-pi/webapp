package w.xhtml.ui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import m.state.StateElement;
import w.xhtml.ui.component.CommandButton;
import w.xhtml.ui.component.DataTable;
import w.xhtml.ui.component.Form;
import w.xhtml.ui.component.InputText;
import w.xhtml.ui.component.Link;

public class WebPage extends StateElement implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final String name;
	
	protected Set<Link> simpleLinks;
	
	protected Set<Form> forms;
	protected Map<Form, Set<CommandButton>> formButtons;
	protected Map<Form, Set<InputText>> formInputs;
	
	protected Set<DataTable> dataTables;
	protected Map<DataTable, Set<Link>> dataTableLinks;
	protected Map<DataTable, Set<CommandButton>> dataTableButtons;
	
	public WebPage(String name) {
		this.name = name;
		this.forms = new HashSet<>();
		this.formButtons = new HashMap<>();
		this.formInputs = new HashMap<>();
		this.simpleLinks = new HashSet<>();
		this.dataTables = new HashSet<>();
		this.dataTableLinks = new HashMap<>();
		this.dataTableButtons = new HashMap<>();
	}
	
	public Set<Link> getSimpleLinks() {
		return this.simpleLinks;
	}
	
	public Set<Form> getForms() {
		return this.forms;
	}
	
	public Map<Form, Set<CommandButton>> getFormButtons() {
		return this.formButtons;
	}
	
	public Map<Form, Set<InputText>> getFormInputs() {
		return this.formInputs;
	}
	
	public Set<DataTable> getDataTables() {
		return this.dataTables;
	}
	
	public Map<DataTable, Set<CommandButton>> getDataTableButtons() {
		return this.dataTableButtons;
	}
	
	public void addSimpleLink(Link link) {
		this.simpleLinks.add(link);
	}

	public void addDataTableLink(DataTable dataTable, Link link) {
		Set<Link> links = this.dataTableLinks.get(dataTable);
		if(links == null) {
			links = new HashSet<>();
		}
		links.add(link);
		this.dataTableLinks.put(dataTable, links);
	}
	
	public void addDataTableButton(DataTable dataTable, CommandButton button) {
		Set<CommandButton> buttons = this.dataTableButtons.get(dataTable);
		if(buttons == null) {
			buttons = new HashSet<>();
		}
		buttons.add(button);
		this.dataTableButtons.put(dataTable, buttons);
	}

	public void addDataTable(DataTable dataTable) {
		this.dataTables.add(dataTable);
	}

	public void addForm(Form form) {
		this.forms.add(form);
	}

	public void addFormButton(Form form, CommandButton button) {
		Set<CommandButton> buttons = this.formButtons.get(form);
		if(buttons == null) {
			buttons = new HashSet<>();
		}
		buttons.add(button);
		this.formButtons.put(form, buttons);
	}

	public void addFormInput(Form form, InputText input) {
		Set<InputText> inputs = this.formInputs.get(form);
		if(inputs == null) {
			inputs = new HashSet<>();
		}
		inputs.add(input);
		this.formInputs.put(form, inputs);
	}

	@Override
	public String toString() {
		return this.name;
	}

	
}
