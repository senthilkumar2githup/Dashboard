(function (root, factory) {
    if (typeof define === "function" && define.amd) {
        define(["lib/dagre/dagre"], factory);
    } else {
        root.GraphData = factory(root.dagre);
    }
}(this, function (dagre) {
    function GraphData() {
        dagre.graphlib.Graph.call(this, { multigraph: true, compound: true });
        this.setGraph({});
        this.setDefaultEdgeLabel(function () { return {}; });
    };
    GraphData.prototype = Object.create(dagre.graphlib.Graph.prototype);

    GraphData.prototype.setData = function (vertices, edges, merge) {
        var context = this;
        var retVal = {
            addedVertices: [],
            addedEdges: []
        }

        //  Add new items  ---
        for (var i = 0; i < vertices.length; ++i) {
            var entity = vertices[i];
            if (!merge || !this.hasNode(entity._id)) {
                this.setNode(entity._id, entity)
                retVal.addedVertices.push(entity);
            }
        }
        for (var i = 0; i < edges.length; ++i) {
            var edge = edges[i];
            if (!merge || !this.hasEdge(edge._id)) {
                this.setEdge(edge._sourceVertex._id, edge._targetVertex._id, edge);
                retVal.addedEdges.push(edge);
            }
        }
        //  Remove old items  ---
        if (merge) {
            var edgeIDs = edges.map(function (item) { return item._id; });
            this.filterEdges(function (item) { return edgeIDs.indexOf(item.v + "_" + item.w) < 0; })
                .forEach(function (item) {
                    try {
                        context.delEdge(item);
                    } catch (e) {
                        var d = 0;
                    }
                })
            ;
            var vertexIDs = vertices.map(function (item) { return item._id; });
            this.filterNodes(function (item) { return vertexIDs.indexOf(item) < 0; })
                .forEach(function (item) {
                    try {
                        context.delNode(item);
                    } catch (e) {
                        var d = 0;
                    }
                })
            ;
        }
        return retVal;
    };

    GraphData.prototype.filterEdges = function (pred) {
        var filtered = [];
        this.eachEdge(function (e) {
            if (pred(e)) {
                filtered.push(e);
            }
        });
        return filtered;
    };

    GraphData.prototype.filterNodes = function (pred) {
        var filtered = [];
        this.eachNode(function (e) {
            if (pred(e)) {
                filtered.push(e);
            }
        });
        return filtered;
    };

    GraphData.prototype.nodeValues = function () {
        var retVal = [];
        this.nodes().forEach(function (item, idx) {
            retVal.push(this.node(item));
        }, this);
        return retVal;
    };

    GraphData.prototype.eachNode = function (callback) {
        this.nodes().forEach(function (item, idx) {
            callback(item, this.node(item));
        }, this);
    };

    GraphData.prototype.edgeValues = function () {
        var retVal = [];
        var context = this;
        this.edges().forEach(function (item, idx) {
            retVal.push(this.edge(item));
        }, this);
        return retVal;
    };

    GraphData.prototype.eachEdge = function (callback) {
        this.edges().forEach(function (item, idx) {
            callback(item, item.v, item.w, this.edge(item));
        }, this);
    };

    return GraphData;
}));
