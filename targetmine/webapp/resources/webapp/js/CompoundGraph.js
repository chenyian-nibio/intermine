'use strict';

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

    this._colors = undefined;
    this._shapes = undefined;
  }

  /**
   * Initialize the graph data
   * Process the data string provided from Targetmine into a data structure that
   * can be used for the visualization
   */
  setData(data){
    /* the data provided by Targetmine's Api is the string representation of a
     * Java ArrayList. To convert this to an array of Javascript objects we
     * first need to dispose the initial and trailing '[' ']' characters */
    data = data.substring(1, data.length);

    /* second, replace the ',' chars for line separators  */
    data = data.replace(/,/g, '\n');

    /* third, we parse the resulting array of rows into an array of objects by
     * using tab separators, and the first row as keys for the mapping */
    this._data = d3.tsvParse(data, d3.autoType);
  }

  /**
   *
   */
  getDataColumns(){
    return this._data.columns;
  }

  /**
   * @param {string} column The name of the column (from the array of columns)
   * available in the _data array, for which we are defining an axis
   */
  getPoints(dimx, dimy, color='Activity Type', shape='Organism Name'){
    let self = this;
    let points = [];
    let xscale = self._xAxis.scale();
    let yscale = self._yAxis.scale();
    this._data.forEach(function(row){
      let p = {};
      p.x = xscale(row[dimx]);
      p.y = yscale(row[dimy]);
      p.color = self._colors[row[color]];
      // p.shape = self._shapes.find(x => x.name === row[shape]).shape;
      p.label = row['Activity Type']+': '+row['Activity Concentration']
      points.push(p);
    });
    return points;
  }

  /**
   * Set the colors using the values of the given column
   */
  setColors(column='Compound > Target Proteins > Activities > Type'){
    this._colors = {};
    let i = 0;
    let self = this;
    let keys = this._data.reduce(function(prev, current){
      if( ! prev.includes(current[column]) )
        prev.push(current[column]);
      return prev;
    }, []);

    keys.forEach(function(k, i){
      self._colors[k] = d3.schemeCategory10[i%10];
    });

    return this._colors;
  }

  /**
   *
   */
  setShapes(column='Compound > Target Proteins > Protein > Organism . Name'){
    this._shapes = [];
    let i = 0;
    let self = this;
    let keys = this._data.reduce(function(prev, current){
      if( ! prev.includes(current[column]) )
        prev.push(current[column]);
      return prev;
    }, []);

    keys.forEach(function(k, i){
      let shape = {};
      shape.name = k;
      shape.shape = d3.symbols[i%d3.symbols.length];
      self._shapes.push(shape);
    });
    return this._shapes;
  }

  getColors(){
    return this._colors;
  }

  /**
   * @return An array with all the distinctive values found for a given column
   * in the data - Used to define the domain of a categorical scale.
   */
  _getKeys(column){
    let keys = [];
    this._data.forEach(function(row){
      if (!keys.includes(row[column]))
        keys.push(row[column]);
    });
    return keys;
  }

  /**
   *
   * @param {string} column The name of the column (from the array of columns)
   * available in the _data array, for which we are defining an axis
   * @param {number} dim An identifier for the axis: 0- X axis, 1- Y axis, 2- Z
   * @param {number} size The size in pixels that the axis should use
   */
  createAxis(column, dim, size){
    /* if the value of the axis are strings, then we need to define an ordinal
     * scale for the axis, alternatively, if the values are numbers, we can
     * define either a logarithmic (or linear) scale */
    let type = typeof(this._data[0][column]);

    /* create the scale for the axis */
    let scale;
    switch (type){
      /* create a categorical axis */
      case 'string':
        /* the domain is the list of different values for the given column */
        let domain = [""].concat(this._getKeys(column));
        /* the range are the X-positions (pixels) for each value */
        let dx = size / domain.length
        let range = [...Array(domain.length).keys()].map( (val,i) => {return i*dx;} );
        scale = d3.scaleOrdinal()
          .domain(domain)
          .range(range)
          ;
        break;

      /* create a continuous scale */
      case 'number':
        let max = -Infinity;
        this._data.forEach(function(row){
          if( +row[column] > max )
          max = +row[column];
        });
        scale = d3.scaleLog()
          .domain([0.1,  max])
          .range([size, 0])
          ;
        break;
    }

    /* create the corresponding axis */
    switch (dim){
      case 0: /* X axis */
        this._xAxis = d3.axisBottom(scale);
        return this._xAxis;
      case 1: /* Y axis */
        this._yAxis = d3.axisLeft(scale)
          .ticks(10, ',.0f')
        ;
        return this._yAxis;
        break;
    }

  }

}
