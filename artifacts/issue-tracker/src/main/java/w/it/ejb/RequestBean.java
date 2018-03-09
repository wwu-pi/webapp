package w.it.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import w.it.entity.Issue;
import w.it.entity.Status;

@Stateless
@Named("requestBean")
public class RequestBean {

	@PersistenceContext
    private EntityManager em;

	public List<Issue> getAllIssues() {
		List<Issue> list = em.createQuery("FROM Issue", Issue.class).getResultList();
		return list;
	}
	
	public void addNewIssue(String author, String title, String status) {
		Issue issue = new Issue();
		issue.setAuthor(author);
		issue.setTitle(title);
		issue.setStatus(status);
		em.persist(issue);
	}

	public void updateIssue(long id, String author, String title, String status) {
		Issue issue = em.find(Issue.class, id);
		issue.setAuthor(author);
		issue.setTitle(title);
		issue.setStatus(status);
		em.flush();
	}
	
	public void archive(long id) {
		Issue issue = em.find(Issue.class, id);
		issue.setStatus("ARCHIVED");
		em.flush();
	}

}
