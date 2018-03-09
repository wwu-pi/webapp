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
	
	@Override
	public String toString() {
		return "Issue(id="+id+", title="+title+")";
	}
}
