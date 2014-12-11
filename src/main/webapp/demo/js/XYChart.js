function createXYChart (divId, chartData) {
	var response = jq.parseJSON(chartData);
	var divElement = jq('$'+divId).empty();
	
	var showLegend = false;
	if(Object.keys(response.chartTypes).length > 1) {
		showLegend = true;
	} 
	var showSecondAxis = false;
	if(response.secondaryYAxisLabel.length){
		showSecondAxis = true;
	}
	var rotateAxis = false;
	if(response.rotateAxis){
		rotateAxis = true;
	}
	var yColumnMargin = 20;
	if(showLegend){yColumnMargin = 60}
	
	var container = jq("#" + divElement.attr("id"));
	
	var filter_desc = "";
	if(response.isFiltered){
		filter_desc = "<span class='btn-link btn-sm' style='float: right; padding: 0px 10px;' id='"+ response.portletId +"_title'> Filters </span>" +
				"<div id='"+ response.portletId +"_filter_content' style='line-height: initial;position: absolute;padding: 2px;border: 1px solid rgb(124, 124, 124);margin: 5px;background-color: rgb(177, 177, 177);font-size: small;color: white;z-index: 2; display: none'>"+ response.filterDescription +"</div>" +
				"<script type='text/javascript'>" +
					"jq('#"+ response.portletId +"_title').mouseenter(function() {" +
							"jq('#"+ response.portletId +"_filter_content').show();" +
									"})" +
					".mouseleave(function() { " +
							"jq('#"+ response.portletId +"_filter_content').hide();});" +
				"</script>";
	}
	
	container.append(jq("<div style='margin-top: 3px; margin-left: 5px; height: 15px;'>"+ filter_desc + " </div>" ));
	
	container.append(
				jq( "<div id='"+ response.portletId + "holderDiv" +"'> Rendering chart... </div>" )
			);
	
	var fullHeight = divElement.height();
	var fullWidth = divElement.width();
	
	if(fullWidth < 50 ){ fullWidth = 400; }
	if(fullHeight < 50 ){ fullHeight = 385; }
	
	var isLargeGraph = false;
	if(response.xCategoryLabels.length > 25){
		isLargeGraph = true;
	}
	
	var xAxisType = 'categorized';
	var timeFormat = null, timeColumnName = null, xAxisDisplayFormat = null,yMin=null,yMax=null;y2Max=null;y2Min=null;
	if(response.timeseries.isEnabled) {
		xAxisType = 'timeseries';
		timeFormat = response.timeseries.format;
		timeColumnName = response.xAxisLabel;
		xAxisDisplayFormat = response.timeseries.displayFormat;
	}
	
	if(response.yMin) {
		yMin=response.yMin;
	}
	if(response.yMax) {
		yMax=response.yMax;
	}
	if(response.y2Min){
		y2Min = response.y2Min;
	}
	if(response.y2Max) {
		y2Max=response.y2Max;
	}
	c3JSON = {
			data: {
				x: timeColumnName,
			    xFormat : timeFormat,
				rows: response.dataRows,	
				types: response.chartTypes,
				axes:response.axes				
			},
			bindto: "#" + response.portletId + "holderDiv",
			size: { 
				width:fullWidth - 5,
				height:fullHeight - 25
			},
			bar:{
				width:{
	            ratio: 0.5 // this makes bar width 50% of length between ticks
			    },
			    zerobased:false
			},
			axis: {
				y: {
					min: yMin,
					max: yMax,
		            padding: {top:0, bottom:0},
					tick: {
		                format: d3.format(".2s")
		            },
		            label: {
		                text: response.primaryYAxisLabel,
		                position: 'outer-middle'
		            }
				},
				x: {
					type: xAxisType,
					categories: response.xCategoryLabels,
					tick: {
						format: xAxisDisplayFormat
			        },
					label: {
		                text: response.xAxisLabel,
		                position: 'outer-center'
		            }
				},
				y2: {
					min: y2Min,
					max: y2Max,
		            padding: {top:0, bottom:0},
					tick: {
		                format: d3.format(".2s")
		            },
		            label: {
		                text: response.secondaryYAxisLabel		                
		            },
		            show: showSecondAxis		            
		        },		       
		       rotated: rotateAxis
			},
			tooltip: {
		        format: {
		            value: d3.format('f')
		        }
		    },
			legend: {
		        show: showLegend,
		        equally: true
		    },
			subchart: {
		        show: isLargeGraph
		    },
			zoom: {
		        enabled: false
		    }
		};
	
	console.log(JSON.stringify(c3JSON));
	
	var chart = c3.generate(c3JSON);
}
