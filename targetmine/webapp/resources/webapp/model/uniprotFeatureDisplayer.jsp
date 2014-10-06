<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">
<h3>UniProt Features</h3>

  	<c:choose>
	  	<c:when test="${!empty featureMap}">
		    <table>
				<c:forEach items="${subsectionOrder}" var="parentEntry">
					<c:if test="${!empty featureMap[parentEntry]}">
		        		<thead>
		        			<tr><th colspan="4"><b>${parentEntry}</b></th></tr>
		        		</thead>
		        		<tbody>
			          	<c:forEach items="${featureMap[parentEntry]}" var="entry">
			            	<tr>
			            		<c:choose>
			            			<c:when test="${entry.begin == entry.end}">
			            				<td>${entry.begin}</td>
			            				<td>1</td>
			            			</c:when>
			            			<c:otherwise>
			            				<c:choose>
			            					<c:when test="${entry.type == 'disulfide bond'}">
			            						<td colspan="2">${entry.begin} &harr; ${entry.end}</td>
				    	        			</c:when>
					            			<c:otherwise>
					            				<td>${entry.begin} - ${entry.end}</td>
			            						<c:set var="len" value="${entry.end - entry.begin + 1}" />
			            						<td>${len}</td>
					            			</c:otherwise>
					            		</c:choose>
			            			</c:otherwise>
			            		</c:choose>
			              		<td>${entry.type}</td>
			              		<td>${entry.description}</td>
			            	</tr>
						</c:forEach>
						</tbody>
		    		</c:if>
				</c:forEach>
		    </table>
		</c:when>
		<c:otherwise>
			<p style="font-style:italic;">No features</p>
		</c:otherwise>
	</c:choose>
</div>
	