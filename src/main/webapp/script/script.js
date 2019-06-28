document.addEventListener('DOMContentLoaded', function () {
    // authentication token
    var tokenJson = getTokenData(getCookieValue("token"));
    var usernameFromToken;

    var profileManagementPage = $("#profile-management");

    if (tokenJson !== null) {
        usernameFromToken = tokenJson.sub;
    }

    function getTokenData(token) {
        if (token === null) {
            return null;
        }

        var encoded = token.split(".")[1];
        var decoded = atob(encoded);
        return JSON.parse(decoded);
    }

    // extracts parameter value from response url
    function getQueryParam(paramName) {
        var urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(paramName);
    }


    // gets a cookie by name (based on https://www.w3schools.com/js/js_cookies.asp)
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

    // rounds number with specified precision (from https://stackoverflow.com/questions/15762768/javascript-math-round-to-two-decimal-places/22977058)
    function roundTo(n, digits) {
        var negative = false;
        if (digits === undefined) {
            digits = 0;
        }
        if (n < 0) {
            negative = true;
            n = n * -1;
        }
        var multiplicator = Math.pow(10, digits);
        n = parseFloat((n * multiplicator).toFixed(11));
        n = (Math.round(n) / multiplicator).toFixed(2);
        if (negative) {
            n = (n * -1).toFixed(2);
        }
        return n;
    }

    // converts time from seconds to "12:44:02"
    function convertTime(seconds) {
        var hours = Math.round(seconds / 3600);
        var minutes = Math.round((seconds % 3600) / 60);
        seconds = Math.round((seconds % 3600) % 60);

        return hours + ":" + minutes + ":" + seconds;
    }

    // saves updated information (currently, full name) entered by user in profile settings
    if (profileManagementPage.length > 0) {

        var constraints = {
            "first-name": {
                presence: true,
                format: {
                    pattern: "[a-zA-Z\\-\\s]+",
                    message: "can only contain letters, spaces and dashes"
                }
            },
            "last-name": {
                presence: true,
                format: {
                    pattern: "[a-zA-Z\\-\\s]+",
                    message: "can only contain letters, spaces and dashes"
                }
            }
        };

        function handleSettingsSaveForm(form) {
            var errors = validate(form, constraints);

            showErrors(form, errors || {});
            if (!errors) {
                submitSettingsSaveForm();
            }
        }

        var settingsSaveForm = document.querySelector("form");
        settingsSaveForm.addEventListener("submit", function (ev) {
            ev.preventDefault();
            console.log("reached.");
            handleSettingsSaveForm(settingsSaveForm);
        });

        // validate inputs on fly
        var inputs = document.querySelectorAll("input");

        for (var i = 0; i < inputs.length; i += 1) {
            inputs.item(i).addEventListener("keyup", function (event) {
                var errors = validate(settingsSaveForm, constraints) || {};

                // if not available, add to errors
                showErrorsForInput(this, errors[this.name])
            });
        }

        // $("#save-my-information").on("click", function () {
        function submitSettingsSaveForm() {
            var firstName = $("#first-name").val().trim();
            var lastName = $("#last-name").val().trim();

            // ignores if fields are empty
            if (!(firstName === "" && lastName === "")) {
                var params = "firstname=" + firstName + "&lastname=" + lastName;
                var http = new XMLHttpRequest();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 204 || http.status === 200) {
                            window.location.replace("/runner/settings");
                        } else {
                            console.log("Response code: " + http.status);
                        }
                    }
                };

                http.open("PUT", "settings", true);
                http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send(params);
            }
        }
    }

    // loading data to show on settings page
    if (profileManagementPage.length > 0) {
        document.querySelector("#link-strava").setAttribute("href", "https://www.strava.com/oauth/authorize?client_id=35158&redirect_uri="
            + window.location.protocol + "//" + window.location.host + "/runner/settings/link_strava&response_type=code&scope=read,read_all,profile:read_all,profile:write,activity:read_all,activity:write");

        getImage(usernameFromToken);

        var http = new XMLHttpRequest();
        var url = "/runner/profiles/" + usernameFromToken;

        http.open('GET', url, true);
        http.setRequestHeader('Accept', 'application/json');
        http.setRequestHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
        http.setRequestHeader('Pragma', 'no-cache');
        http.setRequestHeader('Expires', '0');

        http.onreadystatechange = function () {
            if (http.readyState === 4 && http.status === 200) {
                var parsedResponse = JSON.parse(http.response);

                $("#profile-name-container span")[0].innerText = parsedResponse.firstName + " " + parsedResponse.lastName;

                if (parsedResponse.isPremium === 1) {
                    // show pro tag
                    $("#profile-name-container").append('<span id="pro">PRO</span>');
                } else {
                    document.querySelector(".nav-item.upgrade").classList.remove("hidden");
                }

                // populate first and last name fields
                $("#first-name").val(parsedResponse.firstName);
                $("#last-name").val(parsedResponse.lastName);

                // show the hidden page
                $(".loader-container-absolute").remove();
            } else {
                console.log("Response: " + http.status);
            }
        };

        http.send();
    }


    if ($("#login").length > 0) {
        var loginForm = document.querySelector("#login form");

        loginForm.addEventListener("submit", function (event) {
            event.preventDefault();
            submitLoginForm();
        });

        displayLoginMessage();
    }


    // send ajax request to login
    function submitLoginForm() {
        var username = $("#login input[type='text']").val().trim();
        var password = $("#login input[type='password']").val().trim();
        if (username !== "" && password !== "") {
            var params = "username=" + username + "&password=" + password;
            var http = new XMLHttpRequest();

            var spinnerContainer = $("#login form");

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    var responseMessage = $("#response-message");

                    if (http.status === 200) {
                        usernameFromToken = getTokenData(getCookieValue("token")).sub;
                        window.location.replace("/runner/profiles/" + usernameFromToken);
                    } else if (http.status === 401) {
                        responseMessage.text("Incorrect username or password. Please, try again.");
                        responseMessage.removeClass("response-message-hidden");
                        responseMessage.addClass("response-message-hidden-red");
                        $(".loader-container-absolute").remove();
                    } else if (http.status === 402) {
                        responseMessage.text("Account is not activated");
                        responseMessage.removeClass("response-message-hidden");
                        responseMessage.addClass("response-message-hidden-red");

                        $(".loader-container-absolute").remove();
                    } else {
                        console.log("Response: " + http.status);
                    }
                }
            };

            // show loading spinner
            spinnerContainer.append('<div class="loader-container-absolute loader-container-radius">' +
                '<div class="loader"></div>' +
                '</div>');

            http.open("POST", "/runner/login", true);
            http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            http.setRequestHeader('Accept', 'text/html');
            http.setRequestHeader('Cache-Control', 'no-store');
            http.send(params);
        }
    }

    function displayLoginMessage() {
        var error = getQueryParam("error");
        var message = getQueryParam("message");
        var responseMessage = $("#response-message");

        if (error != null) {
            if (error === "not_authorized") {
                error = "You need to sign in to access this page."
            } else if (error === "token_expired") {
                error = "Your session expired. Sign in to access this page."
            } else if (error === "incorrect_credentials") {
                error = "Incorrect username or password. Please, try again."
            } else if (error === "not_activated") {
                error = "Account has not been activated yet."
            } else if (error === "reset_token_invalid") {
                error = "Password reset link is invalid."
            }

            // insert error into the page
            responseMessage.text(error);
            responseMessage.removeClass("response-message-hidden");
            responseMessage.addClass("response-message-hidden-red");
        }

        if (message != null) {
            if (message === "reset_success") {
                message = "Password was successfully reset."
            } else if (message === "activate_success") {
                message = "Account was successfully activated."
            } else if (message === "registration_success") {
                message = "Your account was successfully registered. Confirm your email address with the link we just sent to you."
            } else if (message === "reset_request_success") {
                message = "If a matching account was found, an email was sent to allow you to reset your password.."
            }

            // insert message into the page
            responseMessage.text(message);
            responseMessage.removeClass("response-message-hidden");
            responseMessage.addClass("response-message-hidden-green");
        }
    }


    if ($("#password-reset-enter").length > 0) {
        var constraints = {
            password: {
                presence: true,
                length: {
                    minimum: 8
                },
                format: {
                    pattern: "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,}",
                    message: "must contain at least one uppercase letter, lowercase letter and number."
                }
            },
            "confirm-password": {
                presence: true,
                equality: {
                    attribute: "password",
                    message: "^Passwords do not match"
                }
            }
        };

        function handleResetEnterForm(form) {
            // validate the form against the constraints
            var errors = validate(form, constraints);
            // then we update the form to reflect the results

            showErrors(form, errors || {});
            if (!errors) {
                submitResetEnterForm();
            }
        }

        var resetEnterForm = document.querySelector("form");
        resetEnterForm.addEventListener("submit", function (ev) {
            ev.preventDefault();
            handleResetEnterForm(resetEnterForm);
        });

        // validate inputs on fly
        var inputs = document.querySelectorAll("input");

        for (var i = 0; i < inputs.length; i += 1) {
            inputs.item(i).addEventListener("keyup", function (event) {
                var errors = validate(resetEnterForm, constraints) || {};

                // if not available, add to errors
                showErrorsForInput(this, errors[this.name])
            });
        }

        function submitResetEnterForm() {
            var recoveryToken = getQueryParam("token");
            var recoveryPassword = $("#password-reset-enter form input[name=password]").val().trim();
            var recoveryPasswordConfirm = $("#password-reset-enter form input[name=confirm-password]").val().trim();

            if (recoveryToken !== "" && recoveryPassword !== "" && recoveryPassword === recoveryPasswordConfirm) {
                var params = "token=" + recoveryToken + "&password=" + recoveryPassword;
                var http = new XMLHttpRequest();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 200) {
                            window.location.replace("/runner/login?message=reset_success");
                        } else if (http.status === 401) {
                            window.location.replace("/runner/login?error=not_authorized");
                        } else if (http.status === 402) {
                            window.location.replace("/runner/login?error=not_activated");
                        } else if (http.status === 404) {
                            window.location.replace("/runner/login?error=reset_token_invalid");
                        } else {
                            console.log("Response: " + http.status);
                        }
                    }
                };

                http.open("POST", "/runner/password/reset/enter", true);
                http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                http.setRequestHeader('Accept', 'text/html');
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send(params);
            }
        }
    }

    if ($("#password-reset-request").length > 0) {
        var resetRequestForm = document.querySelector("form");

        resetRequestForm.addEventListener("submit", function (event) {
            event.preventDefault();
            handleResetRequestFormSubmit();
        });

        function handleResetRequestFormSubmit() {
            var username = $("#password-reset-request form input").val().trim();
            if (username !== "") {
                var param = "username=" + username;
                var http = new XMLHttpRequest();

                var spinnerContainer = $("#password-reset-request form");
                var formHeight = spinnerContainer.height();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 200) {
                            window.location.replace("/runner/login?message=reset_request_success");
                        } else if (http.status === 401) {

                        } else {
                            console.log("Response: " + http.status);
                        }
                    }
                };

                // show loading spinner
                spinnerContainer.css("height", formHeight);
                spinnerContainer.css("display", "block");
                spinnerContainer.html('<div class="loader-container-absolute loader-container-radius">' +
                    '<div class="loader"></div>' +
                    '</div>');

                http.open("POST", "/runner/password/reset", true);
                http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                http.setRequestHeader('Accept', 'text/html');
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send(param);
            }
        }
    }

    var indexNavUl = $("#index nav ul");

    if (tokenJson !== null && tokenJson.exp > Math.floor(Date.now() / 1000)) {
        indexNavUl.append("<li class='nav-item'><a href='/runner/profiles/" + usernameFromToken + "'>profile</a></li>");
        indexNavUl.append("<li class='nav-item'><a href='/runner/logout'>sign out</a></li>");
    } else {
        indexNavUl.append("<li class='nav-item'><a href='/runner/register'>sign up</a></li>");
        indexNavUl.append("<li class='nav-item'><a href='/runner/login'>sign in</a></li>");
    }

    // loading profile info
    if ($("#profile").length > 0) {
        getImage(usernameFromToken);

        var http = new XMLHttpRequest();
        var url = "/runner/profiles/" + usernameFromToken;

        http.open('GET', url, true);
        http.setRequestHeader('Accept', 'application/json');
        http.setRequestHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
        http.setRequestHeader('Pragma', 'no-cache');
        http.setRequestHeader('Expires', '0');

        http.onreadystatechange = function () {
            if (http.readyState === 4 && http.status === 200) {
                var parsedResponse = JSON.parse(http.response);

                $("#profile-name-container span")[0].innerText = parsedResponse.firstName + " " + parsedResponse.lastName;

                if (parsedResponse.isPremium === 1) {
                    // show pro tag
                    $("#profile-name-container").append('<span id="pro">PRO</span>');
                } else {
                    document.querySelector(".nav-item.upgrade").classList.remove("hidden");
                }

                // convert to km
                $("#overview-distance p span")[0].innerText = Math.round(parsedResponse.totalDistance / 1000);

                // convert to hours
                $("#overview-time p span")[0].innerText = Math.round(parsedResponse.totalTime / 3600);
                $("#overview-steps p span")[0].innerText = Math.round(parsedResponse.totalSteps / 1000) + "k";


                document.querySelector("#profile-picture-general div:nth-child(1) p span:nth-child(2)").innerText = parsedResponse.totalRuns;
                document.querySelector("#profile-picture-general div:nth-child(2) p span:nth-child(2)").innerText = parsedResponse.totalDistance + "m";
                document.querySelector("#profile-picture-general div:nth-child(3) p span:nth-child(2)").innerText = parsedResponse.totalSteps;

                var $container = $("#runs-data-container");

                if (parsedResponse.runsList.length > 0) {
                    var lastRunDate = new Date(parsedResponse.runsList[0].date);

                    document.querySelector("#profile-picture-general div:nth-child(4) p:nth-child(2) span").innerText =
                        parsedResponse.runsList[0].name + " - " + lastRunDate.toDateString();

                    for (var i = 0; i < parsedResponse.runsList.length; i++) {
                        var runDate = new Date(parsedResponse.runsList[i].date);

                        $container.append('<div class="profile-card profile-run-card">' +
                            '<div class="run-card-blocks"><div><div class="run-card-title"><h4>' + parsedResponse.runsList[i].name + '</h4></div>' +
                            '<div class="run-card-date">' + runDate.toDateString() + '</div></div><div class="run-card-distance"><div>Distance</div>' +
                            '<div>' + roundTo(parsedResponse.runsList[i].distance / 1000, 2) + ' km</div></div><div class="run-card-time">' +
                            '<div>Time</div><div>' + convertTime(parsedResponse.runsList[i].duration) + '</div></div><div class="run-card-steps">' +
                            '<div>Steps</div><div>' + parsedResponse.runsList[i].steps + '</div></div><div class="run-card-see-more">' +
                            '<a class="button-raised" href="/runner/runs/' + parsedResponse.runsList[i].id + '"><span><i class="fas fa-angle-right"></i>' +
                            '</span></a></div></div></div>');
                    }

                } else {
                    $container.append('<div class="profile-card profile-run-card">Nothing here yet...</div>');
                }

                $(".loader-container-absolute").remove();
            }
        };

        http.send();
    }


    $("#select-sort-type").on("change", function (event) {
        var type = $("#select-sort-type").val();

        var toSort = document.getElementById("runs-data-container").children;
        toSort = Array.prototype.slice.call(toSort, 0);

        if (type === "dateu") {
            toSort.sort(function (a, b) {
                var x = a.querySelector(".run-card-date").innerText;
                var y = b.querySelector(".run-card-date").innerText;

                var Datex = new Date(x);
                var Datey = new Date(y);
                x = Number(Datex.getTime());
                y = Number(Datey.getTime());

                if (x > y) {
                    return 1;
                } else if (x < y) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if (type === "timeu") {
            toSort.sort(function (a, b) {
                var x = a.querySelector(".run-card-time div:nth-of-type(2)").innerText;
                var y = b.querySelector(".run-card-time div:nth-of-type(2)").innerText;

                var XX = x.split(":");
                var YY = y.split(":");

                var secondx = Number(XX[0]) * 3600 + Number(XX[1]) * 60 + Number(XX[2]);
                var secondy = Number(YY[0]) * 3600 + Number(YY[1]) * 60 + Number(YY[2]);

                if (secondx > secondy) {
                    return 1;
                } else if (secondx < secondy) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if (type === "stepu") {
            toSort.sort(function (a, b) {
                var x = a.querySelector(".run-card-steps div:nth-of-type(2)").innerText;
                var y = b.querySelector(".run-card-steps div:nth-of-type(2)").innerText;

                x = Number(x);
                y = Number(y);

                if (x > y) {
                    return 1;
                } else if (x < y) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if (type === "distanceu") {
            toSort.sort(function (a, b) {
                var x = a.querySelector(".run-card-distance div:nth-of-type(2)").innerText;
                var y = b.querySelector(".run-card-distance div:nth-of-type(2)").innerText;

                x = parseInt(x);
                y = parseInt(y);

                if (x > y) {
                    return 1;
                } else if (x < y) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if (type === "dated") {
            toSort.sort(function (a, b) {
                var x = a.querySelector(".run-card-date").innerText;
                var y = b.querySelector(".run-card-date").innerText;

                var Datex = new Date(x);
                var Datey = new Date(y);
                x = Number(Datex.getTime());
                y = Number(Datey.getTime());

                if (x < y) {
                    return 1;
                } else if (x > y) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if (type === "timed") {
            toSort.sort(function (a, b) {
                var x = a.querySelector(".run-card-time div:nth-of-type(2)").innerText;
                var y = b.querySelector(".run-card-time div:nth-of-type(2)").innerText;

                var XX = x.split(":");
                var YY = y.split(":");

                var secondx = Number(XX[0]) * 3600 + Number(XX[1]) * 60 + Number(XX[2]);
                var secondy = Number(YY[0]) * 3600 + Number(YY[1]) * 60 + Number(YY[2]);

                if (secondx < secondy) {
                    return 1;
                } else if (secondx > secondy) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if (type === "stepd") {
            toSort.sort(function (a, b) {
                var x = a.querySelector(".run-card-steps div:nth-of-type(2)").innerText;
                var y = b.querySelector(".run-card-steps div:nth-of-type(2)").innerText;

                x = Number(x);
                y = Number(y);

                if (x < y) {
                    return 1;
                } else if (x > y) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if (type === "distanced") {
            toSort.sort(function (a, b) {
                var x = a.querySelector(".run-card-distance div:nth-of-type(2)").innerText;
                var y = b.querySelector(".run-card-distance div:nth-of-type(2)").innerText;

                x = parseInt(x);
                y = parseInt(y);

                if (x < y) {
                    return 1;
                } else if (x > y) {
                    return -1;
                } else {
                    return 0;
                }
            });
        }

        var parent = document.getElementById('runs-data-container');
        parent.innerHTML = "";

        for (var i = 0, l = toSort.length; i < l; i++) {
            parent.appendChild(toSort[i]);
        }

    });

    // upload photo in profile management
    $("#profile-image-field").on("change", function (event) {
        var input = document.getElementById("profile-image-field");
        var link = input.value;
        var extension = link.substring(link.lastIndexOf('.') + 1).toLowerCase();

        if (input.files && input.files[0] && (extension === "gif" || extension === "png" || extension === "jpeg" || extension === "jpg")) {
            var reader = new FileReader();

            reader.onload = function (e) {
                $('#profile-picture-img img').attr('src', e.target.result);
            };

            reader.readAsDataURL(input.files[0]);
        } else {
            $('#profile-picture-img img').attr('src', '/img/no_preview.png');
        }
    });

    function uploadImage(id) {
        console.log("uploading image");
        var blobFile = id.files[0];
        var image = new FormData();
        image.append("picture", blobFile);

        $.ajax({
            url: "/runner/profiles/" + usernameFromToken + "/picture",
            type: "POST",
            data: image,
            processData: false,
            contentType: false,
            success: function (response) {
                location.reload();
            },
            error: function (xhr, status, errorMessage) {
                console.log(errorMessage);
            }
        });
    }

    function getImage(username) {
        var http = new XMLHttpRequest();
        var url = "/runner/profiles/" + username + "/picture";

        http.open('GET', url, true);
        http.setRequestHeader('Accept', 'text/plain');
        http.setRequestHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
        http.setRequestHeader('Pragma', 'no-cache');
        http.setRequestHeader('Expires', '0');

        http.onreadystatechange = function () {
            if (http.readyState === 4 && http.status === 200) {
                document.getElementById("profile-picture").src = 'data:image/png;base64,' + http.responseText;
            } else {
                console.log("Response: " + http.status);
            }
        };
        http.send();
    }

    // send ajax request for user to check the username availability
    if ($("#register").length > 0) {
        var constraints = {
            email: {
                presence: true,
                email: true
            },
            password: {
                presence: true,
                length: {
                    minimum: 8
                },
                format: {
                    pattern: "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,}",
                    message: "must contain at least one uppercase letter, lowercase letter and number."
                }
            },
            "confirm-password": {
                presence: true,
                equality: {
                    attribute: "password",
                    message: "^Passwords do not match"
                }
            },
            username: {
                presence: true,
                length: {
                    minimum: 2,
                    maximum: 20
                },
                format: {
                    pattern: "[a-z0-9_]+",
                    flags: "i",
                    message: "can only contain letters, digits and underscores"
                }
            },
            "first-name": {
                presence: true,
                format: {
                    pattern: "[a-zA-Z\\-\\s]+",
                    message: "can only contain letters, spaces and dashes"
                }
            },
            "last-name": {
                presence: true,
                format: {
                    pattern: "[a-zA-Z\\-\\s]+",
                    message: "can only contain letters, spaces and dashes"
                }
            },
        };

        validate.validators.usernameAvailabilityValidator = function (value) {
            return new validate.Promise(function (resolve, reject) {

                var param = "username=" + value;
                var http = new XMLHttpRequest();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 200) {
                            if (!JSON.parse(http.response).exists) {
                                resolve();
                            } else {
                                reject("Username already exists");
                            }
                        } else {
                            console.log("Response: " + http.status);
                        }
                    }
                };

                http.open("GET", "/runner/register/username?" + param, true);
                http.setRequestHeader('Accept', 'application/json');
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send();
            });
        };

        validate.validators.emailAvailabilityValidator = function (value) {
            return new validate.Promise(function (resolve, reject) {

                var param = "email=" + value;
                var http = new XMLHttpRequest();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 200) {
                            if (!JSON.parse(http.response).exists) {
                                resolve();
                            } else {
                                reject("Email is taken.");
                            }
                        } else {
                            console.log("Response: " + http.status);
                        }
                    }
                };

                http.open("GET", "/runner/register/email?" + param, true);
                http.setRequestHeader('Accept', 'application/json');
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send();
            });
        };

        var usernameConstraints = {
            username: {
                usernameAvailabilityValidator: true
            }
        };

        var emailConstraints = {
            email: {
                emailAvailabilityValidator: true
            }
        };

        var successUsername = function () {
            showErrorsForInput(document.querySelector("#register input[name='username']"), null);
        };


        var errorUsername = function (errors) {
            showErrorsForInput(document.querySelector("#register input[name='username']"), [errors]);
        };

        var successEmail = function () {
            showErrorsForInput(document.querySelector("#register input[name='email']"), null);
        };


        var errorEmail = function (errors) {
            showErrorsForInput(document.querySelector("#register input[name='email']"), [errors]);
        };


        var form = document.querySelector("form");
        form.addEventListener("submit", function (ev) {
            ev.preventDefault();
            handleRegistrationFormSubmit(form);
        });

        // validate inputs on fly
        var inputs = document.querySelectorAll("input");
        var timeout;

        for (var i = 0; i < inputs.length; ++i) {

            inputs.item(i).addEventListener("keyup", function (event) {
                var errors = validate(form, constraints) || {};

                // if no errors for input username and username was typed, then check it for availability
                if (!errors.hasOwnProperty("username") && event.target.id === "username") {
                    if (timeout) {
                        clearTimeout(timeout);
                    }

                    var username = this.value;

                    timeout = setTimeout(function () {
                        validate.async({username: username}, usernameConstraints).then(successUsername, errorUsername);
                    }, 500);
                } else if (!errors.hasOwnProperty("email") && event.target.id === "email") {

                    if (timeout) {
                        clearTimeout(timeout);
                    }

                    var email = this.value;

                    timeout = setTimeout(function () {
                        validate.async({email: email}, emailConstraints).then(successEmail, errorEmail);
                    }, 500);
                }

                // if not available, add to errors
                showErrorsForInput(this, errors[this.name])
            });
        }

        function handleRegistrationFormSubmit(form, input) {
            // var errors = validate(form, constraints);
            // showErrors(form, errors || {});

            if (document.querySelectorAll(".input-error").length === 0) {
                submitRegistrationForm();
            }
        }
    }

    // Updates the inputs with the validation errors
    function showErrors(form, errors) {
        // We loop through all the inputs and show the errors for that input
        form.querySelectorAll("input[name]").forEach(function (input) {
            // Since the errors can be null if no errors were found we need to handle that
            showErrorsForInput(input, errors && errors[input.name]);
        });
    }

    // Shows the errors for a specific input
    function showErrorsForInput(input, errors) {
        // This is the root of the input
        var inputGroup = closestParent(input.parentNode, "input-group");

        // First we remove any old messages and resets the classes
        resetFormGroup(inputGroup);

        var errorElem;

        if (errors) {

            if (document.querySelector("html").offsetWidth > 750) {
                // we first mark the group has having errors
                inputGroup.classList.add("has-error");

                // add icon and tooltip
                errorElem = document.createElement("span");
                errorElem.classList.add("input-error");

                var errorContainer = document.createElement("div");
                errorContainer.classList.add("input-error-tooltip-text");

                // then we append all the errors
                errors.forEach(function (error) {
                    addError(errorContainer, error);
                });

                errorElem.innerHTML = '<i class="fas fa-exclamation-triangle"></i>';

                var div = document.createElement("div");
                div.appendChild(errorContainer);
                errorElem.appendChild(div);

                inputGroup.appendChild(errorElem);
            } else {
                // we first mark the group as having errors
                inputGroup.classList.add("has-error");

                // add paragraph with error
                errorElem = document.createElement("div");
                errorElem.classList.add("input-error");
                errorElem.classList.add("inline-error");

                // then we append all the errors
                errors.forEach(function (error) {
                    addError(errorElem, error);
                });

                inputGroup.appendChild(errorElem);
            }
        } else {
            inputGroup.classList.add("has-success"); // paint in green?

            errorElem = inputGroup.querySelector("input-error");
            if (errorElem != null) {
                errorElem.remove();
            }
        }
    }

    // Recursively finds the closest parent that has the specified class
    function closestParent(child, className) {
        if (!child || child === document) {
            return null;
        }
        if (child.classList.contains(className)) {
            return child;
        } else {
            return closestParent(child.parentNode, className);
        }
    }

    function resetFormGroup(formGroup) {
        formGroup.classList.remove("has-error");
        formGroup.classList.remove("has-success");

        formGroup.querySelectorAll(".input-error").forEach(function (el) {
            el.remove();
        });
    }

    // Adds the specified error with the following markup
    function addError(errorElem, error) {
        var block = document.createElement("p");
        block.classList.add("help-block");
        block.classList.add("error");
        block.innerText = error;
        errorElem.appendChild(block);
    }


    // sends ajax request to register the user
    function submitRegistrationForm() {
        var username = $("#register form input[name='username']").val().trim();
        var firstName = $("#register form input[name='first-name']").val().trim();
        var lastName = $("#register form input[name='last-name']").val().trim();
        var email = $("#register form input[name='email']").val().trim();
        var password = $("#register form input[name='password']").val().trim();

        if (username !== "" && password !== "") {

            var spinner = document.querySelector(".loader-container-absolute");
            spinner.classList.remove("hidden");

            var params = "username=" + username + "&password=" + password +
                "&first_name=" + firstName + "&last_name=" + lastName + "&email=" + email;

            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200) {
                        console.log("code 200");
                        window.location.replace("/runner/login?message=registration_success");
                    } else if (http.status === 400) {
                        spinner.classList.add("hidden");
                        var responseMessage = $("#response-message");
                        responseMessage.text("Fix the errors!");
                        responseMessage.removeClass("response-message-hidden");
                        responseMessage.addClass("response-message-hidden-red");
                    } else {
                        console.log("Response: " + http.status);
                    }
                }
            };

            http.open("POST", "register", true);
            http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            http.setRequestHeader('Accept', 'text/html');
            http.setRequestHeader('Cache-Control', 'no-store');
            http.send(params);
        }
    }


    // loading info to dashboard
    if ($('#dashboard').length > 0) {
        var http = new XMLHttpRequest();
        var url = window.location.href + "/info";
        http.open('GET', url, true);
        http.setRequestHeader('Accept', 'application/json');

        http.onreadystatechange = function () {
            if (http.readyState === 4 && http.status === 200) {
                var parsedResponse = JSON.parse(http.response);
                document.querySelector("#dashboard-overview-container-grid div:nth-child(2) p:nth-of-type(2) span").innerText = new Date(parsedResponse.date).toDateString();
                document.querySelector("#dashboard-overview-container-grid div:nth-child(5) p:nth-of-type(2) span").innerText = parsedResponse.distance + "m";
                document.querySelector("#dashboard-overview-container-grid div:nth-child(4) p:nth-of-type(2) span").innerText = convertTime(parsedResponse.duration);
                document.querySelector("#dashboard-overview-container-grid div:nth-child(6) p:nth-of-type(2) span").innerText = parsedResponse.steps;
                document.querySelector("#dashboard-overview-container-grid div:nth-child(1) p:nth-of-type(2) span").innerText = parsedResponse.name; // TODO
                document.querySelector("#dashboard-overview-container-grid div:nth-child(3) p:nth-of-type(2) span").innerText = parsedResponse.shoes;

                if (document.querySelector(".premium-required") == null) {
                    loadLayoutOptions();
                    loadFavoriteLayoutOptions();
                }
            }
        };
        http.send();
    }

    function loadLayoutOptions() {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {
                    // show overlay
                    var parsedResponse = JSON.parse(http.response);

                    var x = document.getElementById("select-layout-selection");
                    $("#select-layout-selection").empty();

                    var option;
                    var i;

                    for (i = 0; i < parsedResponse.layoutDataList.length; i++) {
                        option = document.createElement("option");
                        option.text = parsedResponse.layoutDataList[i].name;
                        option.setAttribute("val", parsedResponse.layoutDataList[i].layoutID);
                        if (parsedResponse.currentLayout === parsedResponse.layoutDataList[i].layoutID) {
                            x.add(option);
                        }
                    }

                    for (i = 0; i < parsedResponse.layoutDataList.length; i++) {
                        option = document.createElement("option");
                        option.text = parsedResponse.layoutDataList[i].name;
                        option.setAttribute("val", parsedResponse.layoutDataList[i].layoutID);
                        if (parsedResponse.currentLayout !== parsedResponse.layoutDataList[i].layoutID) {
                            x.add(option);
                        }
                    }
                } else {
                    console.log("Response: " + http.status);
                }
            }
        };

        http.open("GET", window.location.href + "/layout/name", true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }

    function loadFavoriteLayoutOptions() {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {
                    var parsedResponse = JSON.parse(http.response);
                    var x = document.getElementById("favorite-layout-selection");
                    $("#favorite-layout-selection").empty();
                    for (var i = 0; i < parsedResponse.layoutDataList.length; i++) {
                        var option = document.createElement("option");
                        option.text = parsedResponse.layoutDataList[i].name;
                        option.setAttribute("val", parsedResponse.layoutDataList[i].layoutID);
                        x.add(option);
                    }
                } else {
                    console.log("Response: " + http.status);
                }
            }
        };

        http.open("GET", "/runner/profiles/" + usernameFromToken + "/favorite/name", true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }


    $("#upgrade-button").on("click", function (event) {
        upgradeToPremium();
    });

    // send ajax request for user to register
    function upgradeToPremium() {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200) {
                    window.location.replace("/runner/profiles");
                } else {
                    console.log("Response: " + http.status);
                }
            }
        };

        http.open("POST", "/runner/premium/join", true);
        http.setRequestHeader('Accept', 'text/html');
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }

    // dialog on profile management page
    $("#profile-management #upload-new-picture").on("click", function () {
        document.querySelector(".popup-overlay").classList.remove("hidden");
        document.querySelector(".upload-new-picture-dialog").classList.remove("hidden");
    });

    // upload-new-picture-dialog
    $('.upload-new-picture-dialog .upload').on('click', function () {
        var input = document.getElementById("profile-image-field");
        uploadImage(input);

        document.querySelector(".upload-new-picture-dialog").classList.add("hidden");
        document.querySelector(".popup-overlay").classList.add("hidden");
    });


    $("#profile-management .cancel").on("click", function () {
        document.querySelector(".popup-overlay").classList.add("hidden");
        document.querySelector(".upload-new-picture-dialog").classList.add("hidden");
    });

    // responsive navigation bar for mobile devices
    document.querySelector(".nav-button").addEventListener('click', function () {
        var button = document.querySelector(".nav-button");

        document.querySelector("nav ul").classList.toggle('hidden');
        document.querySelector(".popup-overlay").classList.toggle('hidden');

        document.querySelector(".nav-logo").classList.toggle('active');

        if (button.classList.contains("active")) {
            button.querySelector("a i").classList.toggle("fa-bars");
            button.querySelector("a i").classList.toggle("fa-times");
        } else {
            button.querySelector("a i").classList.toggle("fa-bars");
            button.querySelector("a i").classList.toggle("fa-times");
        }

        button.classList.toggle('active');

    });

    // dialog on profile management page
    $("#profile #compare-button-container.profile-upgrade-feature a").on("click", function () {
        document.querySelector(".popup-overlay").classList.remove("hidden");
        document.querySelector(".compare-runs-dialog").classList.remove("hidden");
    });

    $("#profile .cancel").on("click", function (event) {
        document.querySelector(".popup-overlay").classList.add("hidden");
        document.querySelector(".compare-runs-dialog").classList.add("hidden");
    });
});
