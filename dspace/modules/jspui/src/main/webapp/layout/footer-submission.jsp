<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Footer for submission
  --%>

<%@ page import="java.io.*"  %>
<%
Integer userID = (Integer) session.getAttribute("dspace.current.user.id");
String str = userID+"";
String nameOfTextFile = "/tmp/"+session.getId()+".txt";
try {
    PrintWriter pw = new PrintWriter(new FileOutputStream(nameOfTextFile));
    pw.println(str);
    pw.close();
} catch(IOException e) {
   out.println(e.getMessage());
}
%>
<script type="text/javascript">
    var JSESSION = "<%= session.getId() %>";
</script>

<%-- <script type="text/javascript" src="<%= request.getContextPath() %>/js/urn-support.js"></script> --%>
<%-- <script type="text/javascript" src="<%= request.getContextPath() %>/static/js/getISSNAuthority.js"></script> --%>

<%@ include file="footer-default.jsp" %>