(function (root, factory) {
    if (typeof define === "function" && define.amd) {
        define([], factory);
    } else {
        root.DashboardChart = factory();
    }
}(this, function () {
    function DashboardChart() {
    };
   
	
	//  Data ---
    DashboardChart.prototype.setChartData = function (actualData,chartType) {        
		
		var myData = actualData;
		var subject = ["subject"];
		var column = myData['data']['rows'][0];
		var subjectwithColumn = subject.concat(column); 			
		this.columns(subjectwithColumn);
			
        if (chartType == 'pie')
		{
			var pieChartData=[];			
			for(i = 0; i<myData['data']['rows'][0].length; i++)	{	
				var newData=[];
				newData[0] = myData['data']['rows'][0][i];
				newData[1] = myData['data']['rows'][1][i];						
				pieChartData[i] = newData;
			}			
			this.data(pieChartData);
		}	  
		
		if (chartType == 'line' || chartType == 'bar')
		{	
			var ChartData=new Array();			 
			for (i=0;i<myData['axis']['x']['categories'].length;i++)
			{				
				var columnSubject = [];
				columnSubject[0] = myData['axis']['x']['categories'][i];				
				var columnInfo = myData['data']['rows'][i+1];				
				ChartData[i]=columnSubject.concat(columnInfo);				
			}
			this.data(ChartData);
		}		
		
        return this;
    };

    //  Properties  ---
    //TODO:  DashboardChart.prototype._palette = "category20";

    //  Events  ---
    DashboardChart.prototype.click = function (d) {
        console.log("Click:  " + d.label);
    };

    return DashboardChart;
}));
