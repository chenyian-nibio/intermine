'use strict';

var $jq = jQuery.noConflict();
var margin = {top: 40, right: 40, bottom: 40, left: 40};
var width = 400 - margin.left - margin.right;
var height = 400 - margin.top - margin.bottom;

/**
 * @class CompoundGraph
 * @classdesc
 * @author
 * @version
 *
 */
class CompoundGraph{

  constructor(name){
    this._name = name;
    this._data = undefined;

    this._xAxis = undefined;
    this._yAxis = undefined;

    // the list of colors used in the graph display
    this._colors = [];
    // the list of shapes used in the graph display
    this._shapes = [];
  }

  /**
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
   * @param {string} column The name of the column (from the array of columns)
   * available in the _data array, for which we are defining an axis
   */
  _getPoints(dimx, dimy, color='Activity Type', shape='Organism Name'){
    let points = [];
    this._data.forEach(function(item){
      let p = {};
      p.x = xscale(row[dimx]);
      p.y = yscale(row[dimy]);
      p.color = self._colors[row[color]];
      // p.shape = self._shapes.find(x => x.name === row[shape]).shape;
      p.label = row['Activity Type']+': '+row['Activity Concentration']
      points.push(p);
    },this);
    return points;
  }

  /**
   * Init the set of colors used in the display of data.
   * Since the data to be visualized is multi-dimensional in nature, any of the
   * fields that are present can be used to define a categorical color scale.
   * Currently, a fixed amount of 10 base colours are sequentially assigned to
   * each individual value found for the chosen category in the dataset. If more
   * than 10 values are found, colors are re-used.
   *
   * @param {string} key The key in the dataset used to generate the color scale
   */
  initColors(key){
    let self = this;
    /* define the set of individual values that the current key takes, so that
     * we can assign an specific color to each of them */
    let values = this._individualValues(key);
    this._colors = {};

    /* select the table where we will add elements and remove any previous data */
    let table = $jq('#color-table > tbody');
    table.empty();

    /* Iterate over the list to construct the color scale, and simultaneously
     * define the html required to its display */
    for(let i=0; i<values.length; ++i){
      /* create an object to store the key and the corresponding html color definition */
      // let color = {};
      // color.key = values[i];
      // color.html = d3.schemeCategory10[i%10]
      // /* add it to the list of colors */
      // self._colors.push(color);
      this._colors[values[i]] = d3.schemeCategory10[i%10]

      let inp = $jq('<input>');//   let inp = document.createElement('input');
      inp.addClass('color-checkbox'); //inp.className = 'color-checkbox';
      inp.attr('type', 'checkbox'); //inp.setAttribute("type", "checkbox");
      inp.attr('data-color', values[i]);//color.key); //.setAttribute('data-color', colors[k]);
      inp.attr('checked', 'true'); //inp.checked = true;
      //.on('change', self.plot())//_plotinp.addEventListener('change', function(){self.plot();});

      let check = $jq('<td>');// let check = row.insertCell(-1);
      check.append(inp);
      check.css('width', '10%'); //check.style.width = '10%';

      let ctd = $jq('<td>'); // let color = row.insertCell(-1);
      ctd.css('width', '10%'); //color.style.width = '10%';
      ctd.css('background-color', this._colors[values[i]]);//color.html); //color.style.backgroundColor = colors[k];

      let label = $jq('<td>'); // let label = row.insertCell(-1);
      label.attr('id', 'color-label'); //label.setAttribute('id', 'color-label');
      label.text(values[i]);//color.key); //label.innerHTML = k;

      let row = $jq('<tr>'); //let row = table.insertRow(-1);
      row.append(check);
      row.append(ctd);
      row.append(label);

      table.append(row);
    };

    /* Modify the color field according to the new list of values for each data point */
    this._data.forEach(function (item){
      item['color'] = this._colors[item[key]];
    },this);
  }

  /**
   * Initialize the graph data
   * Process the data string provided from Targetmine into a data structure that
   * can be used for the visualization
   *
   * @param {string} data A string representation of the data included in the
   * graph
   */
  initData(data){
    /* the data provided by Targetmine's Api is the string representation of a
     * Java ArrayList. To convert this to an array of Javascript objects we
     * first need to dispose the initial and trailing '[' ']' characters */
    data = data.substring(1, data.length);
    /* second, replace the ',' chars for line separators  */
    data = data.replace(/,/g, '\n');
    /* third, we parse the resulting array of rows into an array of objects by
     * using tab separators, and the first row as keys for the mapping */
    this._data = d3.tsvParse(data, d3.autoType);

    /* update the select components in the interface according to the columns
     * available in the graph's data */
    $jq('#color-select > option').remove(); //[value!=undefined]').remove();
    let cSelect = $jq('#color-select');
    this._data.columns.forEach(function(k){
      let opt = $jq('<option>');
      opt.val(k)
      opt.text(k);
      if( k === 'Activity Type')
        opt.prop('selected', 'selected');

      cSelect.append(opt);
    });

    /* add a new field to data points to store color information */
    this._data.forEach(function(item){
      item.color = undefined;
      item.shape = undefined;
    });
  }

  /**
   * Init the set of shapes used in the display of data.
   * Since the data to be visualized is multi-dimensional in nature, any of the
   * fields that are present can be used to define a categorical shape scale.
   * Currently, the symbols used for the display is limited to those defined in
   * D3, and that can be found in the d3.symbols array. If the amount of individual
   * exceedes that of symbols, then these are re-used.
   *
   * @param {string} key The key in the dataset used to generate the color scale
   */
  initShapes(column='Compound > Target Proteins > Protein > Organism . Name'){
    let keys = this._individualValues(key);

    let self = this;
    keys.forEach(function(k, i){
      let shape = {};
      shape.name = k;
      shape.shape = d3.symbols[i%d3.symbols.length];
      self._shapes.push(shape);
    });

  }




  /**
   *
   */
  initXAxis(key){
    /* if the value of the axis are strings, then we need to define an ordinal
     * scale for the axis, alternatively, if the values are numbers, we can
     * define either a logarithmic (or linear) scale */
    let type = typeof(this._data[0][key]);

    /* create the scale for the axis */
    let scale;
    switch (type){
      /* create a categorical axis */
      case 'string':
        /* the domain is the list of different values for the given column */
        let domain = this._individualValues(key);
        scale = d3.scaleOrdinal()
          .domain(domain)
          ;
        break;

      /* create a continuous scale */
      case 'number':
        let max = -Infinity;
        this._data.forEach(function(row){
          if( +row[key] > max )
          max = +row[key];
        });
        scale = d3.scaleLog()
          .domain([0.1,  max])
          ;
        break;
    }
    scale.range([0,width]);

    /* create the corresponding axis */
    this._xAxis = d3.axisBottom(scale);
  }

  /**
   *
   */
  initYAxis(key){
    /* if the value of the axis are strings, then we need to define an ordinal
     * scale for the axis, alternatively, if the values are numbers, we can
     * define either a logarithmic (or linear) scale */
    let type = typeof(this._data[0][key]);

    /* create the scale for the axis */
    let scale;
    switch (type){
      /* create a categorical axis */
      case 'string':
        /* the domain is the list of different values for the given column */
        let domain = this._individualValues(key);
        scale = d3.scaleOrdinal()
          .domain(domain)
          ;
        break;

      /* create a continuous scale */
      case 'number':
        let max = -Infinity;
        this._data.forEach(function(row){
          if( +row[key] > max )
          max = +row[key];
        });
        scale = d3.scaleLog()
          .domain([0.1,  max])
          ;
        break;
    }
    scale.range([height, 0]);

    /* create the corresponding axis */
    this._yAxis = d3.axisLeft(scale);
    this._yAxis.ticks(10, ',.0f');
  }

  // /**
  //  *
  //  * @param {string} column The name of the column (from the array of columns)
  //  * available in the _data array, for which we are defining an axis
  //  * @param {number} dim An identifier for the axis: 0- X axis, 1- Y axis, 2- Z
  //  * @param {number} size The size in pixels that the axis should use
  //  */
  // createAxis(column, dim, size){
  //   /* if the value of the axis are strings, then we need to define an ordinal
  //    * scale for the axis, alternatively, if the values are numbers, we can
  //    * define either a logarithmic (or linear) scale */
  //   let type = typeof(this._data[0][column]);
  //
  //   /* create the scale for the axis */
  //   let scale;
  //   switch (type){
  //     /* create a categorical axis */
  //     case 'string':
  //       /* the domain is the list of different values for the given column */
  //       let domain = [""].concat(this._getKeys(column));
  //       /* the range are the X-positions (pixels) for each value */
  //       let dx = size / domain.length
  //       let range = [...Array(domain.length).keys()].map( (val,i) => {return i*dx;} );
  //       scale = d3.scaleOrdinal()
  //         .domain(domain)
  //         .range(range)
  //         ;
  //       break;
  //
  //     /* create a continuous scale */
  //     case 'number':
  //       let max = -Infinity;
  //       this._data.forEach(function(row){
  //         if( +row[column] > max )
  //         max = +row[column];
  //       });
  //       scale = d3.scaleLog()
  //         .domain([0.1,  max])
  //         .range([size, 0])
  //         ;
  //       break;
  //   }
  //
  //   /* create the corresponding axis */
  //   switch (dim){
  //     case 0: /* X axis */
  //       this._xAxis = d3.axisBottom(scale);
  //       return this._xAxis;
  //     case 1: /* Y axis */
  //       this._yAxis = d3.axisLeft(scale)
  //         .ticks(10, ',.0f')
  //       ;
  //       return this._yAxis;
  //       break;
  //   }
  //
  // }

  /**
   * Initialize a graph display
   * Whenever a new data file is loaded, a graph display is initialized using a
   * default Y-axis.
   * Notice that the first step required to display a graph is to clear whatever
   * content the display area might have already have.
   * @param {string} col the name of the column used as default Y-axis for the
   * graph
   */
  plot(X='Activity Type', Y='Activity Concentration'){

    let svg = d3.select('svg#canvas');
    /* clear the whole drawing */
    svg.selectAll().remove();
    /* Add a top-most g element to group the whole drawing */
    let canvas = svg.append('g')
      .attr('transform', 'translate('+margin.left+','+margin.top+')')
    ;

    /* draw title */
    /* draw axis */
    /* and add the new one to the display */
    canvas.append('g')
      .attr('id', 'bottom-axis')
      .attr('transform', 'translate(0,'+height+')')
      .call(this._xAxis)
    ;

    // let yAxis = this._graph.createAxis(Y, 1, height-(2*vPadding));
    /* and add the new one to the display */
    canvas.append("g")
      .attr('id', 'left-axis')
      .attr('transform', 'translate('+(-margin.left/2)+',0)')
      .call(this._yAxis)
    ;

    /* draw points */
    let xscale = this._xAxis.scale();
    let yscale = this._yAxis.scale();
    let points = this._data.reduce(function(prev, current){
      prev.push( { x: xscale(current[X]), y: yscale(current[Y]), 'color':current['color'] } );
      return prev;
    }, []);

    /* filter out the points that are hidden from the visualization */
    let visible = document.getElementsByClassName('color-checkbox');
    for( let i = 0; i < visible.length; ++i ){
      let cb = visible.item(i);
      if (cb.checked === false){
        points = points.filter(function(ele){
          if( ele.color !== cb.dataset.color)
            return ele;
        });
      }
    }

    /* add a <g> element to group the drawing of the data points */
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
          .attr('transform', function(d){ console.log(d);return 'translate('+d.x+','+d.y+')'; })
          .attr('fill', function(d){return d.color;})
          .attr('d', function(d){
            let symbol = d3.symbol()
              .size(50)
              .type(d3.symbolStar)//d.shape)
            ;
            return symbol();
          })
        // .append("svg:title")
        //   .text(function(d){ return d.label; })
      ;
  }


    /* get the area available for drawing */
    // let canvas = d3.select('svg#canvas');
    // let width = parseInt(canvas.style('width'));
    // let height = parseInt(canvas.style('height'));
    // let hPadding = width  *.1; // horizontal padding 10% on each side
    // let vPadding = height *.1; // vertical padding 10% on each side
    //
    // /* init options for all select components of the DOM */
    // let select = d3.selectAll('select')
    //   .selectAll('option').remove()
    //   .data(this._graph.getDataColumns())
    //   .enter().append('option')
    //     .attr('value', function(d){ return d; })
    //     .text(function(d){ return d; })
    //   ;

    // /* generate a default y Axis */

    //
    // /* set default colors */
    // document.getElementById('color-select').dispatchEvent(new Event('change'));
    //
    // /* set default shapes */
    // // document.getElementById('shape-select').dispatchEvent(new Event('change'));
    //
    // /* plot the default values */
    // this.plot(X, Y, hPadding, vPadding); // Type vs Concentration
  // }

}
