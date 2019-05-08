<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<%-- Verify that there isnt an empty collection of data --%>
<c:choose>
  <c:when test="${empty data}">
    <h3>No data to visualize</h3>
  </c:when>

  <c:otherwise>
  </c:otherwise>

</div>


<c:forEach var = "counter" begin = "1" end = "10" step = "1" >

         <c:out value = "${counter-5}"/></br>
         <%-- <% System.out.println( "counter = " + pageContext.findAttribute("counter") ); %> --%>
</c:forEach>

<script>
  console.log("data", "${data}");
  // console.log("originalId", "${originalId}");
  // console.log("name", "${name}");
  // console.log('inchiKey', "${inchiKey}");
  // console.log('casRegistryNumber', "${casRegistryNumber}");
  // console.log('targetProteins', "${targetProteins}");
</script>

<div>
  <!-- left column  -->
  <svg id='canvas' >
  </svg>
  <!-- right column -->
  <div style='flex-direction: column; background-color: lightblue; width: 20%;'>

    <!-- Controls that allow the user to dynamically make (in)visible
      different columns from the bottom axis -->
    <div id='axis-div' style='flex-direction: column;'>
      <!-- Uncomment to make available the selection of the bottom Axis -->
      <!-- <label for='axisSelect'>Bottom Axis:</label>
      <select id='axis-select' onchange='app.update(event)' disabled>
        <option value=undefined>Select...</option>
      </select> -->
      <table id='axis-table'></table>
    </div>

    <!-- Controls that allow the user to choose the column within the dataset
      used to display color, and to make (in)visible data points associated
      to (a series of) specific color in the scale -->
    <div id='color-div' style='flex-direction: column;'>
      <label for='color-select'>Color based on:</label>
      <select id='color-select' onchange='app.updateColorScale(event)'>
        <option value=undefined>Select...</option>
      </select>
      <table id='color-table'></table>
    </div>

    <!-- Controls that allow the user to choose the column within the dataset
      used to define the shape of the points displayed, and to make
      (in)visible data points associated to (a series of) specific shape in
      the scale -->
    <div id='shape-div' style='flex-direction: column;'>
      <label for='shape-select'>Shape based on:</label>
      <select id='shape-select' onchange='app.updateShapeScale(event)'>
        <option value=undefined>Select...</option>
      </select>
      <table id='shape-table'></table>
    </div>

  </div>
</div>
