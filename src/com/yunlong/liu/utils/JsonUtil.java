package com.yunlong.liu.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
	private final static ObjectMapper objectMapper = new ObjectMapper();

	private JsonUtil() {

	}

	public static ObjectMapper getInstance() {

		return objectMapper;
	}

	/**
	 * javaBean,list,array convert to json string
	 */
	public static String obj2json(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * json string convert to javaBean
	 */
	public static <T> T json2bean(String jsonStr, Class<T> clazz) {
		try {
			return objectMapper.readValue(jsonStr, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * json string convert to map
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, Object> json2map(String jsonStr) {
		try {
			return objectMapper.readValue(jsonStr, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * json string convert to map with javaBean
	 */
	public static <T> Map<String, T> json2map(String jsonStr, Class<T> clazz) {
		Map<String, Map<String, Object>> map = new HashMap<>();
		try {
			map = objectMapper.readValue(jsonStr, new TypeReference<Map<String, T>>() {
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, T> result = new HashMap<String, T>();
		for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
			result.put(entry.getKey(), map2bean(entry.getValue(), clazz));
		}
		return result;
	}

	/**
	 * json array string convert to list with javaBean
	 */
	public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz) {
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			list = objectMapper.readValue(jsonArrayStr, new TypeReference<List<T>>() {
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<T> result = new ArrayList<T>();
		for (Map<String, Object> map : list) {
			result.add(map2bean(map, clazz));
		}
		return result;
	}

	/**
	 * map convert to javaBean
	 */
	public static <T> T map2bean(Map<?, ?> map, Class<T> clazz) {
		return objectMapper.convertValue(map, clazz);
	}
}
