package com.yunlong.liu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 工具类封装插入和查询列表两种功能，目前功能还比较弱，数据库字段顺序和bean实体类顺序一致，表名称和实体类名称小写一致
 * 
 * 
 * 还有从网上摘抄下来直接传入sql和String[]方式，这种方式获取的list结果为list<Object>形式，commonUtil.java中有工具将其转为list<bean>形式
 * @author liuyunlong 2016年8月30日
 *
 */
public class JdbcUtil {
	private static String USERNAME;
	// 数据库密码
	private static String PASSWORD;
	// 驱动信息
	private static String DRIVER;
	// 数据库地址
	private static String URL;

	private Connection connection;

	private PreparedStatement pstmt;

	private ResultSet resultSet;

	static {
		Properties p = new Properties();
		InputStream in = JdbcUtil.class.getResourceAsStream("/db.properties");
		try {
			p.load(in);
			in.close();
			USERNAME = p.getProperty("jdbc.username");
			PASSWORD = p.getProperty("jdbc.password");
			URL = p.getProperty("jdbc.url");
			DRIVER = p.getProperty("jdbc.driver");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JdbcUtil() {
		try {
			Class.forName(DRIVER);
			getConnection();
		} catch (Exception e) {

		}
	}

	/** 
	 * 获得数据库的连接 
	 * @return 
	 */
	private Connection getConnection() {
		try {
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			System.out.println("数据库连接成功！");
		} catch (SQLException e) {
			System.out.println("数据库连接异常！");
			e.printStackTrace();
		}
		return connection;
	}

	/** 
	 * 释放数据库连接 
	 */
	private void close() {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 增
	 * liuyunlong at 2016年8月30日
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> boolean inserEntity(Class<T> c, Object obj) {
		boolean flag = false;
		StringBuilder columnBuilder = new StringBuilder();
		StringBuilder fieldsBuilder = new StringBuilder();
		String tableName = c.getSimpleName().toLowerCase();
		try {
			// obj转bean对象
			T instance = c.newInstance();
			instance = (T) obj;

			// 获取表的所有列
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet colSet = metaData.getColumns(connection.getCatalog(), "root", tableName, null);
			Field[] fields = c.getDeclaredFields(); // 获取所有属性
			columnBuilder.append("INSERT INTO " + tableName + " (");
			fieldsBuilder.append(" VALUES(");
			int num = 0;
			while (colSet.next()) {
				if ("YES".equals(colSet.getObject(23))) { // 自动增长类型，不参与赋值
					num++;
					continue;
				}
				columnBuilder.append(colSet.getObject(4) + ",");
				fields[num].setAccessible(true);
				Object value = (String) fields[num].get(instance);// get方法获取bean对象的值
				Object def = colSet.getObject(13);
				if (null == value && null != def) {
					value = def;
				}
				if (fields[num].getType() == Integer.class) {
					fieldsBuilder.append(value + ",");
				} else {
					fieldsBuilder.append("'" + value + "" + "',");
				}
				num++;
			}
			columnBuilder.deleteCharAt(columnBuilder.length() - 1);
			fieldsBuilder.deleteCharAt(fieldsBuilder.length() - 1);
			columnBuilder.append(")");
			fieldsBuilder.append(")");
			String sql = columnBuilder.append(fieldsBuilder).toString();
			System.out.println("sql = " + sql);
			pstmt = connection.prepareStatement(sql);
			int i = pstmt.executeUpdate();
			flag = (i > 0 ? true : false);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return flag;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> selectEntityList(Class<T> c, Object entity) {
		List<T> list = new ArrayList<>();
		String tableName = c.getSimpleName().toLowerCase();
		StringBuilder whereAppend = new StringBuilder();
		String sql = "SELECT * FROM " + tableName;
		whereAppend.append(" WHERE ");
		try {
			T instance = c.newInstance();
			instance = (T) entity;
			Field[] fields = c.getDeclaredFields();
			int num = 0;

			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet columns = metaData.getColumns(connection.getCatalog(), "root", tableName, null);
			while (columns.next()) {
				fields[num].setAccessible(true);
				Object value = fields[num].get(instance);
				if (null != value) {
					if (fields[num].getType() == Integer.class) {
						whereAppend.append(columns.getObject(4) + " = " + value + " AND ");
					} else {
						whereAppend.append(columns.getObject(4) + " = '" + value + "' AND ");
					}
				}
				num++;
			}
			sql = sql + whereAppend.toString();
			sql = sql.substring(0, sql.lastIndexOf("AND"));
			System.out.println("SELECT SQL = " + sql);
			pstmt = connection.prepareStatement(sql);
			ResultSet result = pstmt.executeQuery();
			list = Result2List(result, c);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}

	/**
	 * result 转成bean对象List
	 * liuyunlong at 2016年8月30日
	 * @return
	 */
	private <T> List<T> Result2List(ResultSet result, Class<T> c) {
		if (null == result) {
			return null;
		}
		List<T> list = new ArrayList<>();
		try {
			Field[] fields = c.getDeclaredFields();
			while (result.next()) {
				T instance = c.newInstance();
				ResultSetMetaData metaData2 = result.getMetaData();
				int count = metaData2.getColumnCount();
				for (int i = 0; i < count; i++) {
					fields[i].setAccessible(true);
					fields[i].set(instance, result.getObject(i + 1));
				}
				list.add(instance);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * 以下是直接传入sql+String[]方式获取
	 * liuyunlong at 2016年8月31日
	 * @return
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	// 该方法执行一个update/delete/insert语句
	// sql语句是带问号的格式，如：update table_name set column_name = ? where ...
	// parameters = {"...", "..."...}；
	public void executeUpdate(String sql, String[] parameters) {

		try {
			pstmt = connection.prepareStatement(sql);
			// 给？赋值
			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					pstmt.setString(i + 1, parameters[i]);
				}
			}
			// 执行语句
			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			close();
		}
	}

	// 可以执行多个update、delete、insert语句（考虑事务）
	public void executeUpdate(String[] sqls, String[][] parameters) {
		try {
			// 多个sql语句，考虑事务
			connection.setAutoCommit(false);

			for (int i = 0; i < sqls.length; i++) {
				if (parameters[i] != null) {
					pstmt = connection.prepareStatement(sqls[i]);

					for (int j = 0; j < parameters[i].length; j++) {
						pstmt.setString(j + 1, parameters[i][j]);
					}
					pstmt.executeUpdate();
				}

			}

			connection.commit();
		} catch (SQLException e) {
			// 回滚
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			close();
		}
	}

	// 统一的select语句，为了能够访问结果集，将结果集放入ArrayList，这样可以直接关闭资源
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList executeQuery(String sql, String[] parameters) {
		ArrayList results = new ArrayList();

		try {
			pstmt = connection.prepareStatement(sql);

			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					pstmt.setString(i + 1, parameters[i]);
				}
			}

			resultSet = pstmt.executeQuery();

			ResultSetMetaData rsmd = resultSet.getMetaData();
			int column = rsmd.getColumnCount();

			while (resultSet.next()) {
				Object[] objects = new Object[column];

				for (int i = 1; i <= column; i++) {
					objects[i - 1] = resultSet.getObject(i);
				}

				results.add(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			close();
		}
		return results;
	}

	/** 
	 * @param args 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JdbcUtil jdbcUtil = new JdbcUtil();

		/*******************增*********************/
		// User user = new User("HELE", "WJORLD");
		// jdbcUtil.inserEntity(User.class, user);

		/*******************删*********************/

		/*******************改*********************/

		/*******************查*********************/
		// User entity = new User();
		// entity.setUsername("HELE");
		// entity.setId(22);
		// jdbcUtil.selectEntityList(User.class, entity);

	}

}
