document.addEventListener('DOMContentLoaded', function () {

    var chart;
    var ctx = document.querySelector("#chart-compare canvas").getContext('2d');


    initEmptyGraph();

    function getLines() {
        var res = "";
        $.each($("input[name='runID']:checked"), function() {
            res = res + $(this).val() + "x";
        });
        return res;
    }

    $("#compare-button").on('click', function(event) {
        console.log("init request for graph");
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    console.log("code 200");
                    // document.getElementById("chart-compare c").innerHTML = "<canvas id=\"chart-compare-canvas\"></canvas>";

                    initCompareGraph(JSON.parse(http.response));

                } else {
                    console.log("something else...");
                }
            }
        };

        var selectFieldSideValue = $("#select-side-compare input[name='select-side']:checked").val();


        http.open("GET", "compare/graph/" + getLines()
            +"/" + document.getElementById("select-indicator-compare").value
            + selectFieldSideValue , true);
        http.setRequestHeader('Accept', 'application/json');
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    });

    function getRandomColor() {
        var letters = '0123456789ABCDEF'.split('');
        var color = '#';
        for (var i = 0; i < 6; i++ ) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        console.log(color);
        return color;
    }

    function addElementToDataset(lineChart, response) {
        console.log(response);

        for(i = 0; i < response.length; i++) {
            var dataElement = {
                label: response[i].name,
                fill: false,
                borderColor : getRandomColor(),
                data: response[i].stepData,
                yAxisID: 'y-axis-1'
            };
            lineChart.datasets.push(dataElement);
        }
        // lineChart.update();
    }

    function getXaxesLineChart(length) {
        var arrayy = [];
        for(i = 0; i < length; i++) {
            arrayy.push(i);
        }
        return arrayy;
    }

    function initEmptyGraph() {
        chart = Chart.Line(ctx, {
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
            labels: getXaxesLineChart(100),
            datasets: []
        };

        addElementToDataset(lineChartData, response);

        chart = Chart.Line(ctx, {
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


    function addData(chart, label, data) {
        chart.data.labels.push(label);
        chart.data.datasets.forEach((dataset) => {
            dataset.data.push(data);
        });
        chart.update();
    }

    function removeData(chart) {
        chart.data.labels.pop();
        chart.data.datasets.forEach((dataset) => {
            dataset.data.pop();
        });
        chart.update();
    }

    if ($("#compare").length > 0) {
        //TODO add compare

        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    console.log(http.response);

                    var tmp = document.getElementById("compare-checkbox");
                    var parseResponse = JSON.parse(http.response);

                    for(i = 0; i < parseResponse.runsList.length; i++) {
                        tmp.innerHTML += "<label><input type='checkbox' value=" +parseResponse.runsList[i].id
                            + " name='runID'>" + parseResponse.runsList[i].name + "</label>";
                    }

                } else if (http.status === 401) {
                    console.log("code 401");
                } else if (http.status === 402) {
                    console.log("code 402");
                } else {
                    console.log("something else...");
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