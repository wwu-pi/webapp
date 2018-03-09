package w.it.web;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import w.it.ejb.RequestBean;
import w.it.entity.Issue;

@Named @RequestScoped
public class Simple {

	@Inject
	protected RequestBean requestBean;
	
	public String archive(Issue issue) {
		requestBean.archive(issue.getId());
		return "simple-view2";
	}

}
