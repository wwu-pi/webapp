package m.javaee.db;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import m.javaee.var.SymbolicQuery;
import m.state.StateElement;
import m.var.ObjectReference;
import m.var.Variable;
import m.vm.heap.CopiedSymbolicObject;
import m.vm.heap.SymbolicObject;

public class SymbolicObjectDatabase extends StateElement implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map<String, Set<CopiedSymbolicObject>> requiredObjects;
	
	private Map<String, Set<SymbolicObject>> objectdb; 
		
	private Map<SymbolicQuery, ObjectReference> queryResults;
	private Map<ObjectReference, SymbolicQuery> resultsQuery;
	
	public SymbolicObjectDatabase() {
		this.objectdb = new HashMap<>();
		this.requiredObjects = new HashMap<>();
		this.queryResults = new HashMap<>();
		this.resultsQuery = new HashMap<>();
	}
	
	public void addRequiredObject(String entityClass, SymbolicObject entity) {
		Set<CopiedSymbolicObject> entitySet = this.requiredObjects.get(entityClass);
		if(entitySet == null) {
			entitySet = new HashSet<>();
		}
		try {
			entitySet.add(entity.copy());
		} catch (IOException e) {
			throw new RuntimeException("Could not copy object!", e);
		}
		this.requiredObjects.put(entityClass, entitySet);
	}
	
	public void persist(String entityClass, SymbolicObject entity) {
		Set<SymbolicObject> entitySet = this.objectdb.get(entityClass);
		if(entitySet == null) {
			entitySet = new HashSet<>();
		}
		entitySet.add(entity);
		this.objectdb.put(entityClass, entitySet);
	}
	
	public Map<String, Set<SymbolicObject>> getObjectDB() {
		return this.objectdb;
	}
	
	public Variable getId(SymbolicObject entity) {
		return entity.getValue(getIdField(entity));
	}
	
	private String getIdField(SymbolicObject entity) {
		return "id"; // TODO: get from annotation @Id
	}

	public SymbolicObject getObjectById(Variable type, Variable id) {
		for(String entityType : this.objectdb.keySet()) {
			for(SymbolicObject entity : this.objectdb.get(entityType)) {
				Variable entityId = getId(entity);
				if(entityId != null && entityId.equals(id)) {
					return entity;
				}
			}
		}
		return null;
	}

	public Set<SymbolicObject> selectAll(String entityClass) {
		Set<SymbolicObject> entitySet = this.objectdb.get(entityClass);
		if(entitySet == null) {
			entitySet = new HashSet<>();
			this.objectdb.put(entityClass, entitySet);
		}
		return entitySet;
	}
	
	@Override
	public String toString() {
		return this.objectdb.toString();
	}

	public void setQueryResult(SymbolicQuery symQuery, ObjectReference resultList) {
		this.queryResults.put(symQuery, resultList);
		this.resultsQuery.put(resultList, symQuery);
	}
	
	public SymbolicQuery getQuery(ObjectReference resultList) {
		return this.resultsQuery.get(resultList);
	}

	public Map<String, Set<CopiedSymbolicObject>> getRequiredObjects() {
		return this.requiredObjects;
	}

}
