var dashboardViz = {};
var previewData = {};

function createPreview(target, chartType, data) {
	clearChart(target);
	
	require([ "assets/js/Visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "assets/js/Visualization/widgets"
		});
		
		if(data == "null"){
			jq('#'+target).append("<div id=\"NoChart\">Unable to render chart</div>");
		}
		var actualData = JSON.parse(data);
		console.log(actualData);		
		
		previewData.data = actualData;
		previewData.type = chartType;
		previewData.title = actualData.title;
		

	require([ "src/c3/Pie", "src/c3/Line", "src/c3/Column",
				"src/map/ChoroplethStates", "src/c3/Donut", "src/c3/Bar",
				"src/c3/Area", "src/c3/Scatter", "src/c3/Step",
				"src/other/Table" ], function(
				C3Pie, C3Line, C3Column, ChoroplethStates, C3Donut, C3Bar, C3Area, C3Scatter, C3Step,Table) {

			console.log(actualData);
			if (chartType == "C3_PIE") {
				new C3Pie()
					.target(target)
					.data(actualData.data)
					.render();
			}

			if (chartType == "C3_LINE") {
				new C3Line()
				.target(target)
				.columns(actualData.columns)
				.data(actualData.data)
				.render();
			}

			if (chartType == "C3_COLUMN") {
				new C3Column()
				.target(target)
				.columns(actualData.columns)
				.data(actualData.data)
				.render();
			}
			
			if (chartType == "CHORO") {
				 new ChoroplethStates()
				 .columns(actualData.columns)
				 .data(actualData.data)
                 .target(target)
                 .render();
				
			}
			
			if (chartType == "C3_DONUT") {
				new C3Donut()
				.target(target)
				.data(actualData.data)
				.render();
			}
			
			if (chartType == "C3_BAR") {
				new C3Bar()
				.target(target)
				.columns(actualData.columns)
				.data(actualData.data)
				.render();
			}
			
			if (chartType == "C3_AREA") {
				new C3Area()
				.target(target)
				.columns(actualData.columns)
				.data(actualData.data)
				.render();
			}
			
			if (chartType == "C3_SCATTER") {
				new C3Scatter()
				.target(target)
				.columns(actualData.columns)
				.data(actualData.data)
				.render();
			}
			
			if (chartType == "C3_STEP") {
				new C3Step()
				.target(target)
				.columns(actualData.columns)
				.data(actualData.data)
				.render();
			}
			
			if (chartType == "TABLE") {
				console.log("actual data:"+actualData.data);
				new Table()
				.target(target)
				.columns(actualData.columns)
				.data(actualData.data)
				.render();
			}

		});
	});
}

function clearChart(target){
	jq('#'+target).empty();
}

function visualizeDDLChart(data) {
	var chartData = JSON.parse(data);
	var url = chartData.url;
	var target = chartData.target;

	var visualizeRoxie = false;
	var layout = "Hierarchy";
	var hpccID = chartData.hpccID;

	var proxyMappings = null;
	if (!window.location.origin)
		window.location.origin = window.location.protocol + "//" + window.location.host;

	var hostUrl = window.location.origin + "/Dashboard/" + hpccID;

	proxyMappings = jq.parseJSON('{"' + chartData.WsWorkunits + '/WUResult.json":"'+ hostUrl + '/proxy-WUResult.do","'
			+ chartData.WsWorkunits + '/WUInfo.json":"' + hostUrl + '/proxy-WUInfo.do","' 
			+ chartData.WsEcl + '/submit/query/":"' + hostUrl + '/proxy-WsEcl.do"}');

	clearChart(target);
	
	require([ "assets/js/Visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "assets/js/Visualization/widgets"
		});

		require([ "src/marshaller/Graph" ], function(GraphMarshaller) {

			GraphMarshaller.createSingle(url, proxyMappings, visualizeRoxie,
					function(graphDashboard, json) {
						dashboardViz.graph = graphDashboard;						
						if(chartData.layout) {
							var deSerializedString=graphDashboard.deserialize(chartData.layout,[],["layout", "palette"]);
							console.log(graphDashboard);
							console.log(deSerializedString);
							console.log(chartData.layout);
							
							graphDashboard.target(target)
							.layout(layout)
							//.deserialize(chartData.layout)
							.renderDashboards();
							
						}else{
							console.log("no chart data");
							graphDashboard.target(target)
							.layout(layout).renderDashboards();
						}
		                dashboardViz.graph._data
		                	.vertices
		                	.forEach(function(multiChartSurface) {
		                		multiChartSurface.menu(["Configure", "Delete"]);
		                		multiChartSurface._menu.click = function(option) {
		                			var payload = {};
		                			payload.chartId=multiChartSurface._title;
		                			if(option == 'Configure') {
		                				zAu.send(new zk.Event(zk.Widget.$("$dashboardContainer"),'onEditChart', payload, {toServer:true}));
		                				
		                			} else if (option == 'Delete') {
		                				var wid = document.getElementById(this._id).parentNode.id;
		                                if (confirm("Are you sure want to delete the widget?") == true) {
		                                     document.getElementById(wid).style.display = 'none';
		                                     zAu.send(new zk.Event(zk.Widget.$("$dashboardContainer"),'onDeleteChart', payload, {toServer:true}));
		                                } 
		                				
		                			}
		                		};
		                	});
		                
					});
		});

	});

}

function injectPreviewChart(flowType) {
	require([ "assets/js/Visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "assets/js/Visualization/widgets"
		});
		
		require(["src/chart/MultiChartSurface","src/map/ChoroplethStates"], function (MultiChartSurface,ChoroplethStates) {	
			
			var oldData = dashboardViz.graph._data;
			console.log(oldData);
			if(flowType == "EDIT"){
				if (previewData.type == "CHORO") {
					var newGraph=new MultiChartSurface()
	                .title(previewData.title)
	                .faChar("\uf024")
	                .size({ width: 210, height: 210 })
	                .content(new ChoroplethStates().data(previewData.data.data));					
					
				}else{
					var newGraph=new MultiChartSurface()
					.columns(previewData.data.columns)
                    .data(previewData.data.data)
                    .chartType(previewData.type)
                    .title(previewData.title)
                    .faChar("\uf080")
                    .size({ width: 210, height: 210 });					
				}
				for(var d in oldData.vertices){						
					if(oldData.vertices[d]['_title'] == previewData.title){
						//document.getElementById(oldData.vertices[d]['_id']).style.display = 'none';
                        document.getElementById(oldData.vertices[d]['_content']['_id']).style.display = 'none';
						oldData.vertices[d]=newGraph;													
					}						
				}				
			}else{			
				if (previewData.type == "CHORO") {				
						  oldData.vertices.push(  new MultiChartSurface()
							.title(previewData.title)
							.faChar("\uf024")
							.size({ width: 210, height: 210 })
							.content(new ChoroplethStates().data(previewData.data.data))                                    
						  );
				}else{						
						oldData.vertices.push( 
							new MultiChartSurface()
								.columns(previewData.data.columns)
								.data(previewData.data.data)
								.chartType(previewData.type)
								.title(previewData.title)
								.faChar("\uf080")
								.size({ width: 210, height: 210 })
					);
				}
			}
			
			console.log(oldData);
			
			dashboardViz.graph
			 ._data
        	.vertices
        	.forEach(function(multiChartSurface) {
        		multiChartSurface.menu(["Configure", "Delete"]);
        		multiChartSurface._menu.click = function(option) {
        			var payload = {};
        			payload.chartId=multiChartSurface._title;
        			/*for(var i in multiChartSurface){
        				console.log(i+":"+multiChartSurface[i]);
        			}*/
        			if(option == 'Configure') {
        				//console.log(multiChartSurface._title);
        				zAu.send(new zk.Event(zk.Widget.$("$dashboardContainer"),'onEditChart', payload, {toServer:true}));
        				
        			} else if (option == 'Delete') {
        				//var wid = document.getElementById(multiChartSurface['_id']);
                        if (confirm("Are you sure want to delete the widget?") == true) {
                        	 console.log(multiChartSurface);
                             document.getElementById(multiChartSurface['_id']).style.display = 'none';                            
                             document.getElementById(multiChartSurface['_content']['_chart']['_id']).style.display = 'none';                        	
                             zAu.send(new zk.Event(zk.Widget.$("$dashboardContainer"),'onDeleteChart', payload, {toServer:true}));
                        } 
        				
        			}
        		}
        	});
			
			 dashboardViz.graph
         	.data(oldData)         	
         	.render();
			 
			 	

		});
	});
}

function deleteWidget(){							
	var newVertices = [];
	var oldData = dashboardViz.graph._data;
	for(var d in oldData.vertices){							
		if(oldData.vertices[d]['_title'] != previewData.title){
			newVertices.push(oldData.vertices[d]);							
		}
	}		
	var newData = {"vertices":newVertices,"edges":[]};
	console.log(oldData);
	dashboardViz.graph
	.data(newData)
	.render();	 					
}

function saveLayout(chartDivId){
	zAu.send(new zk.Event(zk.Widget.$(chartDivId), "onSave", dashboardViz.graph.serialize(), {toServer:true}));
}

