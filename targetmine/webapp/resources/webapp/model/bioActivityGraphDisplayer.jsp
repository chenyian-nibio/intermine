<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<!-- Verify that there isnt an empty collection of data -->
<c:choose>
  <c:when test="${empty data}">
    <h3>No data to visualize</h3>
  </c:when>

  <c:otherwise>
    <h3>Bio-Activities Graph</h3>
    <!-- Visualization Container -->
    <div class='BioActivityGraph'>
      <!-- Left Column of the Visualization (main display) -->
      <svg class='BioActivityGraphSVG' id='canvas' viewbox='0 0 400 400'></svg>
      <!-- Right Column, reserved for visualization controls -->
      <div class='rightColumn'>
        <!-- Choose the property used to display color, and to make (in)visible
        data points associated to specific colors in the scale -->
        <div id='color-div' style='flex-direction: column;'>
          <label for='color-select'>Color based on:</label>
          <br />
          <select id='color-select'>
            <option value=undefined>Select...</option>
          </select>
          <table id='color-table'><tbody></tbody></table>
        </div>
        <!-- Choose the property used to map display shape, and to make (in)visible
        data points associated to specific shapes in the scale -->
        <div id='shape-div' style='flex-direction: column;'>
          <label for='shape-select'>Shape based on:</label>
          <br />
          <select id='shape-select'>
            <option value=undefined>Select...</option>
          </select>
          <div class='flex-table' id='shape-table'></div>
        </div>
      </div>
    </div>

    <script type='text/javascript'>
      var graph = new BioActivityGraph('${compound}');
      <!-- pass the information fetched from Java to the JS code -->
      graph.loadData('${data}');
    </script>

  </c:otherwise>
</c:choose>

</div>
