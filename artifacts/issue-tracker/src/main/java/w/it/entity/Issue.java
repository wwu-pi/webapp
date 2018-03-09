package w.it.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Issue {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	protected long id;
	
	@NotNull
	@Size(min=1)
	protected String title;
	
	@NotNull
	@Size(min=1)
	protected String author;
	
	@NotNull
	protected String status;
	
	public Issue() {
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String toString() {
		return "Issue(id="+id+", title="+title+", status="+status+")";
	}
}
