package w.it.web;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import w.it.ejb.RequestBean;
import w.it.entity.Issue;

@Named
@RequestScoped
public class IssueEdit {

	@Inject
	protected RequestBean requestBean;
	
	public String submit(Issue issue) {
		requestBean.updateIssue(issue.getId(), issue.getAuthor(), issue.getTitle(), issue.getStatus());
		return "1-view";
	}
}
