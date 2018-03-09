package w.exe.action;

import java.io.Serializable;

import w.exe.action.meta.Action;
import w.xhtml.ui.component.Link;

public class LinkClick extends Action implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected final Link link;
	
	public LinkClick(Link link) {
		this.link = link;
	}
	
	public Link getLink() {
		return link;
	}
	
	@Override public String toString() {
		return "Click on link=" + link;
	}	
}
