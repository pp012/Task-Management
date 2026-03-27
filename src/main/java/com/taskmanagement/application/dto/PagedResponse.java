package com.taskmanagement.application.dto;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> type of items in the page
 */
public class PagedResponse<T> {

	private List<T> content;
	private int page;
	private int size;
	private long totalElements;

	public PagedResponse() {
	}

	public PagedResponse(List<T> content, int page, int size, long totalElements) {
		this.content = content;
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
	}

	public List<T> getContent() {
		return content;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}
}
