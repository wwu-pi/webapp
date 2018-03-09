package w.exe.action.assertion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import m.vm.heap.CopiedSymbolicObject;
import m.vm.heap.SymbolicObject;
import w.exception.SymbolicExecutionException;
import w.exe.action.assertion.meta.Assertion;
import w.xhtml.ui.component.DataTable;

public class AssertDataTableEntries extends Assertion  {

	private static final long serialVersionUID = 1L;
	
	protected final DataTable dataTable;
	protected final List<CopiedSymbolicObject> elements;
	
	public AssertDataTableEntries(DataTable dataTable, List<SymbolicObject> values) {
		this.dataTable = dataTable;
		this.elements = new ArrayList<>();
		for(SymbolicObject symObj : values) {
			try {
				this.elements.add(symObj.copy());
			} catch (IOException e) {
				throw new SymbolicExecutionException("Could not copy symbolic object", e);
			}
		}
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public List<CopiedSymbolicObject> getElements() {
		return elements;
	}
	
	
	
}
