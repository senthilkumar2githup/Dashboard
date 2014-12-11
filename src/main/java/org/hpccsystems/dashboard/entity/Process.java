package org.hpccsystems.dashboard.entity;

import java.util.Date;

import org.hpcc.HIPIE.CompositionInstance;

public class Process {

	 private String id;
	 private String compositionName;
	 private String status;
	 private Date date;	    
	 private CompositionInstance compositionInstance;
	 
	 public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompositionName() {
		return compositionName;
	}

	public void setCompositionName(String compositionName) {
		this.compositionName = compositionName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public CompositionInstance getCompositionInstance() {
		return compositionInstance;
	}

	public void setCompositionInstance(CompositionInstance compositionInstance) {
		this.compositionInstance = compositionInstance;
	}
	    
	 public Process() {
	 }
	    
	 public Process(String projectName, CompositionInstance instance) throws Exception {
	        setId(instance.getWorkunitId());
	        setCompositionName(compositionName);
	        setStatus(instance.getWorkunitStatus());
	        setDate(instance.getWorkunitDate());
	        setCompositionInstance(instance);
	  }

	@Override
	public String toString() {
		return "Process [id=" + id + ", compositionName=" + compositionName
				+ ", status=" + status + ", date=" + date
				+ ", compositionInstance=" + compositionInstance + "]";
	}
	 
	 
}
