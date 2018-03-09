package w.exe.cov.meta;

import java.io.Serializable;

import m.cstr.ConstraintStack;
import m.exe.ex.SymbolicExecutionException;
import m.state.StateElement;
import w.exe.action.CommandButtonClick;
import w.exe.action.LinkClick;
import w.exe.action.TextInput;
import w.exe.action.meta.Action;
import w.xhtml.ui.WebPage;

public class CoverageEntry extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected final WebPage page;
	protected final Action action;
	protected final ConstraintStack constraints;
	
	public CoverageEntry(WebPage page, Action action, ConstraintStack constraints) {
		this.page = page;
		this.action = action;
		this.constraints = new ConstraintStack();
		this.constraints.addConstraint(constraints.getConstraints());
	}
	
	public ConstraintStack getConstraints() {
		return this.constraints;
	}
	
	public boolean covered(CoverageEntry other) {
		if(this.page.hashCode() != other.page.hashCode()) {
			return false;
		}
		
		if(this.action instanceof LinkClick) {
			LinkClick thisAction = (LinkClick)this.action;
			if(other.action instanceof LinkClick) {
				LinkClick otherAction = (LinkClick)other.action;
				return thisAction.getLink().getOutcome().equals(otherAction.getLink().getOutcome())
					&& thisAction.getLink().getValue().equals(otherAction.getLink().getValue())
					&& this.constraints.size() == other.constraints.size();
			}
			return false;
		} else if(this.action instanceof CommandButtonClick) {
			CommandButtonClick thisAction = (CommandButtonClick)this.action;
			if(other.action instanceof CommandButtonClick) {
				CommandButtonClick otherAction = (CommandButtonClick)other.action;
				return thisAction.getButton().getAction().equals(otherAction.getButton().getAction())
						&& thisAction.getButton().getValue().equals(otherAction.getButton().getValue())
						&& this.constraints.size() == other.constraints.size();
			}
			return false;
		} else if(this.action instanceof TextInput) {
			return true;
		} else {
			throw new SymbolicExecutionException("Unknown action in coverage entry: " + this.action);
		}
	}
	
	@Override
	public String toString() {
		return "(action="+action+", constraints="+constraints+")";
	}
}
