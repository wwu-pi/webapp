package w.xhtml.ui.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import m.vm.heap.SymbolicObject;
import w.xhtml.ui.component.meta.UIComponent;

public class DataTable extends UIComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final String id;
	protected final String value;
	protected final String var;
	protected final Set<DataTableColumn> columns;
	
	protected List<SymbolicObject> dataValues;
	
	public void addData(SymbolicObject entry) {
		this.dataValues.add(entry);
	}
	
	public List<SymbolicObject> getData() {
		return this.dataValues;
	}
	
	public DataTable(String id, String value, String var) {
		this.id = id;
		this.value = value;
		this.var = var;
		this.columns = new HashSet<>();
		this.dataValues = new ArrayList<>();
	}
	
	public void addColumn(DataTableColumn column) {
		this.columns.add(column);
	}

	public Set<DataTableColumn> getColumns() {
		return this.columns;
	}
	public String getId() {
		return id;
	}
	public String getValue() {
		return value;
	}
	public String getVar() {
		return var;
	}
	@Override public String toString() {
		return "DataTable(id="+id+", value="+value+", var="+var+")";
	}
}
