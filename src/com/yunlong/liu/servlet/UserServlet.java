package com.yunlong.liu.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.yunlong.liu.bean.User;
import com.yunlong.liu.utils.CommonUtil;
import com.yunlong.liu.utils.JdbcUtil;
import com.yunlong.liu.utils.JsonUtil;

@SuppressWarnings("serial")
public class UserServlet extends HttpServlet {

	Logger logger = Logger.getLogger(UserServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json; charset=utf-8");
		JdbcUtil jdbcUtil = new JdbcUtil();
		// User user = new User();
		// user.setUsername("HELE");
		String params[] = { "HELE" };
		String sql = "SELECT * FROM user WHERE username = ?";
		List list = jdbcUtil.executeQuery(sql, params);
		logger.debug("sql = " + sql);
		List<User> users = CommonUtil.objArr2beanArr(User.class, list);
		// List<User> users = jdbcUtil.selectEntityList(User.class, user);
		// String dataJson = "{\"name\":\"fly\",\"type\":\"虫子\"}";
		String dataJson = JsonUtil.obj2json(users);
		PrintWriter writer = resp.getWriter();
		writer.write(dataJson);
		writer.close();
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		super.service(arg0, arg1);
	}

}
