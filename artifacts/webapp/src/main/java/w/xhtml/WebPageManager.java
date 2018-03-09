package w.xhtml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import m.state.StateElement;
import w.exception.AnalyzeException;
import w.xhtml.parser.XHTMLParser;
import w.xhtml.ui.WebPage;

public class WebPageManager extends StateElement {

	private static final long serialVersionUID = 1L;

	protected final XHTMLFileManager xhtmlManager;
	
	protected Map<String, WebPage> webPages;

	public WebPageManager(String webappPath) {
		this.xhtmlManager = new XHTMLFileManager(webappPath);
		this.webPages = new HashMap<>();
	}
	
	public Map<String, WebPage> getWebPages() {
		return this.webPages;
	}

	public WebPage getPageByName(String pageName) {
		pageName = format(pageName);
		WebPage webPage = this.webPages.get(pageName);
		if(webPage == null) {
			webPage = parse(pageName);
			this.webPages.put(pageName, webPage);
		}
		return webPage;
	}
	
	private String format(String pageName) {
		if(pageName.startsWith("\"")) pageName = pageName.substring(1);
		if(pageName.endsWith("\"")) pageName = pageName.substring(0, pageName.length()-1);
		return pageName;
	}

	private WebPage parse(String pageName) {
		File pageFile = xhtmlManager.getFileByName(pageName);
		if(pageFile == null) throw new AnalyzeException("Could not find start page by name: " + pageName);
		
		XHTMLParser pageParser = new XHTMLParser(pageFile);
		try {
			pageParser.parse();
		} catch (IOException e) {
			throw new AnalyzeException("Could not parse page: "+ pageName, e);
		}
		return pageParser.getPage();
	}
	

}
