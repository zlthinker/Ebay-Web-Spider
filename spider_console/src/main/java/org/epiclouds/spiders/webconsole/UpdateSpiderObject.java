package org.epiclouds.spiders.webconsole;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;




/**
 * Servlet implementation class GetChart
 */

public class UpdateSpiderObject extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public UpdateSpiderObject() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession().getAttribute("user")==null){
			request.getRequestDispatcher("login.jsp").forward(request, response);
			return;
		}
		String classname=request.getParameter("spiderClassName");
		if(classname==null||"".equals(classname)){
			request.setAttribute("error", "爬虫类不能为空！");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
		String objectname=request.getParameter("spiderObjectName");
		if(objectname==null||"".equals(objectname)){
			request.setAttribute("error", "爬虫对象名称不能为空！");
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
		Enumeration<String> names=request.getParameterNames();
		List<AddSpiderObjectBean> values=new LinkedList<AddSpiderObjectBean>();
		while(names.hasMoreElements()){
			String key=names.nextElement();
			if(!"spiderObjectName".equals(key)&&!"spiderClassName".equals(key)&&request.getParameter(key)!=null&&!"".equals(request.getParameter(key))){
				AddSpiderObjectBean aob=new AddSpiderObjectBean();
				aob.setName(key);
				aob.setValue(request.getParameter(key));
				values.add(aob);
			}
		}
		AddSpiderObjectBean ids=new AddSpiderObjectBean();
		ids.setName("id");
		ids.setValue(objectname);
		values.add(ids);
		ConsoleCommandBean result=ConsoleCommandManager.getManager().handleCommand(
				new ConsoleCommandBean(AbstractConsoleCommandHandler.ConsoleCommand.UPDATE,
				SpiderStatusManager.getSpiderClassBean(classname)),values);
		if(result!=null&&result.getCommand()==AbstractConsoleCommandHandler.ConsoleCommand.UPDATESUCCESS){
			request.setAttribute("success", "更新爬虫对象成功！");
			request.getRequestDispatcher("success.jsp").forward(request, response);
			return;
		}
		
		if(result==null||result.getCommand()!=AbstractConsoleCommandHandler.ConsoleCommand.UPDATESUCCESS){
			request.setAttribute("error", "更新爬虫对象出错！返回值："+result);
			request.getRequestDispatcher("error.jsp").forward(request, response);
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}


}
