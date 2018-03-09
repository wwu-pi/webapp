package w.sym.state;

import java.io.Serializable;

import m.cstr.ConstraintStack;
import m.javaee.JavaEEHeap;
import m.javaee.db.SymbolicObjectDatabase;
import w.bean.BeanManager;
import w.exception.SymbolicExecutionException;
import w.exe.action.meta.Action;
import w.exe.action.seq.ActionSequence;
import w.exe.cov.Coverage;
import w.xhtml.WebPageManager;
import w.xhtml.ui.WebPage;

public class SymbolicState implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected static int number;
	protected String id;
	
	protected WebPage previousPage;
	protected WebPage currentPage;
		
	protected Coverage coverage;
	
	protected JavaEEHeap heap;
	protected ConstraintStack constraints;
	
	protected WebPageManager webPageManager;
	
	protected ActionSequence actionSequence;
	
	protected SymbolicObjectDatabase database;
	
	protected int steps;
	
	/**
	 * Initial symbolic state, starting at the given start page.
	 * Heap is empty, no constraints in stack, and there is an empty action-sequence.
	 */
	public SymbolicState(WebPage startPage, SymbolicObjectDatabase database, WebPageManager webPageManager, BeanManager beanManager) {
		this.heap = new JavaEEHeap();
		this.coverage = new Coverage();
		this.webPageManager = webPageManager;
		this.constraints = new ConstraintStack();
		this.currentPage = startPage;
		this.database = new SymbolicObjectDatabase();
		this.actionSequence = new ActionSequence();
		this.id = "ID="+number++;
	}
	
	public Coverage getCoverage() {
		return this.coverage;
	}
	
	public WebPageManager getWebPageManager() {
		return this.webPageManager;
	}
	
	public JavaEEHeap getHeap() {
		return this.heap;
	}
	
	public ConstraintStack getConstraints() {
		return this.constraints;
	}

	public WebPage getCurrentPage() {
		return this.currentPage;
	}
	
	public WebPage getPreviousPage() {
		return this.previousPage;
	}
	
	public int getSteps() {
		return this.steps;
	}

	public void addAction(Action action) {
		this.actionSequence.add(action);
		this.steps++;
	}
	
	public ActionSequence getActionSequence() {
		return this.actionSequence;
	}

	public void updateCurrentPage(WebPage currentPage) {
		if(currentPage == null) throw new SymbolicExecutionException("Cannot update to current page = null!");
		this.previousPage = this.currentPage;
		this.currentPage = currentPage;
	}
	
	public void updateCopiedStateId() {
		this.id += "->" + number++;
	}
	
	@Override
	public String toString() {
		return this.id;
	}

	public SymbolicObjectDatabase getDatabase() {
		return this.database;
	}

	public void updateHeap(JavaEEHeap heap2) {
		this.heap = heap2;
	}
}
