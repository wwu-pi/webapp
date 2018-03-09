package w.exe.cov;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import m.cstr.ConstraintStack;
import m.state.StateElement;
import w.exe.action.meta.Action;
import w.exe.cov.meta.CoverageEntry;
import w.xhtml.ui.WebPage;

public class Coverage extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Set<CoverageEntry> coverages;
	
	private Map<WebPage, Integer> visitedPages;
	
	public Coverage() {
		this.coverages = new HashSet<>();
		this.visitedPages = new HashMap<>();
	}
	
	public boolean isCovered(WebPage webPage, Action action, ConstraintStack constraints) {
		CoverageEntry newCoverage = new CoverageEntry(webPage, action, constraints);
		for(CoverageEntry cov : this.coverages) {
			if(cov.covered(newCoverage)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean addCoverage(WebPage webPage, Action action, ConstraintStack constraints) {
		boolean isCovered = isCovered(webPage, action, constraints);
		if(!isCovered) {
			this.coverages.add(new CoverageEntry(webPage, action, constraints));
		}
		return isCovered;
	}
	
	@Override
	public String toString() {
		return this.coverages.toString();
	}

	public void addVisitedPage(WebPage page) {
		Integer counter = this.visitedPages.get(page);
		if(counter == null) {
			counter = new Integer(0);
		}
		counter = counter + 1;
		this.visitedPages.put(page, counter);
	}
	
	public Map<WebPage, Integer> getVisitedPageCounter() {
		return visitedPages;
	}
}
