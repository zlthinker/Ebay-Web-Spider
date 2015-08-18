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
<title>增加<%=request.getParameter("name") %>爬虫</title>
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
		<fieldset>增加<%=request.getParameter("name") %>爬虫</fieldset>
		<%
			String name=request.getParameter("name");
			List<AddSpiderObjectBean> oo=SpiderStatusManager.getAddSpiderConfig(name);
			if(oo==null){
				if(oo==null){
					out.write("Get the result is null!");
					return;
				}
			}
		%>
		
		<form action="addSpiderObject" method="post" class="form-horizontal">
			<input type="hidden" name="spiderClassName" value="<%=name%>">
			<%
				for(AddSpiderObjectBean ab:oo){
			
			%>
			<div class="control-group">
				<label class="control-label" ><%=ab.getDesc() %></label>
				<div class="controls">
					<input type="text" id="<%=ab.getName() %>" placeholder="<%=ab.getDesc()%>"  name="<%=ab.getName() %>">
				</div>
			</div>
			<%
				}
			%>
			<div class="control-group">
				<div class="controls">
					<button type="submit" class="btn btn-large btn-primary">增加</button>
				</div>
			</div>
		</form>

	</div>
	<%@include file="foot.html"%>
	<SCRIPT type="text/javascript">
		setactive("view");
	</SCRIPT>
</body>

</html>
