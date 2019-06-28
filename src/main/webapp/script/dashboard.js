'use strict';

document.addEventListener('DOMContentLoaded', function () {
    var currentUrl = window.location.href;

    var grid = null;
    var docElem = document.documentElement;
    var gridElement = document.querySelector('.dashboard-tile-grid');

    var dragEnabled;

    var filterField = document.querySelector('.control-filter-field');
    var searchField = document.querySelector('.control-search-field');
    var sortField = document.querySelector('.control-sort-field');

    var filterFieldValue;
    var sortFieldValue;
    var searchFieldValue;

    var dragOrder = [];
    var uuid = 0;

    var tokenJson = getTokenData(getCookieValue("token"));
    var usernameFromToken;

    if (tokenJson !== null) {
        usernameFromToken = tokenJson.sub;
    }

    dragEnabled = document.querySelector("html").offsetWidth > 1023;

    function getTokenData(token) {
        if (token === null) {
            return null;
        }

        var encoded = token.split(".")[1];
        var decoded = atob(encoded);
        return JSON.parse(decoded);
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
            layoutOnInit: false,
            layoutDuration: 400,
            layoutEasing: 'ease',
            dragEnabled: dragEnabled,
            dragSortInterval: 50,
            dragStartPredicate: function (item, event) {
                var isTextArea = event.target.tagName.toLowerCase() === "textarea";

                var isDraggable = sortFieldValue === 'order';
                var isRemoveAction = elementMatches(event.target, '.dashboard-card-remove, .dashboard-card-remove i');
                return (isDraggable && !isRemoveAction && !isTextArea) ? Muuri.ItemDrag.defaultStartPredicate(item, event) : false;
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
                updateIds();
            })
            .on('sort', updateIds);

        getLayoutFromServer();
    }

    // save layout
    function serializeLayout(grid) {
        var counter = 0;

        var itemIds = grid.getItems().map(function (item) {
            counter++;

            return {
                "typeName": item.getElement().getAttribute('data-type'),
                "indicatorName": item.getElement().getAttribute('data-title')
            };
        });

        itemIds = {
            "count": counter,
            "layout": itemIds
        };

        return itemIds;
    }


    // get layout from server
    function getLayoutFromServer() {
        var http = new XMLHttpRequest();
        var spinnerContainer = $("#dashboard-tiles-container");
        spinnerContainer.css("height", window.innerHeight -
            $("#dashboard-overview-container").outerHeight(true) -
            $("footer").outerHeight(true));

        var spinner;

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    // remove spinner
                    spinnerContainer.css("height", "");
                    document.querySelector("#dashboard-tiles-container > div:nth-of-type(2)").remove();

                    var parsedResponse = JSON.parse(http.response);

                    if (parsedResponse.cards.length < 1) {
                        grid.layout(true);
                    } else {
                        loadLayout(grid, parsedResponse);
                    }
                } else if (http.status === 401) {
                    console.log("code 401");
                    location.reload();
                } else {
                    console.log("Response: " + http.status);
                    location.reload();
                }
            }
        };

        // show loading spinner
        spinnerContainer.append('<div class="loader-container">' +
            '<div class="loader"></div>' +
            '</div>');

        http.open("GET", window.location.href + "/layout", true);
        http.setRequestHeader("Accept", "application/json");
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }


    function saveLayout(grid) {
        var layout = serializeLayout(grid);
        var layoutJson = JSON.stringify(layout);
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {

                } else if (http.status === 401) {
                    console.log("code 401");
                    location.reload();
                } else {
                    console.log("Response: " + http.status);
                    location.reload();
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

        for (var i = 0; i < layout.cards.length; i++) {
            item = layout.cards[i];
            addItem(item.cardTypeName, item.name, item, true);
        }
    }

    // based on example from muuri
    function filter() {
        filterFieldValue = filterField.value;

        grid.filter(function (item) {
            var element = item.getElement();
            var isSearchMatch = !searchFieldValue ? true : (element.getAttribute('data-title') || '').toLowerCase().indexOf(searchFieldValue) >= 0;
            var isFilterMatch = !filterFieldValue ? true : (element.getAttribute('data-type') || '') === filterFieldValue;
            return isSearchMatch && isFilterMatch;
        });
    }

    // based on example from muuri
    function sort() {
        var currentSort = sortField.value;
        if (sortFieldValue === currentSort) {
            return;
        }

        if (sortFieldValue === 'order') {
            dragOrder = grid.getItems();
        }

        grid.sort(
            currentSort === 'title' ? compareTitles :
                dragOrder
        );

        updateIds();
        sortFieldValue = currentSort;
    }


    function requestDataForIndicator(typeName, indicatorName, initialLayout) {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    console.log("code 200");

                    var parsedResponse = JSON.parse(http.response);

                    addItem(typeName, indicatorName, parsedResponse, initialLayout);
                } else if (http.status === 401) {
                    console.log("code 400");
                    location.reload();


                } else {
                    location.reload();
                    console.log("Response: " + http.status);
                }
            }
        };

        if (typeName === "graph") {
            http.open("GET", window.location.href + "/graph/50/" + indicatorName, true);
        } else if (typeName === "distribution") {
            http.open("GET", window.location.href + "/distribution/" + indicatorName, true);
        } else if (typeName === "note") {
            http.open("GET", window.location.href + "/note", true);
        } else {
            http.open("GET", window.location.href + "/individual/" + indicatorName, true);
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
        updateIds();

        // Sort the items only if the drag sorting is not active.
        if (sortFieldValue !== 'order') {
            grid.sort('title');
            dragOrder = dragOrder.concat(newItem);
        }

        if (!initialLayout) {
            saveLayout(grid);
        }

        filter();

        if (typeName === "graph" && indicatorName !== "speed") {
            initGraph(newElement, data);
        } else if (typeName === "distribution") {
            initDistribution(newElement, data);
        } else if (indicatorName === "speed") {
            initSpeedGraph(newElement, data);
        } else if (typeName === "note") {
            initNote();
        }
    }

    // based on example from muuri
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
                    if (itemIndex >= 0) {
                        dragOrder.splice(itemIndex, 1);
                    }
                }

                if (saveLayoutFlag) {
                    saveLayout(grid);
                }

                updateIds();
            }
        });
    }

    function compareTitles(a, b) {
        const aVal = a.getElement().getAttribute('data-title') || '';
        const bVal = b.getElement().getAttribute('data-title') || '';
        return aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
    }

    function updateIds() {
        var maxId = 1;
        grid.getItems().forEach(function (item, i) {
            item.getElement().setAttribute('data-id', i + 1);
            maxId = i + 1;
        });
    }

    function elementMatches(element, selector) {
        const p = Element.prototype;
        return (p.matches || p.matchesSelector || p.webkitMatchesSelector || p.mozMatchesSelector || p.msMatchesSelector || p.oMatchesSelector).call(element, selector);
    }

    function elementClosest(element, selector) {
        if (window.Element && !Element.prototype.closest) {
            var isMatch = elementMatches(element, selector);
            while (!isMatch && element && element !== document) {
                element = element.parentNode;
                isMatch = element && element !== document && elementMatches(element, selector);
            }
            return element && element !== document ? element : null;
        } else {
            return element.closest(selector);
        }
    }

    function initControls() {
        searchField.value = "";
        [sortField, filterField].forEach(function (field) {
            field.value = field.querySelectorAll('option')[0].value;
        });

        searchFieldValue = searchField.value.toLowerCase();
        filterFieldValue = filterField.value;
        sortFieldValue = sortField.value;

        searchField.addEventListener('keyup', function () {
            var newSearch = searchField.value.toLowerCase();
            if (searchFieldValue !== newSearch) {
                searchFieldValue = newSearch;
                filter();
            }
        });

        filterField.addEventListener('change', filter);
        sortField.addEventListener('change', sort);
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
                    '<h3 class="dashboard-card-id">' + nameSplit[0] + " " + nameSplit[1].charAt(0) +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="fas fa-times"></i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-single">' +
                    '<div>' +
                    '<span>' + data.average + '</span>' +
                    '</div>' +
                    '<div>' +
                    '<span>min ' + data.minimum + '</span>' +
                    '</div>' +
                    '<div>' +
                    '<span>max ' + data.maximum + '</span>' +
                    '</div>' +
                    '</div>' +
                    '</div>';
                break;
            case "note":
                height = 2;
                width = 2;

                innerContent = '<div class="dashboard-card-front">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + nameSplit[0] + " " + nameSplit[1] +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="fas fa-times"></i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-note">' +
                    '<textarea name="note-text" required>' + data.text +
                    '</textarea>' +
                    '<i class="fas fa-circle"></i>' +
                    '<label>Text</label>' +
                    '</div>' +
                    '</div>';
                break;
            case "graph":
                height = 2;
                width = 2;
                innerContent = '<div class="dashboard-card-front dashboard-card-front-graph">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + nameSplit[0] + " " + nameSplit[1] + " Graph" +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="fas fa-times"></i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-graph">' +
                    '<canvas></canvas>' +
                    '</div>' +
                    '</div>';
                break;
            case "distribution":
                height = 2;
                width = 2;
                innerContent = '<div class="dashboard-card-front dashboard-card-front-graph">' +
                    '<header>' +
                    '<h3 class="dashboard-card-id">' + nameSplit[0] + " " + nameSplit[1] + " Distribution" +
                    '</h3>' +
                    '<button class="dashboard-card-remove"><i class="fas fa-times"></i></button>' +
                    '</header>' +
                    '<div class="dashboard-card-content dashboard-card-graph">' +
                    '<canvas></canvas>' +
                    '</div>' +
                    '</div>';
                break;
        }

        const classNames = 'dashboard-card h' + height + ' w' + width;

        itemElem.innerHTML = '<div class="' + classNames + '" data-id="' + id + '" data-title="' + data.name + '" data-type="' + type + '">' +
            '<div class="dashboard-card-inner">' + innerContent +
            '</div>' +
            '</div>';
        return itemElem.firstChild;
    }


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

        var chart = Chart.Line(ctx, {
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

    function initSpeedGraph(element, data) {
        var ctx = element.querySelector("canvas").getContext('2d');

        var lineChartData = {
            labels: data.right,
            datasets: [{
                label: "km/h",
                borderColor: 'rgb(255, 99, 132)',
                backgroundColor: 'rgb(255, 99, 132)',
                fill: false,
                data: data.left,
                yAxisID: 'y-axis-1'
            }]
        };

        var chart = Chart.Line(ctx, {
            data: lineChartData,
            options: {
                legend: {
                    display: true
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

    $("#controls #add").on("click", function (event) {
        // show overlay
        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".add-dialog").classList.remove("hidden");

        // if note is on the page, remove note option from selectType field
        if (document.querySelector(".dashboard-card-note") != null) {
            document.querySelector('#select-type option[value="note"]').setAttribute("disabled", "disabled");
        } else {
            document.querySelector('#select-type option[value="note"]').removeAttribute("disabled");
        }
    });

    $("#select-type").on("change", function (event) {
        var typeField = document.querySelector("#select-type");
        var indicatorField = document.querySelector("#select-indicator");
        var selectSide;

        if (typeField.value === "individual") {
            indicatorField.querySelector("option[value='speed']").remove();

            $("#select-side input").attr("disabled", false);
            $("#select-side div:nth-of-type(1) input").attr("checked", true);
            document.querySelector("#select-side").classList.remove("disabled");

            $("#select-indicator").attr("disabled", false);
            document.querySelector(".add-dialog .input-select").classList.remove("disabled");
        } else if (typeField.value === "graph" || typeField.value === "distribution") {
            $("#select-indicator").attr("disabled", false);
            document.querySelector(".add-dialog .input-select").classList.remove("disabled");

            if (indicatorField.querySelector("option[value='speed']") == null) {
                var newOption = document.createElement("option");
                newOption.setAttribute("value", "speed");
                newOption.textContent = "Speed";
                indicatorField.appendChild(newOption);
            }

            if (indicatorField.value === "speed") {
                selectSide = $("#select-side input");
                selectSide.attr("disabled", true);
                selectSide.attr("checked", false);
                document.querySelector("#select-side").classList.add("disabled");
            } else {
                $("#select-side input").attr("disabled", false);
                $("#select-side div:nth-of-type(1) input").attr("checked", true);
                document.querySelector("#select-side").classList.remove("disabled");
            }
        } else if (typeField.value === "note") {
            selectSide = $("#select-side input");

            selectSide.attr("disabled", true);
            selectSide.attr("checked", false);
            document.querySelector("#select-side").classList.add("disabled");

            $("#select-indicator").attr("disabled", true);
            document.querySelector(".add-dialog .input-select:nth-of-type(2)").classList.add("disabled");
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
                var selectSide = $("#select-side input");

                selectSide.attr("disabled", true);
                selectSide.attr("checked", false);
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
        if (layout_name !== "") {
            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200 || http.status === 204) {
                        console.log(http.status);
                        console.log("sent");
                        // location.reload();
                        ss[ss.selectedIndex].innerText = layout_name;

                    } else if (http.status === 401) {
                        location.reload();
                    } else {
                        console.log("Response: " + http.status);
                        location.reload();
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
                    document.querySelector(".popup-overlay").classList.add("hidden");

                    // hide popup
                    document.querySelector(".favorite-layout-dialog").classList.add("hidden");

                } else if (http.status === 401) {
                    location.reload();

                } else {
                    console.log("Response: " + http.status);
                    location.reload();

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
                        ss[ss.selectedIndex].innerText = layout_name;

                    } else if (http.status === 401) {
                        location.reload();

                    } else {
                        console.log("Response: " + http.status);
                        location.reload();

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


                    } else if (http.status === 401) {
                        location.reload();

                    } else {
                        console.log("Response: " + http.status);
                        location.reload();
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

        for (var i = 0; i < items.length - 1; i++) {
            removeItem(items[i], false);
        }

        removeItem(items[items.length - 1], true);

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
                } else if (http.status === 401) {
                    location.reload();

                } else {
                    console.log("Response: " + http.status);
                    location.reload();
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
                } else if (http.status === 401) {
                    location.reload();

                } else {
                    console.log("Response: " + http.status);
                    location.reload();
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
                } else if (http.status === 401) {
                    location.reload();
                } else {
                    console.log("Response: " + http.status);
                    location.reload();
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
        // if the window is resized below the threshold, disable muuri's drag and drop
        if (document.querySelector("html").offsetWidth <= 1023) {
            console.log("Window width is small. Drag is disabled now.");
            grid.dragEnabled = false;
        } else {
            // enable drag and drop
            console.log("Window width is wide enough. Drag is enabled now.");
            grid.dragEnabled = true;
        }
    });

    document.querySelector("#eink-new-window a").setAttribute('href', currentUrl + "/infographic/browser");

    function initNote() {
        var statusIcon = document.querySelector(".dashboard-card-note i");
        var noteTextArea = document.querySelector(".dashboard-card-note textarea");
        var timeout;

        noteTextArea.addEventListener("keyup", function () {
            statusIcon.classList.add("unsaved");

            if (timeout) {
                clearTimeout(timeout);
            }

            timeout = setTimeout(function () {
                saveNoteText(noteTextArea.value);

                statusIcon.classList.remove("unsaved");
            }, 1000);
        });
    }


    function saveNoteText(text) {
        var http = new XMLHttpRequest();

        var param = {
            "cardTypeName": "note",
            "text": text,
            "name": "note",
        };

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {

                } else if (http.status === 401) {
                    location.reload();

                } else {
                    console.log("Response: " + http.status);
                    location.reload();
                }
            }
        };

        http.open("PUT", window.location.href + "/note", true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.setRequestHeader('Content-type', 'application/json');
        http.send(JSON.stringify(param));
    }
});