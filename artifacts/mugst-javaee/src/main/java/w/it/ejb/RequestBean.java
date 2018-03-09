package w.it.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import w.it.entity.Issue;

@Stateless
@Named
public class RequestBean {

	@PersistenceContext
    private EntityManager em;

	public List<Issue> getAllIssues() {
		return em.createQuery("FROM Issue", Issue.class).getResultList();
	}
	
	public void addNewIssue(String title) {
		Issue issue = new Issue();
		issue.setTitle(title);
		em.persist(issue);
	}

	public void updateIssue(long id, String title) {
		Issue issue = em.find(Issue.class, id);
		issue.setTitle(title);
		em.flush();
	}

}
