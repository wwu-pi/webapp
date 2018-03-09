package w.it.web;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import w.it.ejb.RequestBean;
import w.it.entity.Issue;

@Named
@SessionScoped
public class IssueManager implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	protected RequestBean requestBean;
	
	protected Issue currentIssue;

	public Issue getCurrentIssue() {
		return currentIssue;
	}

	public void setCurrentIssue(Issue currentIssue) {
		this.currentIssue = currentIssue;
	}
	
	public String editIssue(Issue issue) {
		this.currentIssue = issue;
		return "2-edit";
	}
	
	public String archive(Issue issue) {
		requestBean.archive(issue.getId());
		return "1-view";
	}
	
	public IssueManager() {
	}
	
}
