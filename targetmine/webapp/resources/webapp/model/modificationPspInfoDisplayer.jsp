<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<c:set var="modification" value="${reportObject.object}"/>
<c:forEach var="dataSet" items="${modification.dataSets}">
	<c:if test="${dataSet.name == 'PhosphoSitePlus'}">
		<div style="font-size: 8px; margin: 12px 0px;">
			This page contains the data derived from <a href="http://www.phosphosite.org/uniprotAccAction?id=${modification.protein.primaryAccession}" target="_blank">PhosphoSitePlus&reg; (PSP)</a>. The PSP data is not for commercial use.
		</div>
	</c:if> 
</c:forEach>

