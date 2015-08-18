
<%@page import="org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand"%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="org.epiclouds.spiders.spiderbean.util.*"%>
<%@page import="org.epiclouds.spiders.command.abstracts.*"%>
<%@page import="org.epiclouds.spiders.command.impl.*"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.*"%>
<%@page import="com.alibaba.fastjson.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>爬虫类<%=request.getParameter("name") %>状态查看</title>
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
			String error = null;
			String su = null;
			String name=request.getParameter("name");
			ConsoleCommandBean cc=new ConsoleCommandBean(
					AbstractConsoleCommandHandler.ConsoleCommand.GETSPIDEROBJECTS,SpiderStatusManager.getSpiderClassBean(name));
			Object re=ConsoleCommandManager.getManager().handleCommand(cc);
			ConsoleCommandBean ccb=(ConsoleCommandBean)re;
			if(re==null||ccb.getCommand()!=AbstractConsoleCommandHandler.ConsoleCommand.GETSPIDEROBJECTSSUCCESS){
				error="获取爬虫对象错误(超时)！";
			}else{
				su="获取爬虫对象成功！";
			}
			if (error != null) {
		%>
		<div class="alert alert-error">
			<button type="button" class="close" data-dismiss="alert">&times;
			</button>
			<h4>错误信息</h4>
			<%=error%>
		</div>
		<%
			return;
			}
			if (su != null) {
				%>
		<div class="alert alert-success">
			<button type="button" class="close" data-dismiss="alert">&times;
			</button>
			<h4>成功信息</h4>
			<%=su%>
		</div>
		<% 
			
			}

			
			
			List<SpiderObjectBean> ll=JSONObject.parseArray(JSONObject.toJSONString(ccb.getOb()), 
					SpiderObjectBean.class);

		%>
		
		
						
		<a class="btn btn-large btn-primary"
				href="startAllSpiderObject?classname=<%=name%>">启动所有爬虫，这个过程暂时不可逆，慎重</a>


		<fieldset>所有爬虫对象：<%=ll.size() %></fieldset>
		<table class="table table-bordered">
			<thead>
				<tr class="success">
					<td>名称</td>
					<td>信息</td>
					<td>总数量</td>
					<td>已爬数量</td>
					<td>进度</td>
					<td>开始时间</td>
					<td>上次结束时间</td>
					<td>正在运行</td>
					<td>删除</td>
					<td>启动</td>
					<td>停止</td>
					<td>更新</td>
				</tr>
			</thead>
			<tbody>
				<%
					for(SpiderObjectBean sob:ll){
				%>
				<tr>
					<td><%=sob.getName() %>
					</td>
					<td><%=sob.getInfo() %>
					</td>
					<td><%=sob.getTotal_num() %></td>
					<td><%=sob.getSpided_num() %></td>
					<td>
						<div class="progress progress-info">
							<div class="bar" role="bar"
								style="width: <%=sob.getSpided_num()*100.0/(sob.getTotal_num()==0?1:sob.getTotal_num()) %>%;">
								<%=sob.getSpided_num()*100.0/(sob.getTotal_num()==0?1:sob.getTotal_num())%>%
							</div>
						</div>
					</td>
					<td><%=new DateTime(sob.getStart_time()).toString("yyyy-MM-dd HH:mm:ss") %></td>
					<td><%=new DateTime(sob.getFinish_time()).toString("yyyy-MM-dd HH:mm:ss") %></td>
					<td><%=sob.isIsrun() %></td>
					<td><a class="btn btn-large btn-danger"
						href="deleteSpiderObject?classname=<%=name%>&objectname=<%=sob.getName()%>">
						删除</a></td>
					<td>
					<%
						if(!sob.isIsrun()){
					%>
					<a class="btn btn-large btn-primary"
						href="startSpiderObject?classname=<%=name%>&objectname=<%=sob.getName()%>">立即开始</a>
						<%
						}else{
							out.write("正在运行!");
						}
						%>
						</td>
						
						<td>
					<%
						if(sob.isIsrun()){
					%>
					<a class="btn btn-large btn-primary"
						href="stopSpiderObject?classname=<%=name%>&objectname=<%=sob.getName()%>">停止</a>
						<%
						}else{
							out.write("已经停止!");
						}
						%>
						</td>
					<td>	
						<a class="btn btn-large btn-primary"
							href="updateSpiderObject.jsp?name=<%=name%>&objectName=<%=sob.getName()%>">更新</a>

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
