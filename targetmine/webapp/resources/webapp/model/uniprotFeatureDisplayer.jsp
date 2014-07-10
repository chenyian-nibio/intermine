<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<div class="basic-table">
<h3>UniProt Features</h3>

  	<c:choose>
	  	<c:when test="${!empty featureMap}">
		    <table>
		    <c:forEach items="${featureMap}" var="parentEntry">
		      <c:set var="parentTerm" value="${parentEntry.key}" />
		        <thead>
		        	<tr><th colspan="4">${parentTerm}</th></tr>
		        </thead>
		        <tbody>
			      <tr>
			          <c:forEach items="${parentEntry.value}" var="entry">
			            <tr>
			              <td>${entry.begin}
			              </td>
			              <td>${entry.end}
			              </td>
			              <td>${entry.type}
			              </td>
			              <td>${entry.description}
			              </td>
			            </tr>
			          </c:forEach>
			      </tr>
		        </tbody>
		    </c:forEach>
		    </table>
		</c:when>
		<c:otherwise>
			<p style="font-style:italic;">No results</p>
		</c:otherwise>
	</c:choose>
</div>
	