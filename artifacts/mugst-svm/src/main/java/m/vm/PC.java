package m.vm;

import java.io.Serializable;

import m.state.StateElement;

public class PC extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;
	protected boolean valid;
	protected int pc;
	
	public PC(int pc) {
		this.pc = pc;
		this.valid = true;
	}
	
	public int getPC() {
		return this.pc;
	}

	public void updatePC(int position) {
		this.pc = position;
	}

	public void invalidate() {
		this.valid = false;	
	}
	
	@Override
	public String toString() {
		return "PC("+this.pc+")";
	}
}
