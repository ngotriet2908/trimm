document.addEventListener('DOMContentLoaded', function () {
    var chart;
    var canvasContainer = document.querySelector("#chart-compare");

    canvasContainer.innerHTML = "<canvas></canvas>";
    initEmptyGraph();

    function getLines() {
        var res = "";
        $.each($("input[name='runID']:checked"), function () {
            res = res + $(this).val() + "x";
        });
        return res;
    }

    $("#compare-button").on('click', function (event) {
        var lines = getLines();

        if (lines !== "") {

            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200) {
                        canvasContainer.innerHTML = "<canvas></canvas>";
                        initCompareGraph(JSON.parse(http.response));
                    } else {
                        console.log("Response: " + http.status);
                    }
                }
            };

            var selectFieldSideValue = $("#select-side-compare input[name='select-side']:checked").val();

            http.open("GET", "compare/graph/" + lines
                + "/" + document.getElementById("select-indicator-compare").value
                + selectFieldSideValue, true);
            http.setRequestHeader('Accept', 'application/json');
            http.setRequestHeader('Cache-Control', 'no-store');
            http.send();
        }
    });

    function getRandomColor() {
        var letters = '0123456789ABCDEF'.split('');
        var color = '#';
        for (var i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        console.log(color);
        return color;
    }

    function addElementToDataset(lineChart, response) {
        console.log(response);

        for (i = 0; i < response.length; i++) {
            var dataElement = {
                label: response[i].name,
                fill: false,
                borderColor: getRandomColor(),
                data: response[i].stepData,
                yAxisID: 'y-axis-1'
            };
            lineChart.datasets.push(dataElement);
        }
    }

    function getXaxisLineChart(length) {
        var arrayy = [];
        for (i = 0; i < length; i++) {
            arrayy.push(i);
        }
        return arrayy;
    }

    function initEmptyGraph() {
        chart = Chart.Line(canvasContainer.querySelector("canvas").getContext('2d'), {
            options: {
                responsive: true,
                spanGaps: true,
                hoverMode: 'index',
                stacked: false,
                elements: {
                    point: {
                        radius: 0
                    }
                },
                scales: {
                    yAxes: [{
                        type: 'linear',
                        display: true,
                        position: 'left',
                        id: 'y-axis-1',
                    }],
                }
            }
        });

    }


    function initCompareGraph(response) {
        var lineChartData = {
            labels: getXaxisLineChart(100),
            datasets: []
        };

        addElementToDataset(lineChartData, response);

        chart = Chart.Line(canvasContainer.querySelector("canvas").getContext('2d'), {
            data: lineChartData,
            options: {
                responsive: true,
                spanGaps: true,
                hoverMode: 'index',
                stacked: false,
                elements: {
                    point: {
                        radius: 0
                    }
                },
                scales: {
                    yAxes: [{
                        type: 'linear',
                        display: true,
                        position: 'left',
                        id: 'y-axis-1',
                    }],
                }
            }
        });

    }

    if ($("#compare").length > 0) {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    var tmp = document.getElementById("compare-checkbox");
                    var parseResponse = JSON.parse(http.response);

                    for (i = 0; i < parseResponse.runsList.length; i++) {
                        tmp.innerHTML += "<label><input type='checkbox' value=" + parseResponse.runsList[i].id
                            + " name='runID'>" + parseResponse.runsList[i].id + ": " + parseResponse.runsList[i].name + "</label>";
                    }

                } else if (http.status === 402) {

                } else {
                    console.log("Response: " + http.status);
                }
            }
        };

        http.open("GET", "/runner/compare/select", true);
        http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        http.setRequestHeader('Accept', 'application/json');
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }


});