package w.exe.action.seq;

import java.io.Serializable;
import java.util.LinkedList;

import m.state.StateElement;
import w.exe.action.meta.Action;

public class ActionSequence extends StateElement  implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final LinkedList<Action> sequence;
	
	public ActionSequence() {
		this.sequence = new LinkedList<>();
	}

	public void add(Action action) {
		this.sequence.add(action);
	}

	public LinkedList<Action> getActions() {
		return this.sequence;
	}

	@Override public String toString() {
		return "< " + sequence + " >";
	}
}
