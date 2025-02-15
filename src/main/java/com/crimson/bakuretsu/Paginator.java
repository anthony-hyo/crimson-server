
package com.crimson.bakuretsu;

import com.crimson.bakuretsu.core.Model;

import java.util.List;

public class Paginator<T extends Model<T>> {
	private final Class<T> modelClass;
	private final int pageSize;
	private int currentPage;
	private String whereCondition;
	private Object[] whereParams;
	private String orderByClause;

	public Paginator(Class<T> modelClass, int pageSize) {
		this(modelClass, pageSize, null, (Object[]) null);
	}

	public Paginator(Class<T> modelClass, int pageSize, String whereCondition, Object... whereParams) {
		this.modelClass = modelClass;
		this.pageSize = Math.max(1, pageSize);
		this.currentPage = 1;
		this.whereCondition = whereCondition;
		this.whereParams = whereParams;
	}

	public Paginator<T> orderByRaw(String orderByClause) {
		this.orderByClause = orderByClause;
		return this;
	}

	public List<T> getPage(int page) {
		this.currentPage = Math.max(1, page);
		int offset = (currentPage - 1) * pageSize;

		var query = Model.query(modelClass);

		if (whereCondition != null && !whereCondition.isEmpty()) {
			query.where(whereCondition, whereParams);
		}

		if (orderByClause != null && !orderByClause.isEmpty()) {
			query.orderByRaw(orderByClause);
		}

		return query.limit(pageSize)
			.offset(offset)
			.get();
	}

	public List<T> nextPage() {
		return getPage(++currentPage);
	}

	public List<T> prevPage() {
		if (currentPage > 1) {
			return getPage(--currentPage);
		}
		return getPage(1);
	}

	public boolean hasNext() {
		return currentPage < getTotalPages();
	}

	public boolean hasPrevious() {
		return currentPage > 1;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getTotalPages() {
		long totalRecords = getCount();
		return (int) Math.ceil((double) totalRecords / pageSize);
	}

	public long getCount() {
		var query = Model.query(modelClass);

		if (whereCondition != null && !whereCondition.isEmpty()) {
			query.where(whereCondition, whereParams);
		}

		return query.count();
	}

	public int pageCount() {
		return getTotalPages();
	}
}