<%@page import="org.epiclouds.spiders.util.ConsoleConfig"%>
<%@page import="org.epiclouds.spiders.spiderbean.util.SpiderStatusManager"%>
<%@page import="org.epiclouds.spiders.command.abstracts.*"%>
<%@page import="org.epiclouds.spiders.command.impl.*"%>
<%@page import="org.epiclouds.spiders.command.abstracts.ConsoleCommandManager"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.*"%>
<%@page import="com.alibaba.fastjson.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>查看爬虫类<%=request.getParameter("name") %>配置</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<!-- Bootstrap -->
<link href="css/bootstrap.css" rel="stylesheet" media="screen">
</head>
<body>

	<%@include file="navbar.html"%>


	<div class="container-fluid">

		<!-- Main hero unit for a primary marketing message or call to action -->
		<div class="hero-unit"></div>
		<%
			HttpSession s=request.getSession();
			if(s.getAttribute("user")==null){
				response.sendRedirect("login.jsp");
				return;
			}
			String error = (String)request.getAttribute("error");
			String su = (String)request.getAttribute("success");
			if (error != null) {
		%>
		<div class="alert alert-error">
			<button type="button" class="close" data-dismiss="alert">&times;
			</button>
			<h4>错误信息</h4>
			<%=error%>
		</div>
		<%
			}
			if (su != null) {
				%>
		<div class="alert alert-success">
			<button type="button" class="close" data-dismiss="alert">&times;
			</button>
			<h4>成功信息</h4>
			<%=su%>
			<%} %>
		</div>
		<fieldset>爬虫类<%=request.getParameter("name") %>配置</fieldset>
		<%
			String name=request.getParameter("name");
			ConsoleCommandBean bean2=new ConsoleCommandBean(
					AbstractConsoleCommandHandler.ConsoleCommand.GETCONFIG,SpiderStatusManager.getSpiderClassBean(name));
			
			ConsoleCommandBean result=(ConsoleCommandBean)ConsoleCommandManager.getManager().handleCommand(bean2);
			if(result==null){
				out.write("Get the result is null!");
				return;
			}
			if(result.getCommand()!=AbstractConsoleCommandHandler.ConsoleCommand.GETCONFIGSUCCESS){
				out.write("Get the result is failure!"+result);
				return;
			}
			List<SpiderClassConfigBean> oo=JSONObject.parseArray(JSONObject.toJSONString(result.getOb()), SpiderClassConfigBean.class);
			if(oo==null){
				out.write("Get the result is null!");
				return;
			}
		%>

		<table class="table table-bordered">
			<thead>
				<tr class="success">
					<td>参数名称</td>
					<td>当前值</td>
					<td>解释</td>
					<td>运行时可修改</td>
					<td>更新</td>
				</tr>
			</thead>
			<tbody>
				<%
			
			for(SpiderClassConfigBean bean:oo){
			%>
				<tr>
					<td><%=bean.getName() %></td>
					<td><%=bean.getValue()%></td>
					<td><%=bean.getDesc()%></td>
					<td><%=bean.isIsrun()%></td>
					<td>
						<% 
					if(bean.isIsrun()){
				%>
						<form class="form-horizontal" id=1 action="updateRunningConfig"
							method="post">
							<input class="span2"  type="hidden"
								value="<%=name %>" name="spiderClassName">
							<input class="span2"  type="hidden"
								value="<%=bean.getName() %>" name="configName">
							<div class="input-append">
								<input class="span2" id="appendedInputButton" type="text"
									name="value"
									placeholder="<%=bean.getDesc()%>">
								<button class="btn" type="submit">更新</button>
							</div>
						</form> <%
					}else{
						out.write("启动参数，不能修改！");
					}
					%>
					</td>
				</tr>
				<%
				}
			%>

			</tbody>
		</table>


	</div>
	<%@include file="foot.html"%>
	<SCRIPT type="text/javascript">
		setactive("view");
	</SCRIPT>
</body>

</html>
