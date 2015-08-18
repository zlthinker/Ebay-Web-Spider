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

public class StartAllSpiderObject extends HttpServlet{
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public StartAllSpiderObject() {
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

		ConsoleCommandBean result=ConsoleCommandManager.getManager().handleCommand(
				new ConsoleCommandBean(AbstractConsoleCommandHandler.ConsoleCommand.STARTALL,
				SpiderStatusManager.getSpiderClassBean(classname)),"");
		if(result!=null&&result.getCommand()==AbstractConsoleCommandHandler.ConsoleCommand.STARTALLSUCCESS){
			request.setAttribute("success", "启动所有爬虫对象成功！");
			request.getRequestDispatcher("success.jsp").forward(request, response);
			return;
		}
		
		if(result==null||result.getCommand()!=AbstractConsoleCommandHandler.ConsoleCommand.STARTALLSUCCESS){
			request.setAttribute("error", "启动所有爬虫对象出错！"+result);
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}
}
