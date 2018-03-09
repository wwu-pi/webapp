package w.it.web;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import w.it.ejb.RequestBean;

@Named
@RequestScoped
public class IssueCreate {

	@Inject
	protected RequestBean requestBean;
	
	protected String author;
	protected String title;

	public String submit() {
		this.requestBean.addNewIssue(author, title, "OPEN");
		return "1-view";
//		if(this.title.equals("andreas")) {
//			this.requestBean.addNewIssue(title, "OPEN");
//			return "d-view";
//		} else {
//			return "error";
//		}
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
}
