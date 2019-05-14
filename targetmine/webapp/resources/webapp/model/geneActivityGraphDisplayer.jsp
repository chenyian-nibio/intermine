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
    <h3>Gene Activities</h3>
    <%-- Visualization Container --%>
    <div class='graphDisplayer'>
      <%-- Left Column of the Visualization (main display) --%>
      <svg class='graphDisplayer' id='canvas' viewbox='0 0 400 400'></svg>
      <%-- Right Column, reserved for visualization controls --%>
      <div class='rightColumn'>

        <%-- Choose the property used to display color, and to make (in)visible
        data points associated to specific colors in the scale  --%>
        <div id='color-div' style='flex-direction: column;'>
          <label for='color-select'>Color based on:</label>
          <select id='color-select'  onchange='app.updateColorScale(event.target.value)'>
            <option value=undefined>Select...</option>
          </select>
          <table id='color-table'><tbody></tbody></table>
        </div>

      </div> <%-- Right column --%>
    </div>

    <script>
      var graph = new CompoundGraph('${compound}');
      graph.initData('${data}');
      setTimeout(function(){
        graph.initXAxis('Activity Type');
        graph.initYAxis('Activity Concentration');
        graph.initColors('Activity Type');
        graph.plot();
      }, 0);
    </script>

  </c:otherwise>
</c:choose>

</div> <%-- collection-table --%>
