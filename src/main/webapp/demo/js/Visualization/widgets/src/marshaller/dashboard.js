(function (root, factory) {
    if (typeof define === "function" && define.amd) {
        define(["d3/d3", "../common/SVGWidget", "./HipieDDL"], factory);
    } else {
        root.DASHBOARD = factory(root.d3, root.Widget, root.HipieDDL);
    }
}(this, function (d3, Widget, HipieDDL) {
    function DASHBOARD(target) {
        Widget.call(this);

        this.marshaller = new HipieDDL.Marshaller();

        this._url = "";
    };
    DASHBOARD.prototype = Object.create(Widget.prototype);

   DASHBOARD.prototype.url = function (_) {
        if (!arguments.length) return this._url;
        this._url = _;
        return this;
    };
    
    DASHBOARD.prototype.proxyMappings = function (_) {
        var retVal = this.marshaller.proxyMappings(_);
        if (arguments.length) {
            return this;
        }
        return retVal;
    };

    DASHBOARD.prototype.target = function (_) {
        if (!arguments.length) return this._target;
        this._target = _;
        return this;
    };

    DASHBOARD.prototype.size = function (_) {
        if (!arguments.length) return this._size;
        this._size = _;
        return this;
    };

    DASHBOARD.prototype.render = function (ids) {
        var context = this;
        if (this._url[0] === "[" || this._url[0] === "{") {
            this.marshaller.parse(this._url, function () {
                context.doRender(ids);
            });
        } else {
            this.marshaller.url(this._url, function (response) {
			    context.doRender(ids);                 
            });
        }
        return this;
    };

   
	
	DASHBOARD.prototype.doRender = function (ids) {			
        for (var key in this.marshaller.dashboards) {
            var dashboard = this.marshaller.dashboards[key];
            
            for (mat in ids){
            	console.log(ids[mat]);
            }
			
            try {
    			for (mat in ids){
    				console.log(ids[mat].div+":"+ids[mat].chart);
    				d3.select("#"+ids[mat].div).selectAll("marshalViz").data(
    								dashboard.visualizationsArray.filter(
    									function (d) { 
    										if (d.id == ids[mat].chart){
    											return d.widget; 
    										}	
    									}
    								), 
    									function (d) {
    										if (d.id == ids[mat].chart){
    											return d.id; 
    										}
    									}
    								).enter().append("div")
    								.attr("class", "marshalViz")
    								.style({
    									width : d3.select('#'+ids[mat].div).style('width'),
    									height: d3.select('#'+ids[mat].div).style('height'),
    									display: "inline-block"
    								})
    								.each(function (item) {								    									
    									if (item.id == ids[mat].chart){
    										console.log(item.id); 
    										console.log(this);
    										var element = d3.select(this);
    										item.widget
    											.target(this)
    											.render()
    										;
    									}
    								})
    							;
    			}
    			
            } catch(e){
            	console.log(e);
            }
							
							
							
							
							for (var key in dashboard.datasources) {							    
								dashboard.datasources[key].fetchData({}, true);
							}
			
                
          
        }
    };

    return DASHBOARD;
}));
