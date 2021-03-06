(function (root, factory) {
    if (typeof define === "function" && define.amd) {
        define([], factory);
    } else {
        root.I2DChart = factory();
    }
}(this, function () {
    function I2DChart() {
    };

    //  Data ---
    I2DChart.prototype.testData = function () {
        this.columns(["Subject", "Year 1", "Year 2"]);//, "__viz_faChar"]);
        this.data([
            ["Geography", 75, 68],
            ["English", 45, 55],
            ["Math", 98, 92],
            ["Science", 66, 60]//, "\uf0c3"] //  http://fortawesome.github.io/Font-Awesome/cheatsheet/
        ]);
        return this;
    };
	

    //  Properties  ---
    //TODO:  I2DChart.prototype._palette = "category20";

    //  Events  ---
    I2DChart.prototype.click = function (d) {
        console.log("Click:  " + d.label);
    };

    return I2DChart;
}));
