package m.exe.bcel;

import org.apache.bcel.generic.InstructionHandle;

public class PC {

	protected InstructionHandle ih;
	protected final m.vm.PC statePC;
	
	public PC(m.vm.PC statePC, InstructionHandle ih) {
		this.ih = ih;
		this.statePC = statePC;
	}
	
	public void setInstruction(InstructionHandle ih) {
		this.ih = ih;
		this.statePC.updatePC(this.ih.getPosition());
	}
	
	public InstructionHandle getCurrentInstruction() {
		return this.ih;
	}
	
	public InstructionHandle getNextInstruction() {
		return this.ih.getNext();
	}
	
	public void advance() {
		this.ih = this.ih.getNext();
		this.statePC.updatePC(this.ih.getPosition());
	}

	public boolean valid() {
		return this.ih != null;
	}
	
	public void invalidate() {
		this.ih = null;
		this.statePC.invalidate();
	}
	
}
