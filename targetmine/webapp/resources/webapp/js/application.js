'use strict';

/* global variable for the application */
var app;

/**
 *
 * @type {HTMLElement}
 * @property {HTMLElement} _display The svg component where all the drawing will
 * take place
 * @type {CompoundGraph}
 * @property {CompoundGraph} _graph The graph element we are to visualize on the
 * display
 */

class Application{

  constructor(){
    this._display = d3.select('svg');
    this._graph = new CompoundGraph('dummy name');
  }

  /**
   * Load a data file
   * Read the contents of a data file and use to initialize a visualization,
   * using the concentration column for the left axis, and the type column for
   * the bottom axis.
   * TO-DO a third (right) axis should be included in the visualization when 2
   * (or more) concentration columns are available in the dataset
   */
  onLoadFile(){
    /* retrieve the file to load */
    let file = d3.select('#loadFile').node();
    /* nothing to do if there isnt a file to read */
    if( file.files.length === 0 ) return;
    let url = URL.createObjectURL(file.files[0]);
    let rqst = new XMLHttpRequest();
    rqst.open('GET', url);
    rqst.onreadystatechange = function(){
      /* process once the loading of data has finished */
      if( rqst.readyState == 4 && rqst.status == 200 ){
        /* parse the contents of the file as an TSV table and add it to the graph */
        console.log(rqst.responseText[0]);
        this._graph.setData(d3.tsvParse(rqst.responseText, d3.autoType));
        /* initialize axis selection */
        this._initDisplay('Compound > Target Proteins > Activities > Concentration(nM)');
      }
    }.bind(this)
    rqst.send();
  }

  /**
   * Initialize a graph display
   * Whenever a new data file is loaded, a graph display is initialized using a
   * default Y-axis.
   * Notice that the first step required to display a graph is to clear whatever
   * content the display area might have already have.
   * @param {string} col the name of the column used as default Y-axis for the
   * graph
   */
  _initDisplay(col){
    /* get the area available for drawing */
    let canvas = d3.select('svg#canvas');
    let width = parseInt(canvas.style('width').replace('px',''))*.8;
    let height = parseInt(canvas.style('height').replace('px',''));
    let vertPadding = width *.05; // vertical padding 5% on each side
    let horzPadding = height * .05; // horizontal padding 5% on each side

    /* init options for all select components of the DOM */
    let select = d3.selectAll('select')
      .selectAll('option').remove()
      .data(this._graph.getDataColumns())
      .enter().append('option')
        .attr('value', function(d){ return d; })
        .text(function(d){ return d; })
      ;

    /* generate a default x Axis */
    let xAxis = this._graph.createAxis('Compound > Target Proteins > Activities > Type', 0);
    /* remove any previous one */
    d3.select('#bottom-axis')
      .remove();
    /* and add the new one to the display */
    canvas.append('g')
      .attr('id', 'bottom-axis')
      .attr('transform', 'translate('+horzPadding+', '+(height-vertPadding)+')')
      .call(xAxis)
      ;

    /* generate a default y Axis */
    let yAxis = this._graph.createAxis(col, 1);
    /* remove any previous axis */
    d3.select('#left-axis')
      .remove();
    /* and add the new one to the display */
    canvas.append("g")
      .attr('id', 'left-axis')
      .attr('transform', 'translate('+(1.25*horzPadding)+', '+vertPadding+')')
      .call(yAxis)
      ;

    /* set default colors */
    document.getElementById('color-select').dispatchEvent(new Event('change'));

    /* set default shapes */
    document.getElementById('shape-select').dispatchEvent(new Event('change'));

    /* plot the default values */
    this.plot();//'Compound > Target Proteins > Activities > Type', col);
  }

  /**
   * Update the graph for a user selected obsice coordinate
   *
   * @type {Event}
   * @param {Event} evt The change event triggered by user selection
   */
  update(evt){
    /* get the column selected by the user */
    let col = evt.target.value;
    /* remove previous elements from the visualization */
    d3.select('svg').select('#bottom-axis')
        .remove();
    /* generate a new axis based on the selection made by the user */
    let axis = this._graph.createAxis(col,0);
    d3.select('svg')
      .append("g")
      .attr('id', 'bottom-axis')
      .attr('transform', 'translate('+horzPadding+', '+(height-vertPadding)+')')
      .call(axis)
      ;
    /* plot the corresponding values */
    this.plot(col, 'Compound > Target Proteins > Activities > Concentration(nM)');
  }

  /**
   * Display the legend for color use in the graph
   *
   * @type {Event}
   * @param {event} evt The change event that triggers the change in the color
   * scale used for the display of variables
   */
  updateColorScale(evt){
    let self = this;
    /* get the column selected by the user */
    let col = evt.target.value;
    /* update the colors used for the given column */
    let colors = this._graph.setColors(col);

    /* select the table where we will add elements and remove any previous data */
    let table = document.getElementById('color-table');
    while(table.lastChild){
      table.removeChild(table.lastChild);
    }

    Object.keys(colors).forEach(function(k){
      let row = table.insertRow(-1);

      let check = row.insertCell(-1);
      check.style.width = '10%';

      let inp = document.createElement('input');
      inp.className = 'color-checkbox';
      inp.setAttribute("type", "checkbox");
      inp.setAttribute('data-color', colors[k]);
      inp.checked = true;
      inp.addEventListener('change', function(){self.plot();});
      check.append(inp);

      let color = row.insertCell(-1);
      color.style.width = '10%';
      color.style.backgroundColor = colors[k];

      let label = row.insertCell(-1);
      label.setAttribute('id', 'color-label');
      label.innerHTML = k;
    });

    this.plot();
  }

  /**
   * Display the legend used for the different shapes in the graph
   *
   * @type {Event}
   * @param {event} evt The change event that triggers the use of shapes
   * according to the data column selected by the user
   */
  updateShapeScale(evt){
    /* get the column selected by the user */
    let col = evt.target.value;
    /* update the list of shapes used for the given column */
    let shapes = this._graph.setShapes(col);

    /* select the table where we will add elements and remove any previous data */
    d3.select('#shape-table').selectAll('tr')
      .remove()
      ;

    /**
      add a new element to the scale for each individual shape used, each row
      is made of 3 different elements:
      1. checbox: allows the display/ hiding of a particular shape
      2. svg: a sample of the shape
      3. label: the value in the data for which the shape is used
    */
    //   /* the new row, at the end of the table */
    let rows = d3.select('#shape-table').selectAll('tr')
      .data(shapes)
      .enter().append('tr')
      ;

    /* 1. the checkbox element */
    let td = rows.append('td')
      .style('width', '10%')
      ;
    let cb = td.append('input')
      .classed('shape-checkbox', true)
      .attr('type', 'checkbox')
      .attr('checked', true)
      .attr('id', function(d){ return d.name; })
      .on('change', this.plot())
      ;


    td = rows.append('td')
      .style('width', '10%')

    let shp = td.append('svg')
      .style('height', '20px')
      .attr('fill', 'black')
      .append('path')
        .attr('transform', 'translate(10,10)')
        .attr('d', function(d){
          let symbol = d3.symbol()
            .size(50)
            .type(d.shape)
            ;
          return symbol();
        })
      ;


    td = rows.append('td')
      .text(function(d){ return d.name; })
      ;
    // /* redraw the graph using the newly selected shape options */
    this.plot();
  }


  /**
   *
   */
  plot( x = 'Compound > Target Proteins > Activities > Type',
        y = 'Compound > Target Proteins > Activities > Concentration(nM)'){
    /* get the area available for drawing */
    let mysvg = d3.selectAll('svg');
    let width = parseInt(mysvg.style('width').replace('px',''))*.8;
    let height = parseInt(mysvg.style('height').replace('px',''));
    let vertPadding = width *.05; // vertical padding 5% on each side
    let horzPadding = height * .05; // horizontal padding 5% on each side

    let color = document.getElementById('color-select').value;
    let coords = this._graph.getPoints(x, y, color);

    /* filter out the points that are hidden from the visualization */
    let visible = document.getElementsByClassName('color-checkbox');
    for( let i = 0; i < visible.length; ++i ){
      let cb = visible.item(i);
      if (cb.checked === false){
        coords = coords.filter(function(ele){
          if( ele.color !== cb.dataset.color)
            return ele;
        });
      }
    };

    /* delete any previous points */
    d3.select('svg').select('#points')
      .remove()
    ;

    /* add a <g> element to group the drawing of the data points */
    d3.select('svg')
      .append('g')
      .attr('id', 'points')
      .attr('transform', 'translate('+horzPadding+','+vertPadding+')')
      ;

    /* for each data point, generate a group where we can add multiple svg
     * elements */
    let points = d3.select('#points').selectAll('g')
      .data(coords)
    let point = points.enter().append('g')
        .attr('class', 'data-point')
        .append('path')
          .attr('d', function(d){
            let symbol = d3.symbol()
              .size(50)
              .type(d.shape)
              ;
            return symbol();
          })
          .attr('fill', function(d){return d.color;})
          .attr('transform', function(d){ return 'translate('+d.x+','+d.y+')'; })
          .append("svg:title")
            .text(function(d){ return d.label; })
      ;
  }

}
