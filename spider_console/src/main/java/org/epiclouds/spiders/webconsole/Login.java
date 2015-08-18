package org.epiclouds.spiders.webconsole;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Servlet implementation class GetChart
 */

public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public Login() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession().getAttribute("user")!=null){
			request.getRequestDispatcher("index.jsp").forward(request, response);
			return;
		}
		String name=request.getParameter("username");
		String pass=request.getParameter("password");
		if("admin123".equals(name)&&"123Yuanshuju456".equals(pass)){
			request.setAttribute("success", "登录成功");
			request.getSession().setAttribute("user", "ok");
			request.getRequestDispatcher("index.jsp").forward(request, response);
			return;
		}
		request.setAttribute("error", "登录失败！");
		request.getRequestDispatcher("login.jsp").forward(request, response);
		return;
	}


}
