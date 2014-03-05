<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-1-transitional.dtd">
<html>
<head>
<!-- for google webmaster -->
	<meta name="google-site-verification" content="" />
<!-- for yahoo -->
	<META name="y_key" content="" />
<!-- for microsoft -->
	<meta name="msvalidate.01" content="" />
	<META NAME="ROBOTS" CONTENT="NOFOLLOW"/>
<!-- htmlHead.jsp -->
	<link href="${WEB_PROPERTIES['project.sitePrefix']}/rss/targetmine-notice.xml" rel="alternate" type="application/rss+xml" title="TargetMine | News" />

    <link rel="stylesheet" type="text/css" href="/targetmine/css/inlineTagEditor.css"/>
    <link rel="stylesheet" type="text/css" href="/targetmine/css/resultstables.css"/>

	<script type="text/javascript" src="/targetmine/js/jquery-1.7.js"></script>
	<script type="text/javascript" src="http://cdn.intermine.org/js/underscore.js/1.3.3/underscore-min.js"></script>
	<script type="text/javascript" src="http://cdn.intermine.org/js/backbone.js/0.9.2/backbone-min.js"></script>
	<script type="text/javascript" src="http://cdn.intermine.org/js/intermine/imjs/intermine-1.01.00/imjs.js"></script>
		
	<meta content="microarray, bioinformatics, genomics, drug discovery, target discovery" name="keywords"/>
	<meta content="Integrated queryable database for target discovery" name="description"/>
	<meta content="text/html; charset=iso-8859-1" http-equiv="Content-Type"/>

	<title>TargetMine: Data Sources</title>

<!-- this is here because it needs to be higher priority than anything else imported -->
	<link rel="stylesheet" type="text/css" href="/targetmine/css/webapp.css"/>
	<link rel="stylesheet" type="text/css" href="/targetmine/themes/grey/theme.css"/>

	<link href="http://cdn.intermine.org/css/bootstrap/2.0.3-prefixed/css/bootstrap.min.css" rel="stylesheet" />
	<link href="/targetmine/js/etc/tables.css" rel="stylesheet" />
	<link href="http://cdn.intermine.org/css/jquery-ui/1.8.19/jquery-ui-1.8.19.custom.css" rel="stylesheet" />
	<link href="http://cdn.intermine.org/css/google-code-prettify/latest/prettify.css" rel="stylesheet" />
	<link href="http://cdn.intermine.org/css/font-awesome/css/font-awesome.css" rel="stylesheet" />

		<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
		<link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
		<!-- DataTables -->
		<script src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>
		
        <script type="text/javascript">
		
		$(document).ready(function(){
			$('#dataset').css("visibility", "hidden");
			var service_url = "http://10.100.0.36/";
			$.ajax({
				url: service_url + "targetmine/service/query/results?query=%3Cquery+name%3D%22%22+model%3D%22genomic%22+view%3D%22DataSet.code+DataSet.name+DataSet.version+DataSet.dateType+DataSet.date+DataSet.url+DataSet.description%22+longDescription%3D%22%22+sortOrder%3D%22DataSet.name+asc%22%3E%3C%2Fquery%3E&format=json",
				dataType: "json",
				success: function(result) {
//					console.log(result);
					for (var i=0; i<result.results.length; i++) {
						var code = result.results[i][0];
						var name = result.results[i][1];
						var url = result.results[i][5];
						
						name = name.replace(/ data set/,"");
						$("#name_"+code).html('<a href="' + url + '">' + name + '</a>');
						$("#name_"+code).attr("title",result.results[i][6]);
						
						var version = result.results[i][2];
						if (version != null) {
							if (code=='GOAN') {
								version = version.replace(/,/g,"<br/>");
							} 
							$("#ver_"+code).html(version);
						} else {
							$("#ver_"+code).html("-");
						}
						
						var date = result.results[i][4];
						if (date != null) {
						// SCOP contains illegal date
							if (code=='SCOP') {
								$("#date_"+code).html("2009/6/-");
							} else {
								var d = new Date(date);
								var date_string = d.getFullYear() + "/" + (d.getMonth() + 1) + "/" + d.getDate();
								$("#date_"+code).html(date_string);
							}
							$("#date_"+code).attr("title",result.results[i][3])
						} else {
							$("#date_"+code).html("-");
						}
					}
					$('#dataset').dataTable();
					$('#dataset').css("visibility", "visible");
				},
				error: function() {
					$("#dataset").html("Error retrieving data from TargetMine");
				}
			})
		});
		</script>

	<style type="text/css">
		.im-query-actions { display: none; }
		.im-management-tools { display: none; }
		.im-title-part { color: white; }
		.im-th-button { color: #08c !important;}
			td.tick { font-weight: bold; color: red; text-align: center;}
			td.none { font-weight: none; color: green; text-align: center;}
	</style>

<!-- /htmlHead.jsp -->
</head>
<body class="fixed">
<div align="center" id="headercontainer">
  <!-- Header -->
  <div id="header">
    <a href="${WEB_PROPERTIES['project.sitePrefix']}" alt="Home" rel="NOFOLLOW">
       <img id="logo" src="model/images/new_tm_logo_1s.png" width="227px" height="50px" alt="Logo" /></a>
    <p id="version" style="margin-top: 30px"><fmt:message key="header.version"/> <c:out value="${WEB_PROPERTIES['project.releaseVersion']}" escapeXml="false"/></span>
    <p style="margin-top: 30px"><c:out value="${WEB_PROPERTIES['project.subTitle']}" escapeXml="false"/></p>
  </div>

<!-- Tab Menu -->
  <div id="menucontainer">
    <ul id="nav">
      <li id="home" >
        <a href="/${WEB_PROPERTIES['webapp.path']}/begin.do">
          <fmt:message key="menu.begin"/>
        </a>
      </li>
      <li id="templates">  
         <a href="/${WEB_PROPERTIES['webapp.path']}/templates.do">
          <fmt:message key="menu.templates"/>
        </a>
      </li>
      <li id="bags" >
        <a href="/${WEB_PROPERTIES['webapp.path']}/bag.do">
          <fmt:message key="menu.bag"/>
        </a>
      </li>
      <li id="query" >
        <a href="/${WEB_PROPERTIES['webapp.path']}/customQuery.do">
          <fmt:message key="menu.customQuery"/>&nbsp;
        </a>
      </li>
      <li id="category" class="activelink">
        <a href="/${WEB_PROPERTIES['webapp.path']}/dataCategories.do">
          <fmt:message key="menu.dataCategories"/>
        </a>
      </li>
      <li id="api"  >
        <a href="/${WEB_PROPERTIES['webapp.path']}/api.do">
          <fmt:message key="menu.api"/>
        </a>
      </li>
      <li id="mymine">  
        <a href="/${WEB_PROPERTIES['webapp.path']}/mymine.do">
          <span><fmt:message key="menu.mymine"/></span>
        </a>
      </li>
    </ul>
  <ul id="loginbar">
        <li><a href="#" onclick="showContactForm();return false;"><fmt:message key="feedback.link"/></a></li>
        <c:if test="${PROFILE.loggedIn}">
            <li>
              <!-- display (optionally trimmed) username -->
              <c:choose>
                <c:when test="${! empty PROVIDER}">
                  <c:choose>
                    <c:when test="${empty USERNAME || USERNAME == 'nullnull'}">
                      <c:set var="displayUserName" value="logged in with OpenID"/>
                    </c:when>
            <c:otherwise>
              <c:set var="displayUserName" value="${USERNAME}"/>
            </c:otherwise>
                  </c:choose>
        </c:when>
        <c:otherwise>
          <c:set var="displayUserName" value="${PROFILE.username}"/>
        </c:otherwise>
        </c:choose>
        <c:choose>
                <c:when test="${fn:length(displayUserName) > 25}">
                  <c:out value="${fn:substring(displayUserName,0,25)}"/>&hellip;
                </c:when>
                <c:otherwise>
                  <c:out value="${displayUserName}"/>
                </c:otherwise>
              </c:choose>
            </li>
        </c:if>
        <li class="last"><im:login/></li>
    </ul>
  </div>

  <!-- Logged in section -->
  <c:set var="loggedin" value="${PROFILE.loggedIn}"/>

  <!-- Submenu section -->
  <c:set var="itemList" value="bag:lists.upload.tab.title:upload:0 bag:lists.view.tab.title:view:0 api:api.perl.tab.title:perl:0 api:api.python.tab.title:python:0 api:api.ruby.tab.title:ruby:0 api:api.java.tab.title:java:0 mymine:mymine.bags.tab.title:lists:0 mymine:mymine.history.tab.title:history:0 mymine:mymine.savedqueries.tab.title:saved:1 mymine:mymine.savedtemplates.tab.title:templates:1" />
   <c:if test="${PROFILE.superuser}">
       <c:set var="itemList" value="${itemList} mymine:mymine.tracks.tab.title:tracks:1 mymine:mymine.users.tab.title:users:1 mymine:mymine.labels.tab.title:labels:0"></c:set>
   </c:if>
   <c:if test="${PROFILE.local}">
       <c:set var="itemList" value="${itemList} mymine:mymine.password.tab.title:password:1"/>
   </c:if>
    <c:set var="itemList" value="${itemList} mymine:mymine.account.tab.title:account:1"/>
  <fmt:message key="${pageName}.tab" var="tab" />
  <c:choose>
    <c:when test="${tab == 'mymine'}">
      <c:set var="styleClass" value="submenu_mymine" />
    </c:when>
    <c:otherwise>
      <c:set var="styleClass" value="submenu" />
    </c:otherwise>
  </c:choose>
  <c:set var="submenuid" value="submenu"/>
  <c:if test="${fixedLayout == true}">
    <c:set var="submenuid" value="${submenuid}fixed"/>
  </c:if>
  <div id="${submenuid}" class="${styleClass}">
    <div id="submenudiv">
      <div id="quicksearch">
        <tiles:insert name="quickSearch.tile">
          <tiles:put name="menuItem" value="true"/>
        </tiles:insert>
      </div>
        <ul id="submenulist">
        <c:set var="count" value="0"/>
        <c:set var="subtabName" value="subtab${pageName}" scope="request" />
        <c:forTokens items="${itemList}" delims=" " var="item" varStatus="counter">
          <c:set var="tabArray" value="${fn:split(item, ':')}" />
          <c:if test="${tabArray[0] == tab}">
          <c:choose>
            <c:when test="${((empty subtabs[subtabName] && count == 0)||(subtabs[subtabName] == tabArray[2])) && (tab == pageName)}">
              <%-- open li element --%>
        <li id="subactive_${tab}"
                <c:choose>
                  <c:when test="${count == 0}">class="first ${fn:replace(tabArray[1], ".", "")}"</c:when>
                  <c:otherwise>class="${fn:replace(tabArray[1], ".", "")}"</c:otherwise>
                </c:choose>
              > <%-- Close li element --%>
                <div><span><fmt:message key="${tabArray[1]}" /></span></div>
              </li>
            </c:when>
            <c:when test="${(tabArray[3] == '1') && (loggedin == false)}">
              <%-- open li --%>
              <li
                <c:choose>
                  <c:when test="${count == 0}">class="first ${fn:replace(tabArray[1], ".", "")}"</c:when>
                  <c:otherwise>class="${fn:replace(tabArray[1], ".", "")}"</c:otherwise>
                </c:choose>
        >
        <%-- close li --%>
        <div>
                <span onclick="alert('You need to log in'); return false;">
                  <fmt:message key="${tabArray[1]}"/>
                </span>
                </div>
              </li>
            </c:when>
            <c:otherwise>
              <%-- open li --%>
              <li
                <c:choose>
                  <c:when test="${count == 0}">class="first ${fn:replace(tabArray[1], ".", "")}"</c:when>
                  <c:otherwise>class="${fn:replace(tabArray[1], ".", "")}"</c:otherwise>
                </c:choose>
        >
        <%-- close li --%>
                <div>
                <a href="/${WEB_PROPERTIES['webapp.path']}/${tab}.do?subtab=${tabArray[2]}">
                  <fmt:message key="${tabArray[1]}"/>
                </a>
                </div>
              </li>
            </c:otherwise>
          </c:choose>
          <c:set var="count" value="${count+1}"/>
          </c:if>
        </c:forTokens>
        <!--
        <c:if test="${pageName == 'begin'}">
          <li>
          <div>
            <a href="${WEB_PROPERTIES['project.sitePrefix']}/what.shtml">What is ${WEB_PROPERTIES['project.title']}?</a>
          </div>
          </li>
        </c:if>
         -->
        </ul>
    </div>
  </div>

</div>
<!-- /headMenu.jsp -->

<div id="pagecontentcontainer" align="center" class="dataCategories-page">
    <div id="pagecontentmax">
    <div style="clear: both;"></div>
      <!-- errorMessagesContainers.jsp -->
<link rel="stylesheet" type="text/css" href="css/errorMessages.css"/>
<div>
    <div class="topBar errors" id="error_msg" style="display:none">
    	<a onclick="javascript:jQuery('#error_msg').hide('slow');return false" href="#">Hide</a>
    </div>
    <!-- the fail class is added on list analysis search results -->
    <div class="topBar messages " id="msg" style="display:none">
    	<a onclick="javascript:jQuery('#msg').hide('slow');return false" href="#">Hide</a>
    </div>
    <div class="topBar lookupReport" id="lookup_msg" style="display:none">
    	<a onclick="javascript:jQuery('#lookup_msg').hide('slow');return false" href="#">Hide</a>
    </div>
    <noscript>
      <div class="topBar errors">
        <p>Your browser does not have JavaScript enabled. Some parts of this website may require JavaScript to function correctly</p>
      </div>
      <br/>
    </noscript>
</div>
<!-- /errorMessagesContainers.jsp -->
<!-- contextHelp.jsp -->
<div id="ctxHelpDiv"
    style="display:none"
>
  <div class="topBar info">
    <a href="#" onclick="javascript:jQuery('#ctxHelpDiv').hide('slow');return false">Close</a>
    <div id="ctxHelpTxt"></div>
  </div>
</div><!-- hints.jsp -->
<!-- /hints.jsp -->
<!-- dataCategories -->

	<div style="width: 90%">
	<table id="dataset" class="display">
		<thead>
			<tr>
				<th>Data Source</th>
				<th style="width: 100px;">Version</th>
				<th style="width: 100px;">Date</th>
				<th>Protein interaction</th>
				<th>Transcription factor</th>
				<th>Biological pathway</th>
				<th>Compound interaction</th>
				<th>Disease association</th>
				<th>Protein structure</th>
				<th>miRNA interaction</th>
			</tr>
		</thead>
		<tbody>
			<tr><td id="name_AMAD">Amadeus</td><td id="ver_AMAD">-</td><td id="date_AMAD">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_BGRD">BioGRID interaction data set</td><td id="ver_BGRD">-</td><td id="date_BGRD">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_CATH">CATH</td><td id="ver_CATH">-</td><td id="date_CATH">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td></tr>
			<tr><td id="name_CHBI">ChEBI</td><td id="ver_CHBI">-</td><td id="date_CHBI">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_CMBL">ChEMBL</td><td id="ver_CMBL">-</td><td id="date_CMBL">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_DOAN">DO Annotation</td><td id="ver_DOAN">-</td><td id="date_DOAN">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_DGBK">DrugBank</td><td id="ver_DGBK">-</td><td id="date_DGBK">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_DRGB">DrugEBIlity</td><td id="ver_DRGB">-</td><td id="date_DRGB">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">?</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_GENE">ENZYME</td><td id="ver_GENE">-</td><td id="date_GENE">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_ENZY">Entrez Gene</td><td id="ver_ENZY">-</td><td id="date_ENZY">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_GN3D">Gene3D</td><td id="ver_GN3D">-</td><td id="date_GN3D">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td></tr>
			<tr><td id="name_GWAS">Genome-Wide Association Studies</td><td id="ver_GWAS">-</td><td id="date_GWAS">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_INTP">InterPro data set</td><td id="ver_INTP">-</td><td id="date_INTP">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_KORT">KEGG orthologues data set</td><td id="ver_KORT">-</td><td id="date_KORT">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_KEGG">KEGG pathways data set</td><td id="ver_KEGG">-</td><td id="date_KEGG">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_LGEX">Ligand Expo</td><td id="ver_LGEX">-</td><td id="date_LGEX">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td></tr>
			<tr><td id="name_NCIP">NCI Pathway Interaction Database</td><td id="ver_NCIP">-</td><td id="date_NCIP">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_AFFY">NetAffx Annotation Files</td><td id="ver_AFFY">-</td><td id="date_AFFY">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_OREG">ORegAnno</td><td id="ver_OREG">-</td><td id="date_OREG">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_PPIV">PPI view</td><td id="ver_PPIV">-</td><td id="date_PPIV">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_PBCM">PubChem</td><td id="ver_PBCM">-</td><td id="date_PBCM">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_REAC">Reactome data set</td><td id="ver_REAC">-</td><td id="date_REAC">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_SCOP">SCOP</td><td id="ver_SCOP">-</td><td id="date_SCOP">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td></tr>
			<tr><td id="name_SIFT">SIFTS</td><td id="ver_SIFT">-</td><td id="date_SIFT">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td></tr>
			<tr><td id="name_STCH">STITCH</td><td id="ver_STCH">-</td><td id="date_STCH">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_SWPR">Swiss-Prot data set</td><td id="ver_SWPR">-</td><td id="date_SWPR">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_TRMB">TrEMBL data set</td><td id="ver_TRMB">-</td><td id="date_TRMB">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_GOAN">UniProt-GOA</td><td id="ver_GOAN">-</td><td id="date_GOAN">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_IREF">iRefIndex interaction data set</td><td id="ver_IREF">-</td><td id="date_IREF">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_MIRB">miRBase</td><td id="ver_MIRB">-</td><td id="date_MIRB">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_MITB">miRTarBase</td><td id="ver_MITB">-</td><td id="date_MITB">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td></tr>
			<tr><td id="name_WPDB">wwPDB</td><td id="ver_WPDB">-</td><td id="date_WPDB">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td></tr>
			<!-- to be removed -->
			<tr><td id="name_OMIM">OMIM</td><td id="ver_OMIM">-</td><td id="date_OMIM">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick">V</td><td class="none">-</td><td class="none">-</td></tr>
		</tbody>
	</table>
	</div>


</div>
    <br/>
	<div class="body" align="center" style="clear:both">
	    <!-- funding -->
	    <div id="funding-footer">
	        TargetMine is developed by <a href="http://mizuguchilab.org/" target="_new">The Mizuguchi Laboratory</a> at <a href="http://www.nibio.go.jp/" target="_new" title="National Institute of Biomedical Innovation"><img src="/targetmine/model/images/logo_nibio_full.png" border="0" alt="NIBIO_logo"></a><br/>
	        <br/>
	        <!-- powered -->
	        <p>Powered by</p>
	        <a target="new" href="http://intermine.org" title="InterMine">
	            <img src="images/icons/intermine-footer-logo.png" alt="InterMine logo" />
	        </a>
	    </div>
	</div>
</div>
</body>
</html>