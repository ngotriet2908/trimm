'use strict';

document.addEventListener('DOMContentLoaded', function () {
    // create nicescroll instance
    // $("body").niceScroll({cursorcolor:"#86c232"});

    var currentUrl = window.location.href;

    var grid = null;
    var docElem = document.documentElement;
    var gridElement = document.querySelector('.dashboard-tile-grid');

    var dragEnabled;

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
    var dragOrder = [];
    var uuid = 0;

    var tokenJson = getTokenData(getCookieValue("token"));
    var usernameFromToken;

    if (tokenJson !== null) {
        usernameFromToken = tokenJson.sub;
    }

    if (document.querySelector("html").offsetWidth <= 400) {
        dragEnabled = false;
    } else {
        // enable
        dragEnabled = true;
    }


    function getTokenData(token) {
        if (token === null) {
            return null;
        }

        var encoded = token.split(".")[1];

        console.log(encoded);

        var decoded = atob(encoded);

        var decodedJson = JSON.parse(decoded);

        return decodedJson;
    }

    function getCookieValue(cookieName) {
        var name = cookieName + "=";
        var cookies = document.cookie.split(';');
        for (var i = 0; i < cookies.length; i++) {
            var cookie = cookies[i];
            while (cookie.charAt(0) === ' ') {
                cookie = cookie.substring(1, cookie.length);
            }

            if (cookie.indexOf(name) === 0) {
                return cookie.substring(name.length, cookie.length);
            }
        }

        return null;
    }

    // initialize grid
    function initGrid() {
        var dragCounter = 0;

        grid = new Muuri(gridElement, {
            // items: generateElements(10),
            layoutOnInit: false, // added
            layoutDuration: 400,
            layoutEasing: 'ease',
            dragEnabled: dragEnabled,
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

            var card = {
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

        http.onreadystatechange = function () {
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


    function getJSONlayout(grid) {
        var layout = serializeLayout(grid);
        // window.localStorage.setItem('layout', layout);
        console.log("layout saved: " + layout);

        var layoutJson = JSON.stringify(layout);
        return layoutJson;
    }

    function saveLayout(grid) {
        var layout = serializeLayout(grid);
        // window.localStorage.setItem('layout', layout);
        console.log(layout);

        var layoutJson = JSON.stringify(layout);

        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
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
                dragOrder
        );

        // Update indices and active sort value.
        updateIndices();
        sortFieldValue = currentSort;
    }


    function requestDataForIndicator(typeName, indicatorName, initialLayout) {
        // var params = "indicator=" + indicator;

        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
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

        var newElement = generateElement(++uuid, indicatorName, typeName, data);

        // Set the display of the new elements to "none" so it will be hidden by
        // default.
        newElement.style.display = 'none';

        // Add the elements to the grid.
        var newItem = grid.add(newElement);

        // Update UI indices.
        updateIndices();

        // Sort the items only if the drag sorting is not active.
        if (sortFieldValue !== 'order') {
            grid.sort('title');
            dragOrder = dragOrder.concat(newItem);
        }

        // Finally save layout and filter the items

        if (!initialLayout) {
            saveLayout(grid);
        }

        filter();

        if (typeName === "graph" && indicatorName !== "speed") {
            initGraph(newElement, data);
        } else if (typeName === "distribution") {
            initDistribution(newElement, data); // TODO
        } else if (indicatorName === "speed") {
            initSpeedGraph(newElement, data); // TODO
        }
    }

    function removeItem(e, saveLayoutFlag) {
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

                if (saveLayoutFlag) {
                    saveLayout(grid);
                }

                updateIndices();
            }
        });
    }


    function generateElement(id, title, type, data) {
        var itemElem = document.createElement('div');

        var height, width, innerContent;
        var nameSplit = data.name.split("_");

        if (nameSplit[1] === undefined) {
            nameSplit[1] = "";
        }

        switch (type) {
            case "individual":
                height = 1;
                width = 1;

                innerContent = '<div class="dashboard-card-front">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + nameSplit[0] + " " + nameSplit[1] + // here was an id TODO
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
                    '<h3 class="dashboard-card-id">' + nameSplit[0] + " " + nameSplit[1] + " Graph" +
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
                    '<h3 class="dashboard-card-id">' + nameSplit[0] + " " + nameSplit[1] + " Distribution" +
                    '' +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="material-icons">&#xE5CD;</i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-graph">' + // TODO
                    '<canvas></canvas>' +
                    '</div>' +
                    '</div>';
                break;
        }

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


    // updating ids of items when layout changes
    function updateIndices() {
        var maxIndex = 1;
        grid.getItems().forEach(function (item, i) {
            item.getElement().setAttribute('data-id', i + 1);
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
            removeItem(e, true);
        }
    });

    // start
    initControls();
    initGrid();


    // add graph
    function initGraph(element, data) {
        var ctx = element.querySelector("canvas").getContext('2d');

        var lineChartData = {
            labels: data.step_no,
            datasets: [{
                label: data.name,
                borderColor: 'rgb(255, 99, 132)',
                backgroundColor: 'rgb(255, 99, 132)',
                fill: false,
                data: data.left,
                yAxisID: 'y-axis-1'
            }, {
                label: "baseline",
                borderColor: 'rgb(54, 162, 235)',
                backgroundColor: 'rgb(54, 162, 235)',
                fill: false,
                data: data.right,
                yAxisID: 'y-axis-1'
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
                        label: function (tooltipItem) {
                            return tooltipItem.yLabel;
                        }
                    }
                },
                scales: {
                    xAxes: [{
                        barPercentage: 0.5,
                        barThickness: 4,
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

    // add graph
    function initSpeedGraph(element, data) {
        var ctx = element.querySelector("canvas").getContext('2d');

        var lineChartData = {
            labels: data.step_no,
            datasets: [{
                label: "speed",
                borderColor: 'rgb(255, 99, 132)',
                backgroundColor: 'rgb(255, 99, 132)',
                fill: false,
                data: data.left,
                yAxisID: 'y-axis-1'
            }]
        };

        window.myLine = Chart.Line(ctx, {
            data: lineChartData,
            options: {
                legend: {
                    display: false
                },
                tooltips: {
                    callbacks: {
                        label: function (tooltipItem) {
                            return tooltipItem.yLabel;
                        }
                    }
                },
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


    $(".popup-overlay").on("click", function (event) {
        // hide
        document.querySelector(".popup-overlay").classList.add("hidden");

        // remove all dialogs
        document.querySelector(".remove-all-dialog").classList.add("hidden");
        document.querySelector(".add-dialog").classList.add("hidden");
        document.querySelector(".eink-sent-dialog").classList.add("hidden");
    });

    $("#controls #add").on("click", function (event) {
        // show overlay
        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".add-dialog").classList.remove("hidden");
    });

    $("#select-type").on("change", function (event) {
        var typeField = document.querySelector("#select-type");
        var indicatorField = document.querySelector("#select-indicator");
        console.log(typeField.value);
        console.log(indicatorField.value);

        if (typeField.value === "individual") {
            indicatorField.querySelector("option[value='speed']").remove();

            $("#select-side input").attr("disabled", false);
            $("#select-side div:nth-of-type(1) input").attr("checked", true);
            document.querySelector("#select-side").classList.remove("disabled");
        } else if (typeField.value === "graph" || typeField.value === "distribution") {

            if (indicatorField.querySelector("option[value='speed']") == null) {
                var newOption = document.createElement("option");
                newOption.setAttribute("value", "speed");
                newOption.textContent = "Speed";
                indicatorField.appendChild(newOption);
            }

            if (indicatorField.value === "speed") {
                $("#select-side input").attr("disabled", true);
                $("#select-side input").attr("checked", false);
                document.querySelector("#select-side").classList.add("disabled");
            } else {
                $("#select-side input").attr("disabled", false);
                $("#select-side div:nth-of-type(1) input").attr("checked", true);
                document.querySelector("#select-side").classList.remove("disabled");
            }
        }
    });

    $("#select-indicator").on("change", function (event) {
        var typeField = document.querySelector("#select-type");
        var indicatorField = document.querySelector("#select-indicator");

        if (typeField.value === "distribution" || typeField.value === "graph") {

            if (indicatorField.value !== "speed") {
                $("#select-side input").attr("disabled", false);
                $("#select-side div:nth-of-type(1) input").attr("checked", true);
                document.querySelector("#select-side").classList.remove("disabled");
            } else {
                $("#select-side input").attr("disabled", true);
                $("#select-side input").attr("checked", false);
                document.querySelector("#select-side").classList.add("disabled");
            }
        }
    });

    $(".add-dialog .add").on("click", function (event) {
        var selectFieldTypeValue = document.querySelector(".add-dialog #select-type").value;
        var selectFieldIndicatorValue = document.querySelector(".add-dialog #select-indicator").value;

        var selectFieldSideValue = $(".add-dialog input[name='select-side']:checked").val();

        if (!(selectFieldTypeValue === "distribution" && selectFieldIndicatorValue === "speed")
            && !(selectFieldTypeValue === "graph" && selectFieldIndicatorValue === "speed")) {
            selectFieldIndicatorValue += selectFieldSideValue;
        }

        console.log(selectFieldTypeValue);
        console.log(selectFieldIndicatorValue);

        requestDataForIndicator(selectFieldTypeValue, selectFieldIndicatorValue, false);
    });


    $('#controls #export-eink button').on('click', function (event) {
        // show overlay
        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".eink-sent-dialog").classList.remove("hidden");
    });


    $('#controls #select-layout button').on('click', function (event) {
        if (document.querySelector(".premium-required") == null) {
            var ss = document.getElementById("select-layout-selection");
            document.getElementById('save-layout-name-field').value = ss[ss.selectedIndex].value;
        }

        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".select-layout-dialog").classList.remove("hidden");


    });


    $('#controls #select-favorite-layout button').on('click', function (event) {

        if (document.querySelector(".premium-required") == null) {
            var ss = document.getElementById("favorite-layout-selection");

            if (ss[ss.selectedIndex] !== null && ss[ss.selectedIndex] !== undefined) {
                document.getElementById('favorite-layout-name-field').value = ss[ss.selectedIndex].value;
            }
        }

        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".favorite-layout-dialog").classList.remove("hidden");

    });

    $('#select-layout-selection').on('change', function (event) {
        var ss = document.getElementById("select-layout-selection");
        document.getElementById('save-layout-name-field').value = ss[ss.selectedIndex].value;
    });


    $('#favorite-layout-selection').on('change', function (event) {
        var ss = document.getElementById("favorite-layout-selection");
        document.getElementById('favorite-layout-name-field').value = ss[ss.selectedIndex].value;
    });

    $('#save-layout-name').on('click', function (event) {
        var layout_name = $("#save-layout-name-field").val();
        console.log(layout_name);
        if (layout_name !== "") {
            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200 || http.status === 204) {
                        console.log(http.status);
                        // TODO show email sent!
                        console.log("sent");
                        // location.reload();
                        ss[ss.selectedIndex].innerText = layout_name;

                    } else if (http.status === 401) {
                        console.log("code 401");


                    } else {
                        console.log("something else...");
                    }
                }
            };
            var ss = document.getElementById("select-layout-selection");
            http.open("PUT", window.location.href + "/rename_layout/"
                + ss[ss.selectedIndex].getAttribute('val') + "/" + layout_name, true);
            http.setRequestHeader('Cache-Control', 'no-store');
            http.send();
        }
    });

    $('.favorite-layout-dialog .saveFavourite').on("click", function (event) {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {

                    // location.reload();
                    console.log("restored to default.")

                    document.querySelector(".popup-overlay").classList.add("hidden");

                    // hide popup
                    document.querySelector(".favorite-layout-dialog").classList.add("hidden");

                } else if (http.status === 401) {
                    console.log("code 401");


                } else {
                    console.log("something else...");
                }
            }
        };

        var ss = document.getElementById("favorite-layout-selection");

        http.open("PUT", window.location.href + "/save_favorite/" +
            ss[ss.selectedIndex].getAttribute('val') + "/", true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    });

    $('#favorite-layout-name').on('click', function (event) {
        var layout_name = $("#favorite-layout-name-field").val();
        console.log(layout_name);
        if (layout_name !== "") {
            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200 || http.status === 204) {
                        console.log(http.status);
                        // TODO show email sent!
                        console.log("sent");
                        // location.reload();
                        ss[ss.selectedIndex].innerText = layout_name;

                    } else if (http.status === 401) {
                        console.log("code 401");


                    } else {
                        console.log("something else...");
                    }
                }
            };
            var ss = document.getElementById("favorite-layout-selection");
            http.open("PUT", "/runner/profiles/" + usernameFromToken + "/rename_favorite/"
                + ss[ss.selectedIndex].getAttribute('val') + "/" + layout_name, true);
            http.setRequestHeader('Cache-Control', 'no-store');
            http.send();
        }
    });


    $(".eink-sent-dialog button.export-eink").on('click', function (event) {
        var email = $(".eink-sent-dialog input").val();
        console.log(email);
        if (email !== "") {
            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200 || http.status === 204) {
                        console.log(http.status);
                        // TODO show email sent!
                        console.log("sent");


                    } else if (http.status === 401) {
                        console.log("code 401");


                    } else {
                        console.log("something else...");
                    }
                }
            };

            http.open("GET", window.location.href + "/infographic/email?email=" + email, true);
            http.setRequestHeader('Cache-Control', 'no-store');
            http.send();
            // hide popup
            document.querySelector(".eink-sent-dialog").classList.add("hidden");

            // hide overlay
            document.querySelector(".popup-overlay").classList.add("hidden");
        }
    });


    $("#controls #remove-all").on("click", function (event) {
        // show overlay
        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".remove-all-dialog").classList.remove("hidden");
    });

    // remove all button
    $('.remove-all-dialog .remove-all').on('click', function (event) {
        var items = document.querySelectorAll(".dashboard-card");

        console.log(items[0]);

        //
        for (var i = 0; i < items.length - 1; i++) {
            removeItem(items[i], false);
        }

        removeItem(items[items.length - 1], true);

        //
        console.log("removed layout.");

        // hide popup
        document.querySelector(".remove-all-dialog").classList.add("hidden");

        // hide overlay
        document.querySelector(".popup-overlay").classList.add("hidden");
    });

    $(".cancel").on("click", function (event) {
        // hide
        document.querySelector(".popup-overlay").classList.add("hidden");

        // remove all dialogs
        document.querySelector(".remove-all-dialog").classList.add("hidden");
        document.querySelector(".add-dialog").classList.add("hidden");
        document.querySelector(".eink-sent-dialog").classList.add("hidden");
        document.querySelector(".restore-layout-dialog").classList.add("hidden");
        document.querySelector(".select-layout-dialog").classList.add("hidden");
        document.querySelector(".favorite-layout-dialog").classList.add("hidden");

    });

    $("#controls #reset-to-default").on("click", function (event) {
        // show overlay
        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".restore-layout-dialog").classList.remove("hidden");
    });


    $('.restore-layout-dialog .restore').on('click', function (event) {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {

                    location.reload();
                    console.log("restored to default.")

                } else if (http.status === 401) {
                    console.log("code 401");


                } else {
                    console.log("something else...");
                }
            }
        };

        http.open("POST", window.location.href + "/layout/reset", true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    });

    $('.select-layout-dialog .select').on('click', function (event) {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {

                    location.reload();
                    console.log("restored to default.")

                } else if (http.status === 401) {
                    console.log("code 401");


                } else {
                    console.log("something else...");
                }
            }
        };

        var ss = document.getElementById("select-layout-selection");

        http.open("PUT", window.location.href + "/current_layout/" +
            ss[ss.selectedIndex].getAttribute('val'), true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    });

    $('.favorite-layout-dialog .select').on('click', function (event) {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {

                    location.reload();
                    console.log("restored to default.")

                } else if (http.status === 401) {
                    console.log("code 401");


                } else {
                    console.log("something else...");
                }
            }
        };

        var ss = document.getElementById("favorite-layout-selection");

        http.open("PUT", window.location.href + "/load_favorite/" +
            ss[ss.selectedIndex].getAttribute('val'), true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    });

    $("#controls #open-menu").on("click", function (event) {
        document.querySelector("#menu-options").classList.toggle("hidden");
    });


    window.addEventListener('resize', function (event) {
        // if we have a small device - disable muuri's drag and drop
        if (document.querySelector("html").offsetWidth <= 1023) {
            grid.dragEnabled = false;
        } else {
            // enable
            grid.dragEnabled = true;
        }
    });

    // if (document.querySelector(".premium-required") == null) {
    document.querySelector("#eink-new-window a").setAttribute('href', currentUrl + "/infographic/browser");
    // }
});