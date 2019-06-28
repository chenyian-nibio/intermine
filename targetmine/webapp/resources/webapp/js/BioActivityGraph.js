'use strict';

var $jq = jQuery.noConflict();
// margins from the border of the canvas that wont be used for drawing
var margin = {top: 40, right: 40, bottom: 40, left: 40};
// available drawing space for the canvas
var width = 400 - margin.left - margin.right;
var height = 400 - margin.top - margin.bottom;

/**
 * @class CompoundGraph
 * @classdesc
 * @author
 * @version
 *
 */
class BioActivityGraph{

  constructor(name){
    this._name = name;
    this._data = undefined;

    this._xPlot = 'Activity Type';
    this._yPlot = 'Activity Concentration';

    this._xAxis = undefined;
    this._yAxis = undefined;

    // the list of colors used in the graph display
    this._colors = [];
    // the list of shapes used in the graph display
    this._shapes = [];

    /* init listeners for interface components */
    let self = this;
    $jq(document).on('change', '#color-select', function(evt){
      console.log('CHANGE: color selection');
      self._updateColorScale('#color-select');
      self.plot(self._xPlot, self._yPlot);
    });
    $jq(document).on('change', '#shape-select', function(evt){
      console.log('CHANGE: shape selection');
      self._updateShapeScale('#shape-select');
      self.plot(self._xPlot, self._yPlot);
    })

    /* Add a base component to the SVG element. All posterior drawing will be
     * done nested to this basic component */
    let graph = d3.select('svg#canvas').append('g')
      .attr('id', 'graph')
      .attr('transform', 'translate('+margin.left+','+margin.top+')')
    ;
  }

  /**
   * Find individual values for a given column.
   * Indiviudual values are required to construct the categorical scales used
   * for color and shape, later used to map distinctive points at the time of
   * display.
   *
   * @param {string} column The column in the data for which we are trying to
   * find all individual values
   * @return An array with all the distinctive values found for a given column
   * in the data - Used to define the domain of a categorical scale.
   */
  _individualValues(column){
    let values = this._data.reduce(function(prev, current){
      if( ! prev.includes(current[column]) )
        prev.push(current[column]);
      return prev;
    }, []);
    return values;
  }

  /**
   * Update the color scale used in the display of data.
   * Since the data to be visualized is multi-dimensional in nature, any of the
   * fields that are present can be used to define a categorical color scale.
   * Currently, a fixed amount of 10 base colours are sequentially assigned to
   * each individual value found for the chosen category in the dataset. If more
   * than 10 values are found, colors are re-used.
   *
   * @param {string} id The id of the select element for which we are changing
   * the color scale
   */
  _updateColorScale(id){
    let self = this;
    /* retrieve the option based on which to define the color scale */
    let key = $jq(id).val();

    /* define the set of individual values that the current key takes, so that
     * we can assign an specific color to each of them */
    let values = this._individualValues(key);
    this._colors = {};
    values.forEach(function(k, i){
      this._colors[k] = d3.schemeCategory10[i%10];
    }, this );

    /* Modify the color field according to the new list of values for each data
     * point */
    this._data.forEach(function (item){
      item['color'] = this._colors[item[key]];
    },this);

    /* create the actual table of values  */
    this._updateTable('color');

    /* Add a listener to show/hide elements based in color */
    $jq(document).on('click', '.color-checkbox', function(evt){
      self.plot(self._xPlot, self._yPlot);
    });
  }

  _updateShapeScale(id){
    let self = this;
    /* retrieve the option based on which to define the color scale */
    let key = $jq(id).val();

    /* define the set of individual values that the current key takes, so that
     * we can assign an specific color to each of them */
    let values = this._individualValues(key);
    this._shapes = {};
    values.forEach(function(k, i){
      this._shapes[k] = d3.symbols[i%7];
    }, this );

    /* Modify the color field according to the new list of values for each data
     * point */
    this._data.forEach(function (item){
      item['shape'] = this._shapes[item[key]];
    },this);

    /* update the table */
    this._updateTable('shape');

    /* Add a listener to show/hide elements based in color */
    $jq(document).on('click', '.shape-checkbox', function(evt){
      self.plot(self._xPlot, self._yPlot);
    });
  }

  /**
   * Update the options available for a given Select DOM element.
   * Given the id of a select element, it updates the options available based on
   * the names of the columns of the data array.
   *
   * @param {string} id The id of the select component that should be updated
   * @param {string} selected From the options that are being added, indicates
   * the one that should be marked as selected
   */
  _updateOptions(id, selected){
    /* remove previous options */
    $jq(id+' > option').remove();
    /* and add the options based on the columns of the current data */
    let select = $jq(id);
    this._data.columns.forEach(function (key){
      /* only categorical options are added to the list */
      if ( typeof(this._data[0][key]) !== 'string' ) return;

      let opt = $jq('<option>');
      opt.val(key);
      opt.text(key);
      if( key === selected )
        opt.prop('selected', 'selected');
      select.append(opt);
    }, this);
  }

  _updateTable(type){
    /* Generate an array of data elements that we can use to generate a 'table'
     * of elements using D3 */
    let self = this;
    let values = (type === 'color') ? Object.keys(this._colors) : Object.keys(this._shapes);
    let data = values.reduce( function(prev, current){
      prev.push(
        {
          'key': current,
          'value': (type === 'color') ? self._colors[current] : self._shapes[current],
        }
      )
      return prev;
    }, []);

    /* Before we can (re)build the 'table' we need to clean any previous elements */
    let table = d3.select('#'+type+'-table')
      .selectAll('div').remove();

    /* We use D3 to build all the rows in the table at the same time */
    table = d3.select('#'+type+'-table').selectAll('div')
      .data(data);

    /* create each row */
    let row = table.enter().append('div')
      .attr('class', 'flex-row')
      ;

    /* first cell: checkbox input */
    row.append('div')
      .attr('class', 'flex-cell')
      .append('input')
        .attr('class', type+'-checkbox')
        .attr('type', 'checkbox')
        .attr('data-'+type, function(d){ return d.key; } )
        .property('checked', 'true')

    /* second cell: a simple color background or an svg element with a symbol */
    let cell = row.append('div')
      .attr('class', 'flex-cell')
      ;
    if( type === 'color' )
      cell.style('background-color', function(d){ return d.value; });
    else{ // shape
      cell.append('svg')
        .attr('viewBox', '-5 -5 10 10')
        .style('height', 'inherit')
        .append('path')
          .attr('fill', 'black')
          .attr('d', function(d){
            let symbol = d3.symbol()
              .size(10)
              .type(d.value)
            ;
            return symbol();
          })
      ;
    }

    /* third cell: label */
    row.append('div')
      .attr('class', 'flex-cell label')
      .text( function(d){ return d.key; } )
      ;
  }

  /**
   * Dynamically generate the X-Axis used in the graph
   * Compounds can have multiple types of bio-activity measurements registered,
   * thus, whenever a new graph is generated, the axis used for the measurements
   * needs to be generated. The key should always define a categorical scale.
   *
   * @param {string} key The key, on of the available fields in the dataset,
   * that will be used to define the axis. Typically, this will be 'Activity Type'
   */
  _updateXAxis(key){
    /* retrieve the individual values associated to the key, and use them to
     * define an ordinal scale */
    let values = this._individualValues(key);
    let scale = d3.scaleOrdinal()
      .domain(values)
      .range(values.map(function(k,i){ return i*(width/(values.length-1)); }))
      ;
    /* create the corresponding axis */
    this._xAxis = d3.axisBottom(scale);

    /* remove previous axis components */
    let canvas = d3.select('svg#canvas > g#graph');
    canvas.selectAll('#bottom-axis').remove();

    canvas.append('g')
      .attr('id', 'bottom-axis')
      .attr('transform', 'translate(0,'+height+')')
      .call(this._xAxis)
    ;
  }

  /**
   * Update the scale and redraw the left-side axis.
   * Different scales can be used for the display of the data. The left-side
   * axis in particular must correspond to a numerical value, and the scale
   * generated is logarithmic.
   *
   * @param {string} key The key value, from the one availables in the dataset,
   * used to construct the scale.
   */
  _updateYAxis(key){
    /* we need to define min and maximum values for the scale */
    let max = this._data.reduce(function(prev, current){
      return Math.max(+current[key], prev);
    }, -Infinity);
    let scale = d3.scaleLog()
      .domain([0.1,  max])
      .range([height, 0]);

    /* create the corresponding axis */
    this._yAxis = d3.axisLeft(scale);
    this._yAxis.ticks(10, ',.0f');

    /* remove previous axis components */
    let canvas = d3.select('svg#canvas > g#graph');
    canvas.selectAll('#left-axis').remove();
    canvas.append("g")
      .attr('id', 'left-axis')
      .attr('transform', 'translate('+(-margin.left/2)+',0)')
      .call(this._yAxis)
    ;
  }

  /**
   * Initialize the graph data
   * Process the data string provided from Targetmine into a data structure that
   * can be used for the visualization
   *
   * @param {string} data A string representation of the data included in the
   * graph
   */
  loadData(data){
    /* the data provided by Targetmine's Api is the string representation of a
     * Java ArrayList. To convert this to an array of Javascript objects we
     * first need to dispose the initial and trailing '[' ']' characters */
    data = data.substring(1, data.length-1);
    /* second, replace the ',' chars for line separators  */
    data = data.replace(/,/g, '\n');
    /* third, we parse the resulting array of rows into an array of objects by
     * using tab separators, and the first row as keys for the mapping */
    this._data = d3.tsvParse(data, d3.autoType);
    /* add a new field to data points to store color information */
    this._data.forEach(function(item){
      item.color = undefined;
      item.shape = undefined;
    });

    /* log the results of the loading proces */
    console.log('Loaded Data: ', this._data);

    /* update the select components in the interface according to the columns
     * available in the graph's data */
    this._updateOptions('#color-select', this._xPlot);
    this._updateOptions('#shape-select', this._xPlot);

    /* update the axis of the graph */
    this._updateXAxis(this._xPlot);
    this._updateYAxis(this._yPlot);

    /* update the colors used for the data points in the graph */
    this._updateColorScale('#color-select');

    /* update the shapes used for the data points in the graph */
    this._updateShapeScale('#shape-select');

    /* plot the data points */
    this.plot(this._xPlot, this._yPlot);
  }

  /**
   * Initialize a graph display
   * Whenever a new data file is loaded, a graph display is initialized using a
   * default Y-axis.
   * Notice that the first step required to display a graph is to clear whatever
   * content the display area might have already have.
   * @param {string} X the name of the column used as default Y-axis for the
   * graph
   */
  plot(X, Y){
    let self = this;

    /* Generate an array of data points positions and colors based on the scale
     * defined for each axis */
    let xscale = this._xAxis.scale();
    let yscale = this._yAxis.scale();
    /* Get a list of the colors that should be displayed */
    let cb = []
    $jq.map($jq('.color-checkbox:checked'), function(obj, k){
      cb.push(obj.dataset.color);
    });
    $jq.map($jq('.shape-checkbox:checked'), function(obj, k){
      cb.push(obj.dataset.shape);
    })

    let colorMap = $jq('#color-select').val();
    let shapeMap = $jq('#shape-select').val();

    let points = this._data.reduce(function(prev, current){
      /* filter out the points that are hidden from the visualization */
      if( cb.includes(current[colorMap]) && cb.includes(current[shapeMap]) ){
        prev.push(
          {
            x: xscale(current[X]),
            y: yscale(current[Y]),
            'color':current['color'],
            'shape':current['shape'],
            'label': current[self._yPlot],
          }
        );
      }
      return prev;
    }, []);

    /* redraw the points, using the updated positions and colors */
    let canvas = d3.select('svg#canvas > g#graph');
    canvas.selectAll('#points').remove();

    canvas.append('g')
      .attr('id', 'points')
      ;

    /* for each data point, generate a group where we can add multiple svg
     * elements */
    let pts = d3.select('#points').selectAll('g')
      .data(points)
    let point = pts.enter().append('g')
        .attr('class', 'data-point')
        .append('path')
          .attr('transform', function(d){ return 'translate('+d.x+','+d.y+')'; })
          .attr('fill', function(d){return d.color;})
          .attr('d', function(d){
            let symbol = d3.symbol()
              .size(50)
              .type(d.shape)
            ;
            return symbol();
          })
        .append("svg:title")
          .text(function(d){ return d.label; })
      ;
  }
}
