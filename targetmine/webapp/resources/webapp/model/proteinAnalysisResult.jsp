<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<c:choose>
  <c:when test="${empty object.proteinDomainRegions}">
    <p>No domain found for this protein</p>
  </c:when>
  <c:otherwise>
    <table class="lookupReport" cellspacing="5" cellpadding="0"><tr><td>
	    <img style="border: 1px solid #ccc" title="Protein Analysis Result"
	         src="<html:rewrite action="/proteinAnalysisResultRenderer?object=${object.id}"/>"/>	    
    </td></tr></table>
  </c:otherwise>
</c:choose>
