package com.yunlong.liu.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public class CommonUtil {

	/**
	 * list<Object[]> 装成list<Bean>对象
	 * liuyunlong at 2016年8月31日
	 * @return
	 */
	public static <T> List<T> objArr2beanArr(Class<T> c, List<?> list) {
		List<T> ts = new ArrayList<>();
		Field[] fields = c.getDeclaredFields();
		if (CollectionUtils.isNotEmpty(list) && null != fields) {
			for (int i = 0; i < list.size(); i++) {
				Object[] obj = (Object[]) list.get(i);
				try {
					T instance = c.newInstance();
					if (null != obj && obj.length > 0) {
						for (int j = 0; j < obj.length; j++) {
							fields[j].setAccessible(true);
							fields[j].set(instance, obj[j]);
						}
					}
					ts.add(instance);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ts;
	}
}
