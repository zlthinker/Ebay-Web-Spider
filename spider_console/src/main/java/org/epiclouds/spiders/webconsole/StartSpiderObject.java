package org.epiclouds.spiders.webconsole;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;




/**
 * Servlet implementation class GetChart
 */

public class StartSpiderObject extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public StartSpiderObject() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession().getAttribute("user")==null){
			request.getRequestDispatcher("login.jsp").forward(request, response);
			return;
		}
		String classname=request.getParameter("classname");
		if(classname==null||"".equals(classname)){
			request.setAttribute("error", "爬虫类不能为空！");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
		String objectname=request.getParameter("objectname");
		if(objectname==null||"".equals(objectname)){
			request.setAttribute("error", "爬虫对象不能为空！");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
		ConsoleCommandBean result=ConsoleCommandManager.getManager().handleCommand(
				new ConsoleCommandBean(AbstractConsoleCommandHandler.ConsoleCommand.START,
				SpiderStatusManager.getSpiderClassBean(classname)),objectname);
		if(result!=null&&result.getCommand()==AbstractConsoleCommandHandler.ConsoleCommand.STARTSUCCESS){
			request.setAttribute("success", "启动爬虫对象"+objectname+"成功！");
			request.getRequestDispatcher("success.jsp").forward(request, response);
			return;
		}
		
		if(result==null||result.getCommand()!=AbstractConsoleCommandHandler.ConsoleCommand.STARTSUCCESS){
			request.setAttribute("error", "启动爬虫对象出错！"+result);
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}


}
