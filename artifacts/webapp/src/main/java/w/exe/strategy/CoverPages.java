//package w.exe.strategy;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import w.exe.strategy.meta.CoverageStrategy;
//import w.xhtml.WebPageManager;
//import w.xhtml.ui.WebPage;
//
//public class CoverPages extends CoverageStrategy {
//
//	private final int visitPageCount;
//	private final WebPageManager webPageManager;
//	private final Map<WebPage, Integer> coveredPages;
//	
//	public CoverPages(int visitPageCount, WebPageManager webPageManager) {
//		this.visitPageCount = visitPageCount;
//		this.webPageManager = webPageManager;
//		this.coveredPages = new HashMap<>();
//	}
//	
//	public void addCoveredPage(WebPage page) {
//		Integer number = this.coveredPages.get(page);
//		if(number == null) {
//			number = 1;
//		} else {
//			number ++;
//		}
//		this.coveredPages.put(page, number);
//	}
//
//	@Override
//	public boolean enoughCoverage() {
//		return coverage() >= 0.9;
//	}
//
//	@Override
//	public double coverage() {
//		int allPages = this.webPageManager.getWebPages().size();
//		if(allPages == 0) return 0;
//		
//		int covered = 0;		
//		for(WebPage page : this.coveredPages.keySet()) {
//			if(this.coveredPages.get(page) >= this.visitPageCount) {
//				covered++;
//			}
//		}
//		return covered / allPages;
//	}
//}
