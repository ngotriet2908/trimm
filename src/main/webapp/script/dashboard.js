'use strict';

document.addEventListener('DOMContentLoaded', function () {
    // create nicescroll instance
    // $("body").niceScroll({cursorcolor:"#86c232"});

    var grid = null;
    var docElem = document.documentElement;
    var gridElement = document.querySelector('.dashboard-tile-grid');

    var filterField = document.querySelector('.control-filter-field');
    var searchField = document.querySelector('.control-search-field');
    var sortField = document.querySelector('.control-sort-field');
    // var layoutField = document.querySelector('.layout-field');

    var filterFieldValue;
    var sortFieldValue;
    var layoutFieldValue;
    var searchFieldValue;

    var addItemsElement = document.querySelector('.add-more-items');
    var characters = 'abcdefghijklmnopqrstuvwxyz';
    var filterOptions = ['red', 'blue', 'green'];
    var dragOrder = [];
    var uuid = 0;


    // initialize grid
    function initGrid() {
        var dragCounter = 0;

        grid = new Muuri(gridElement, {
            // items: generateElements(10),
            layoutOnInit: false, // added
            layoutDuration: 400,
            layoutEasing: 'ease',
            dragEnabled: true,
            dragSortInterval: 50,
            // dragContainer: document.body,
            dragStartPredicate: function (item, event) {
                var isMap = event.target.id === "mapid";

                var isDraggable = sortFieldValue === 'order';
                var isRemoveAction = elementMatches(event.target, '.dashboard-card-remove, .dashboard-card-remove i');
                return (isDraggable && !isRemoveAction && !isMap) ? Muuri.ItemDrag.defaultStartPredicate(item, event) : false;
            },
            dragReleaseDuration: 400,
            dragReleaseEasing: 'ease'
        })
            .on('dragStart', function () {
                ++dragCounter;
                docElem.classList.add('dragging');
            })
            .on('dragEnd', function () {
                if (--dragCounter < 1) {
                    docElem.classList.remove('dragging');
                }
            })
            .on('move', function () {
                saveLayout(grid);
                updateIndices();
            })
            .on('sort', updateIndices);

        getLayoutFromServer();
    }

    // save layout
    function serializeLayout(grid) {
        var counter = 0;

        var itemIds = grid.getItems().map(function (item) {
            counter++;

            var card  = {
                "typeName": item.getElement().getAttribute('data-type'),
                "indicatorName": item.getElement().getAttribute('data-title')
            };

            return card;
        });
        //
        itemIds = {
            "count": counter,
            "layout": itemIds
        };

        return itemIds;
    }


    // get layout from server
    function getLayoutFromServer() {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function() {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    console.log("code 200");
                    console.log("layout retrieved.");

                    var parsedResponse = JSON.parse(http.response);

                    if (parsedResponse.cards.length < 1) {
                        console.log("No layout available.");
                        grid.layout(true);
                    } else {
                        // parsedResponse = JSON.parse(parsedResponse);
                        console.log(parsedResponse);
                        console.log("Loading layout from server.");
                        loadLayout(grid, parsedResponse);
                    }
                } else if (http.status === 401) {
                    console.log("code 400");
                } else {
                    console.log("something else: " + http.status);
                }
            }
        };

        http.open("GET", window.location.href + "/layout", true);
        http.setRequestHeader("Accept", "application/json");
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }


    function saveLayout(grid) {
        var layout = serializeLayout(grid);
        // window.localStorage.setItem('layout', layout);
        console.log("layout saved: " + layout);

        var layoutJson = JSON.stringify(layout);

        var http = new XMLHttpRequest();

        http.onreadystatechange = function() {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    console.log("code 200");

                    // TODO print some line in the bottom like "layout saved..."
                } else if (http.status === 401) {
                    console.log("code 400");
                } else {
                    console.log("something else: " + http.status);
                    console.log("layout saved.");
                }
            }
        };

        http.open("PUT", window.location.href + "/layout", true);
        http.setRequestHeader("Content-Type", "application/json");
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send(layoutJson);
    }

    function loadLayout(grid, serializedLayout) {
        var layout = serializedLayout;

        var item;

        console.log("loading layout: ");
        console.log(layout);

        for (var i = 0; i < layout.cards.length; i++) {
            item = layout.cards[i];

            if (item.cardTypeName === "graph") {
                addItem(item.cardTypeName, item.name, item, true)
            } else if (item.cardTypeName === "individual") {
                addItem(item.cardTypeName, item.name, item, true)
            } else if (item.cardTypeName === "distribution") {
                addItem(item.cardTypeName, item.name, item, true)
            }

            console.log("added: " + item);
        }
    }


    function filter() {
        filterFieldValue = filterField.value;
        grid.filter(function (item) {
            var element = item.getElement();
            var isSearchMatch = !searchFieldValue ? true : (element.getAttribute('data-title') || '').toLowerCase().indexOf(searchFieldValue) > -1;
            var isFilterMatch = !filterFieldValue ? true : (element.getAttribute('data-type') || '') === filterFieldValue;
            return isSearchMatch && isFilterMatch;
        });
    }

    function sort() {
        // Do nothing if sort value did not change.
        var currentSort = sortField.value;
        if (sortFieldValue === currentSort) {
            return;
        }

        // If we are changing from "order" sorting to something else
        // let's store the drag order.
        if (sortFieldValue === 'order') {
            dragOrder = grid.getItems();
        }

        // Sort the items.
        grid.sort(
            currentSort === 'title' ? compareItemTitle :
                currentSort === 'color' ? compareItemColor :
                    dragOrder
        );

        // Update indices and active sort value.
        updateIndices();
        sortFieldValue = currentSort;
    }


    function requestDataForIndicator(typeName, indicatorName, initialLayout) {
        // var params = "indicator=" + indicator;

        var http = new XMLHttpRequest();

        http.onreadystatechange = function() {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    console.log("code 200");

                    var parsedResponse = JSON.parse(http.response);

                    // insert into html
                    addItem(typeName, indicatorName, parsedResponse, initialLayout);
                } else if (http.status === 401) {
                    console.log("code 400");


                } else {
                    console.log("something else...");
                }
            }
        };

        if (typeName === "graph") {
            http.open("GET", window.location.href + "/graph/50/" + indicatorName, true);
        } else if (typeName === "distribution") {
            http.open("GET", window.location.href + "/distribution/" + indicatorName, true); // TODO
        } else {
            http.open("GET", window.location.href + "/indicator/" + indicatorName, true); // TODO
        }

        http.setRequestHeader('Accept', 'application/json');
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }


    function addItem(typeName, indicatorName, data, initialLayout) {
        // Generate new elements.
        console.log("generating element with id: " + (uuid + 1) + " of type: " + typeName);

        var newElement = generateElement(++uuid, indicatorName, getRandomItem(["red", "green", "blue"]), typeName, data);

        // Set the display of the new elements to "none" so it will be hidden by
        // default.
        newElement.style.display = 'none';

        // Add the elements to the grid.
        var newItem = grid.add(newElement);

        // Update UI indices.
        updateIndices();

        // Sort the items only if the drag sorting is not active.
        if (sortFieldValue !== 'order') {
            grid.sort(sortFieldValue === 'title' ? compareItemTitle : compareItemColor);
            dragOrder = dragOrder.concat(newItem);
        }

        // Finally save layout and filter the items

        if (!initialLayout) {
            saveLayout(grid);
        }

        filter();

        if (typeName === "map") {
            initMap();
        } else if (typeName === "graph") {
            initGraph(newElement, data);
        } else if (typeName === "distribution") {
            initDistribution(newElement, data); // TODO
        }
    }

    function removeItem(e) {
        var elem;
        if (e.target === undefined) {
            elem = e;
        } else {
            elem = elementClosest(e.target, '.dashboard-card');
        }

        grid.hide(elem, {
            onFinish: function (items) {
                var item = items[0];
                grid.remove(item, {removeElements: true});
                if (sortFieldValue !== 'order') {
                    var itemIndex = dragOrder.indexOf(item);
                    if (itemIndex > -1) {
                        dragOrder.splice(itemIndex, 1);
                    }
                }

                saveLayout(grid);
                updateIndices();
            }
        });
    }

    // change the layout type
    // function changeLayout() {
    //     layoutFieldValue = layoutField.value;
    //     grid._settings.layout = {
    //         horizontal: false,
    //         alignRight: layoutFieldValue.indexOf('right') > -1,
    //         alignBottom: layoutFieldValue.indexOf('bottom') > -1,
    //         fillGaps: layoutFieldValue.indexOf('fillgaps') > -1
    //     };
    //     grid.layout();
    // }

    function generateElement(id, title, color, type, data) {
        var itemElem = document.createElement('div');

        var height, width, innerContent;

        switch(type) {
            case "individual":
                height = 1;
                width = 1;
                innerContent = '<div class="dashboard-card-front">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + data.name + // here was an id TODO
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="material-icons">&#xE5CD;</i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-single">' +

                    '<div>' +
                    '<span>' + data.average + '</span>' +
                    '</div>' +
                    '' +
                    '<div>' +
                    '<span>min ' + data.minimum + '</span>' +
                    '</div>' +
                    '' +
                    '<div>' +
                    '<span>max ' + data.maximum + '</span>' +
                    '</div>' +

                    '</div>' +
                    '</div>';
                break;
            case "individual-high":
                height = 2;
                width = 1;
                innerContent = '<div class="dashboard-card-front">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + id +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="material-icons">&#xE5CD;</i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-single">' +

                    '<div>' +
                    '<span>' + title + '</span>' +
                    '</div>' +
                    '' +
                    '<div>' +
                    '<span>min 29.8</span>' +
                    '</div>' +
                    '' +
                    '<div>' +
                    '<span>max 44.0</span>' +
                    '</div>' +

                    '</div>' +
                    '</div>';
                break;
            case "individual-big":
                height = 2;
                width = 2;
                innerContent = '<div class="dashboard-card-front">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + id +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="material-icons">&#xE5CD;</i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-single">' +

                    '<div>' +
                    '<span>' + title + '</span>' +
                    '</div>' +
                    '' +
                    '<div>' +
                    '<span>min 29.8</span>' +
                    '</div>' +
                    '' +
                    '<div>' +
                    '<span>max 44.0</span>' +
                    '</div>' +

                    '</div>' +
                    '</div>';
                break;
            case "graph":
                height = 2;
                width = 2;
                innerContent = '<div class="dashboard-card-front dashboard-card-front-graph">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + data.name +
                    '' +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="material-icons">&#xE5CD;</i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-graph">' +
                    '<canvas></canvas>' +
                    '</div>' +
                    '</div>';

                // TODO load graph

                break;
            case "distribution": // TODO
                height = 2;
                width = 2;
                innerContent = '<div class="dashboard-card-front dashboard-card-front-graph">' + // TODO
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + data.name +
                    '' +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="material-icons">&#xE5CD;</i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-graph">' + // TODO
                    '<canvas></canvas>' +
                    '</div>' +
                    '</div>';
                break;
            case "map":
                height = 2;
                width = 2;
                innerContent = '<div class="dashboard-card-front dashboard-card-front-map">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + id +
                    '' +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="material-icons">&#xE5CD;</i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-map">' +
                    '<div id="mapid"></div>' +
                    '</div>' +
                    '</div>';

                // TODO load map (check if map is not already added)

                break;
        }

        // var classNames = 'dashboard-card h' + height + ' w' + width + ' ' + color;
        var classNames = 'dashboard-card h' + height + ' w' + width;

        var itemTemplate = '' +
            '<div class="' + classNames + '" data-id="' + id + '" data-title="' + data.name + '" data-type="' + type + '">' +
            '<div class="dashboard-card-inner">' + innerContent +
            '</div>' +
            '</div>';

        itemElem.innerHTML = itemTemplate;
        return itemElem.firstChild;
    }

    // random generators for new random items
    function getRandomItem(collection) {
        return collection[Math.floor(Math.random() * collection.length)];
    }

    // sorting and filtering functions
    function compareItemTitle(a, b) {
        var aVal = a.getElement().getAttribute('data-title') || '';
        var bVal = b.getElement().getAttribute('data-title') || '';
        return aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
    }

    function compareItemColor(a, b) {
        var aVal = a.getElement().getAttribute('data-type') || '';
        var bVal = b.getElement().getAttribute('data-type') || '';
        return aVal < bVal ? -1 : aVal > bVal ? 1 : compareItemTitle(a, b);
    }

    // updating ids of items when layout changes
    function updateIndices() {
        var maxIndex = 1;
        grid.getItems().forEach(function (item, i) {
            item.getElement().setAttribute('data-id', i + 1);
            // item.getElement().querySelector('.dashboard-card-id').innerHTML = i + 1;
            maxIndex = i + 1;
        });
    }

    // sorting and filtering function
    function elementMatches(element, selector) {
        var p = Element.prototype;
        return (p.matches || p.matchesSelector || p.webkitMatchesSelector || p.mozMatchesSelector || p.msMatchesSelector || p.oMatchesSelector).call(element, selector);
    }

    // getting the closest element that triggered the evt
    function elementClosest(element, selector) {
        if (window.Element && !Element.prototype.closest) {
            var isMatch = elementMatches(element, selector);
            while (!isMatch && element && element !== document) {
                element = element.parentNode;
                isMatch = element && element !== document && elementMatches(element, selector);
            }
            return element && element !== document ? element : null;
        }
        else {
            return element.closest(selector);
        }
    }

    function initControls() {
        // Reset field values.
        searchField.value = "";
        [sortField, filterField].forEach(function (field) {
            field.value = field.querySelectorAll('option')[0].value;
        });

        // Set initial search query, active filter, active sort value and active layout.
        searchFieldValue = searchField.value.toLowerCase();
        filterFieldValue = filterField.value;
        sortFieldValue = sortField.value;
        // layoutFieldValue = layoutField.value;

        // Search field binding.
        searchField.addEventListener('keyup', function () {
            var newSearch = searchField.value.toLowerCase();
            if (searchFieldValue !== newSearch) {
                searchFieldValue = newSearch;
                filter();
            }
        });

        // Filter, sort and layout bindings.
        filterField.addEventListener('change', filter);
        sortField.addEventListener('change', sort);
        // layoutField.addEventListener('change', changeLayout);
    }

    // Add/remove items bindings.
    // addItemsElement.addEventListener('click', addItems);
    gridElement.addEventListener('click', function (e) {
        if (elementMatches(e.target, '.dashboard-card-remove, .dashboard-card-remove i')) {
            removeItem(e);
        }
    });

    // start
    initControls();
    initGrid();

    // map
    function initMap() {
        console.log("loading map...");
        var mymap = L.map('mapid').setView([51.505, -0.09], 13);

        L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
            attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, ' +
                '<a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
            maxZoom: 18,
            id: 'mapbox.streets',
            accessToken: 'pk.eyJ1Ijoic2t5d2Fsa2VydWEiLCJhIjoiY2p2d2w3MmZzMDlmczQ4cnhjeW43aWNraSJ9.v3yPqOHBFqO74s7kNcUfVA'
        }).addTo(mymap);

        var pointA = new L.LatLng(51.505, -0.09);
        var pointB = new L.LatLng(51.520, -0.10);
        var pointList = [pointA, pointB];

        var firstpolyline = new L.Polyline(pointList, {
            color: 'red',
            weight: 3,
            opacity: 0.5,
            smoothFactor: 1
        });
        firstpolyline.addTo(mymap);
    }

    // add graph
    function initGraph(element, data) {
        var ctx = element.querySelector("canvas").getContext('2d');

        var lineChartData = {
            labels: data.step_no,
            datasets: [{
                label: "left",
                borderColor: 'rgb(255, 99, 132)',
                backgroundColor: 'rgb(255, 99, 132)',
                fill: false,
                data: data.left,
                yAxisID: 'y-axis-1'
            },
                {
                label: "right",
                borderColor: 'rgb(54, 162, 235)',
                backgroundColor: 'rgb(54, 162, 235)',
                fill: false,
                data: data.right,
                yAxisID: 'y-axis-2'
            }]
        };

        window.myLine = Chart.Line(ctx, {
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
                    }, {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        id: 'y-axis-2',

                        // grid line settings
                        gridLines: {
                            drawOnChartArea: false,
                        },
                    }],
                }
            }
        });

    }


    function initDistribution(element, data) {
        var ctx = element.querySelector("canvas").getContext('2d');

        var barChartData = {
            labels: data.pointX,
            datasets: [{
                borderColor: 'rgb(54, 162, 235)',
                backgroundColor: 'rgb(54, 162, 235)',
                borderWidth: 1,
                data: data.pointY
            }]

        };


        var myBarChart = new Chart(ctx, {
            type: 'bar',
            data: barChartData,
            responsive: true,
            options: {
                legend: {
                    display: false
                },
                tooltips: {
                    callbacks: {
                        label: function(tooltipItem) {
                            return tooltipItem.yLabel;
                        }
                    }
                },
                scales: {
                    xAxes: [{
                        barPercentage: 0.5,
                        barThickness: 6,
                        maxBarThickness: 8,
                        minBarLength: 2,
                        gridLines: {
                            offsetGridLines: true
                        }
                    }],
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'steps'
                        }
                    }]
                },
            }
        })
    }


    $(".popup-overlay").on("click", function(event) {
        // hide
        document.querySelector(".popup-overlay").classList.add("hidden");

        // remove all dialogs
        document.querySelector(".remove-all-dialog").classList.add("hidden");
        document.querySelector(".add-dialog").classList.add("hidden");
        document.querySelector(".eink-sent-dialog").classList.add("hidden");
    });

    $("#controls #add").on("click", function(event) {
        // show overlay
        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".add-dialog").classList.remove("hidden");
    });


    $(".add-dialog .add").on("click", function(event) {
        var selectFieldTypeValue = document.querySelector(".add-dialog #select-type").value;
        var selectFieldIndicatorValue = document.querySelector(".add-dialog #select-indicator").value;

        var selectFieldSideValue = $(".add-dialog input[name='select-side']:checked").val();

        if (selectFieldTypeValue !== "graph") {
            selectFieldIndicatorValue += selectFieldSideValue;
        }

        console.log(selectFieldTypeValue);
        console.log(selectFieldIndicatorValue);

        requestDataForIndicator(selectFieldTypeValue, selectFieldIndicatorValue, false);
    });


    $('#controls #export-eink button').on('click', function(event) {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {
                    console.log("code 200");
                    // TODO show email sent!

                    // show overlay
                    document.querySelector(".popup-overlay").classList.remove("hidden");

                    // show popup
                    document.querySelector(".eink-sent-dialog").classList.remove("hidden");

                } else if (http.status === 401) {
                    console.log("code 401");


                } else {
                    console.log("something else...");
                }
            }
        };

        http.open("GET", window.location.href + "/export/kindle", true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();

    });


    $("#controls #remove-all").on("click", function(event) {
        // show overlay
        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".remove-all-dialog").classList.remove("hidden");
    });

    // remove all button
    $('.remove-all-dialog .remove-all').on('click', function(event) {
        var items = document.querySelectorAll(".dashboard-card");

        console.log(items[0]);
        //
        for (var i = 0; i < items.length; i++) {
            removeItem(items[i]);
        }
        //
        console.log("removed layout.");

        // hide popup
        document.querySelector(".remove-all-dialog").classList.add("hidden");

        // hide overlay
        document.querySelector(".popup-overlay").classList.add("hidden");
    });

    $(".cancel").on("click", function(event) {
        // hide
        document.querySelector(".popup-overlay").classList.add("hidden");

        // remove all dialogs
        document.querySelector(".remove-all-dialog").classList.add("hidden");
        document.querySelector(".add-dialog").classList.add("hidden");
        document.querySelector(".eink-sent-dialog").classList.add("hidden");
    });

});

