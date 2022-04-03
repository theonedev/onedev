package io.onedev.server.web.component.pipeline;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;

public class JobIndex implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int column;
	
	private final int row;
	
	public JobIndex(int column, int row) {
		this.column = column;
		this.row = row;
	}
	
	public int getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	public static JobIndex of(List<List<Job>> pipeline, Job job) {
		for (int columnIndex = 0; columnIndex < pipeline.size(); columnIndex++) {
			List<Job> column = pipeline.get(columnIndex);
			for (int rowIndex = 0; rowIndex < column.size(); rowIndex++) {
				if (column.get(rowIndex).equals(job)) 
					return new JobIndex(columnIndex, rowIndex);
			}
		}
		throw new ExplicitException("Unable to find job: " + job.getName());
	}
	
	public Job getJob(List<List<Job>> pipeline) {
		return pipeline.get(column).get(row);
	}
	
	public String toJson() {
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		return column + "-" + row;
	}
	
}
