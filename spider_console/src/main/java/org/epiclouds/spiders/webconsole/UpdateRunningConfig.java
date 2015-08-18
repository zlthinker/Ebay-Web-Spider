package org.epiclouds.spiders.webconsole;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.command.abstracts.SpiderClassConfigBean;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;
import org.epiclouds.spiders.util.ConsoleConfig;



/**
 * Servlet implementation class GetChart
 */

public class UpdateRunningConfig extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public UpdateRunningConfig() {
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
		HttpSession s=request.getSession();
		if(s.getAttribute("user")==null){
			response.sendRedirect("login.jsp");
			return;
		}
		try{
			String classname=request.getParameter("spiderClassName");
			if(classname==null||"".equals(classname)){
				request.setAttribute("error", "爬虫类不能为空！");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			String para=request.getParameter("configName");
			if(para==null||"".equals(para)){
				request.setAttribute("error", "配置的名称"+para+"不能为空！");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			String value=request.getParameter("value");
			if(value==null||"".equals(value)){
				request.setAttribute("error", "配置的值"+value+"不能为空！");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			SpiderClassConfigBean config=new SpiderClassConfigBean();
			config.setName(para);
			config.setValue(value);
			
			ConsoleCommandBean result=ConsoleCommandManager.getManager().handleCommand(
					new ConsoleCommandBean(AbstractConsoleCommandHandler.ConsoleCommand.MODIFYCONFIG,
					SpiderStatusManager.getSpiderClassBean(classname)),config);
			if(result!=null&&result.getCommand()==AbstractConsoleCommandHandler.ConsoleCommand.MODIFYCONFIGSUCCESS){
				request.setAttribute("success", "修改爬虫类"+classname+"配置成功！");
				request.getRequestDispatcher("success.jsp").forward(request, response);
				return;
			}
			
			if(result==null||result.getCommand()!=AbstractConsoleCommandHandler.ConsoleCommand.MODIFYCONFIGSUCCESS){
				request.setAttribute("error", "修改爬虫类出错！"+result);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
		}catch(Exception e){
			request.setAttribute("error", "更新参数失败！"+e.toString());
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}


}
