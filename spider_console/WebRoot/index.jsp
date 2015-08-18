<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.*"%>
<%@page import="org.epiclouds.spiders.spiderbean.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>所有爬虫类</title>
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
		</div>
		<% 
			}
			List<SpiderClassBean> rr=SpiderStatusManager.getAllSpiderClassBeans();
		%>


		<fieldset>
			爬虫类数量：<%=rr.size()%></fieldset>
		<table class="table table-bordered">
			<thead>
				<tr class="success">
					<td>名称</td>
					<td>描述</td>
					<td>队列名称</td>
					<td>删除</td>
					<td>查看</td>
					<td>增加</td>
					<td>配置</td>
				</tr>
			</thead>
			<tbody>
				<%
			for(SpiderClassBean eb:rr){
		%>
				<tr>
					<td><%=eb.getName()%></td>
					<td><%=eb.getDesc() %></td>
					<td><%=eb.getQueue_name() %></td>
					<td>
						<a class="btn btn-large btn-danger"
						href="removeSpiderClass?classname=<%=eb.getName()%>">删除</a>
					</td>
					<td>
						<a class="btn btn-large btn-primary"
						href="viewSpiderClass.jsp?name=<%=eb.getName()%>">查看</a>
					</td>
					<td>
						<a class="btn btn-large btn-primary"
						href="addSpiderObject.jsp?name=<%=eb.getName()%>">增加</a>
					</td>
					<td>
						<a class="btn btn-large btn-primary"
						href="modifySpiderClassConfig.jsp?name=<%=eb.getName()%>">配置</a>
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
