function createVisualization(url, ids) {
	console.log("Creating visualisation - " + url + " ids - " + ids);
	
	ids = jq.parseJSON(ids);
	
	console.log(ids);
	
	require([ "js/Visualization/widgets/config" ], function() {

		requirejs.config({
			baseUrl : "js/Visualization/widgets"
		});

		require(["src/other/Comms", "src/common/Surface", "src/common/Text",
				"src/common/TextBox", "src/common/Shape", "src/common/FAChar",
				"src/common/Icon", "src/common/List", "src/common/Menu",
				"src/common/Palette", "src/graph/Graph", "src/graph/Edge",
				"src/graph/Vertex", "src/chart/MultiChartSurface",
				"src/c3/Pie", "src/chart/Bubble", "src/map/ChoroplethStates",
				"src/map/GMap", "src/marshaller/dashboard",
				"src/marshaller/Graph", "src/other/MorphText",
				"src/other/Slider", "src/other/Table" ], function(Comms,
				Surface, Text, TextBox, Shape, FAChar, Icon, List, Menu,
				Palette, Graph, Edge, Vertex, MultiChartSurface, Pie, Bubble,
				ChoroplethStates, GMap, DashboardMarshaller, GraphMarshaller,
				MorphText, Slider, Table) {
			try {
				dashboardMarshaller = new DashboardMarshaller();
								
				dashboardMarshaller.url(url);
				dashboardMarshaller.render(ids);

			} catch (e) {
				console.log(e.message);
				alert(e.message);
			}
		});
	});
}