<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<tiles:importAttribute/>

<!-- pagenotfound.jsp -->
<html:xhtml/>

<div class="body pagenotfound">
  <h1>Oops, Page Not Found</h1>

  <p>The page you attempted to access does not exist. Try...</p>
  <ul>
    <li>going to the home page</li>
    <li>using the quicksearch</li>
    <li>or Contact us at ${WEB_PROPERTIES['error.support']}</li>
  </ul>
</div>
<img border="0" src="/${WEB_PROPERTIES['webapp.path']}/model/images/oops.png" title="Sorry..."/>

<!-- /pagenotfound.jsp -->
