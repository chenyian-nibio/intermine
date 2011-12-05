<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="basic-table">
<h3>Protein domains</h3>

<c:choose>
  <c:when test="${empty hasProteinDomain}">
    <p style="margin: 10px;">No domain found for this protein.</p>
  </c:when>
  <c:otherwise>
    <table class="lookupReport" cellspacing="5" cellpadding="0"><tr><td>
      <div style="width: 800px; overflow-x: auto;">
	    <img style="border: 1px solid #ccc" title="Protein Analysis Result"
	         src="<html:rewrite action="/proteinAnalysisResultRenderer.do?object=${proteinId}"/>"/>	    
      </div>
    </td></tr></table>
  </c:otherwise>
</c:choose>
