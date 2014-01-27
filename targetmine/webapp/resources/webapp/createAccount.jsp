<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>

<!-- createAccount.jsp -->
<html:xhtml/>
<div class="body" align="center">
<im:boxarea stylename="plainbox" fixedWidth="60%">
  <html:form action="/createAccountAction">
      <b><i><fmt:message key="createAccount.privacy"/></i></b>
    <p/>
    <table>
      <tr>
        <td><fmt:message key="createAccount.username"/></td>
        <td><html:text property="username"/><br/></td>
      </tr>
      <tr>
        <td><fmt:message key="createAccount.password"/></td>
        <td><html:password property="password"/><br/></td>
      </tr>
      <tr>
        <td><fmt:message key="createAccount.password2"/></td>
        <td><html:password property="password2"/><br/></td>
      </tr>
      <tr>
      <c:if test="${!empty WEB_PROPERTIES['mail.mailing-list']}">
        <td>&nbsp;</td>
        <td><html:checkbox property="mailinglist"/><i>&nbsp;<fmt:message key="createAccount.mailinglist"/></i></td>
      </c:if>
      </tr>
    </table>

    <table style="width: 100%; background: #FFF; margin: 10px 0px; padding: 10px; border: 1px solid black;"><tr><td>
    <h3 style="text-align: center; margin: 10px 0px;">TargetMine service terms and conditions</h3>
    <ol>
    	<li style="margin: 5px">We may remove your account, if we determine that you consume excessive network capacity and/or system resources, and adversely affect our ability to provide the TargetMine service (the "Service") to other users.</li>
		<li style="margin: 5px">We reserve the right to suspend or discontinue the Service without prior notice to you, for emergency maintenance or repairs of our computer system, or in the event of circumstances beyond our control.</li>
		<li style="margin: 5px">Under no circumstances, are we liable for any damages arising from the unavailability of the Service, removal of your account, loss of uploaded or saved data, or any use, misuse or reliance of the Service.</li>
	</ol>
	</td></tr>
    </table>

    <p style="text-align: center"><html:submit property="action"><fmt:message key="createAccount.createAccount"/></html:submit></p>

  </html:form>
</im:boxarea>
</div>
<!-- /createAccount.jsp -->
