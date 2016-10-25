package com.miracle9.lottery.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BaseDao {
	@Autowired
	private SessionFactory sessionFactory;

	/**
	 * 获取当前线程的session。如果当前线程没有session就openSession并且绑定到当前线程。
	 * 所以同一个线程的session都是一样的。
	 */
	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * 打开一个新session
	 */
	public Session openSession() {
		return sessionFactory.openSession();
	}

	/**
	 * 根据ID查询
	 */
	@SuppressWarnings("unchecked")
	public <T> T getById(Class<T> c, int id) {
		return (T) getCurrentSession().get(c, id);
	}

	/**
	 * 更新
	 */
	public void update(Object obj) {
		getCurrentSession().update(obj);
	}

	/**
	 * 添加
	 */
	public void add(Object obj) {
		getCurrentSession().save(obj);
	}

	public void delete(Class<?> c, int id) {
		getCurrentSession().delete(getById(c, id));
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> c, String hql, Object... params) {
		Query query = getCurrentSession().createQuery(hql);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				query.setParameter(i, params[i]);
			}
		}
		return query.list();
	}

	/**
	 * 查询记录数
	 */
	public int getCount(String hql, Object... params) {
		Query query = getCurrentSession().createQuery(hql);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				query.setParameter(i, params[i]);
			}
		}
		return (Integer) query.uniqueResult();
	}

	/**
	 * 根据字段查询单个对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T getByField(Class<T> c, String hql, Object... params) {
		Query query = getCurrentSession().createQuery(hql);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				query.setParameter(i, params[i]);
			}
		}
		List<T> list = query.list();
		return list.isEmpty() ? null : list.get(0);
	}

	public void evict(Object obj) {
		getCurrentSession().evict(obj);
	}

	public void clear() {
		getCurrentSession().clear();
	}
	
	public void flush(){
		getCurrentSession().flush();
	}
}
