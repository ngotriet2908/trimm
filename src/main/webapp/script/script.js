document.addEventListener('DOMContentLoaded', function () {
    var tokenJson = getTokenData(getCookieValue("token"));
    var usernameFromToken;

    if (tokenJson !== null) {
        usernameFromToken = tokenJson.sub;
    }

    function getTokenData(token) {
        if (token === null) {
            return null;
        }

        var encoded = token.split(".")[1];

        var decoded = atob(encoded);

        var decodedJson = JSON.parse(decoded);

        return decodedJson;
    }

    function getQueryParam(paramName) {
        var urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(paramName);
    }


    // get specific cookie (from https://www.w3schools.com/js/js_cookies.asp)
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


    // rounder (from https://stackoverflow.com/questions/15762768/javascript-math-round-to-two-decimal-places/22977058)
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

    function convertTime(seconds) {
        var hours = Math.round(seconds / 3600);

        var minutes = Math.round((seconds % 3600) / 60);
        seconds = Math.round((seconds % 3600) % 60);

        return hours + ":" + minutes + ":" + seconds;
    }

    if ($("#profile-management").length > 0) {

        $("#save-my-information").on("click", function () {
            var firstname = $("#first-name-field").val().trim();
            var lastname = $("#last-name-field").val().trim();

            if (!(firstname === "" && lastname === "")) {
                var params = "firstname=" + firstname + "&lastname=" + lastname;

                var http = new XMLHttpRequest();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 204) {
                            console.log("code 204");
                            window.location.replace("settings"); // TODO
                        } else {
                            console.log("something else...");
                        }
                    }
                };

                http.open("PUT", "settings", true);
                http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send(params);
            }
        });
    }



    if ($("#login").length > 0) {
        var loginForm = document.querySelector("#login form");

        loginForm.addEventListener("submit", function (event) {
            event.preventDefault();
            submitLoginForm();
        });

    }

    // send ajax request to login
    function submitLoginForm() {
        var username = $("#login input[type='text']").val().trim();
        var password = $("#login input[type='password']").val().trim();
        if (username !== "" && password !== "") {

            var params = "username=" + username + "&password=" + password;

            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200) {
                        console.log("code 200");

                        usernameFromToken = getTokenData(getCookieValue("token")).sub;

                        window.location.replace("/runner/profiles/" + usernameFromToken);
                    } else if (http.status === 401) {
                        console.log("code 401");

                        $("#response-message").text("Incorrect username or password. Please, try again.");
                        $("#response-message").removeClass("response-message-hidden");
                        $("#response-message").addClass("response-message-hidden-red");

                        // $("#login input[type='text']").addClass("incorrect");
                        // $("#login input[type='password']").addClass("incorrect");

                    } else if (http.status === 402) {
                        console.log("code 402");

                        $("#response-message").text("Account is not activated");
                        $("#response-message").removeClass("response-message-hidden");
                        $("#response-message").addClass("response-message-hidden-red");

                        // $("#login input[type='text']").addClass("incorrect");
                        // $("#login input[type='password']").addClass("incorrect");

                    } else {
                        console.log("something else...");
                    }
                }
            };

            http.open("POST", "/runner/login", true);
            http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            http.setRequestHeader('Accept', 'text/html');
            http.setRequestHeader('Cache-Control', 'no-store');
            http.send(params);
        } else {
            // fields are empty TODO

        }
    }

    if ($("#login").length > 0) {
        displayLoginMessage();
    }

    function displayLoginMessage() {
        var error = getQueryParam("error");
        var message = getQueryParam("message");

        if (error != null) {
            console.log(error);

            if (error == "not_authorized") {
                error = "You need to sign in to access this page."
            } else if (error = "token_expired") {
                error = "Your session expired. Sign in to access this page."
            } else if (error = "incorrect_credentials") {
                error = "Incorrect username or password. Please, try again."
            }

            // insert error into the page
            $("#response-message").text(error);
            $("#response-message").removeClass("response-message-hidden");
            $("#response-message").addClass("response-message-hidden-red");
        }

        if (message != null) {
            console.log(message);

            if (message === "reset_success") {
                message = "Password was successfully reset."
            } else if (message === "activate_success") {
                message = "Account was successfully activated."
            }

            // insert error into the page
            $("#response-message").text(message);
            $("#response-message").removeClass("response-message-hidden");
            $("#response-message").addClass("response-message-hidden-green");
        }

        // show the page? TODO
    }


    if ($("#password-reset-enter").length > 0) {
        var resetEnterForm = document.querySelector("form");
        resetEnterForm.addEventListener("submit", function (ev) {
            ev.preventDefault();
            handleResetEnterFormSubmit();
        });

        function handleResetEnterFormSubmit() {
            var recoveryToken = getQueryParam("token");
            var recoveryPassword = $("#password-reset-enter form input[name=password]").val().trim();
            var recoveryPasswordConfirm = $("#password-reset-enter form input[name=confirm-password]").val().trim();

            if (recoveryToken !== "" && recoveryPassword !== "" && recoveryPassword === recoveryPasswordConfirm) {

                var params = "token=" + recoveryToken + "&password=" + recoveryPassword;

                var http = new XMLHttpRequest();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 200) {
                            console.log("code 200");

                            window.location.replace("/runner/login?message=reset_success"); // TODO
                        } else if (http.status === 401) {
                            console.log("code 401");
                            $("#response-message").text("Access not allowed.");
                            $("#response-message").removeClass("response-message-hidden");
                        } else if (http.status === 402) {
                            console.log("code 401");
                            $("#response-message").text("Account is not activate");
                            $("#response-message").removeClass("response-message-hidden");
                        } else if (http.status === 404) {
                            $("#response-message").text("Password reset link is invalid.");
                            $("#response-message").removeClass("response-message-hidden");
                        } else {
                            console.log("something else...");
                        }
                    }
                };

                http.open("POST", "/runner/password/reset/enter", true);
                http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                http.setRequestHeader('Accept', 'text/html');
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send(params);
            } else {
                // fields are empty TODO

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

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 200) {
                            console.log("code 200");

                            // window.location.replace("/");
                            document.querySelector(".progress").classList.remove("progress-hidden");
                            // TODO email with reset was sent, show success message

                            document.querySelector(".progress").classList.add("progress-hidden");
                            document.querySelector(".reset-success").classList.remove("reset-success-hidden");
                        } else if (http.status === 401) {
                            console.log("code 401");
                            document.querySelector(".progress").classList.add("progress-hidden");
                            $("#response-message").removeClass("response-message-hidden");
                        } else {
                            console.log("something else...");
                            document.querySelector(".progress").classList.add("progress-hidden");
                            $("#response-message").removeClass("response-message-hidden");
                        }
                    }
                };

                http.open("POST", "/runner/password/reset", true);
                http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                http.setRequestHeader('Accept', 'text/html'); // TODO
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send(param);

                // show loading window
                document.querySelector(".progress").classList.remove("progress-hidden");
            } else {
                // fields are empty TODO

            }
        }
    }

    // TODO what if session id expired/server restarted?
    // if user is already logged in, show link to user's profile page and not "login"
    // username = getTokenData(getCookieValue("token")).sub;

    if (tokenJson !== null && tokenJson.exp > Math.floor(Date.now() / 1000)) {
        $("#index nav ul").append("<li class='nav-item'><a href='/runner/profiles/" + usernameFromToken + "'>profile</a></li>");
        $("#index nav ul").append("<li class='nav-item'><a href='/runner/logout'>sign out</a></li>");
    } else {
        $("#index nav ul").append("<li class='nav-item'><a href='/runner/register'>sign up</a></li>");
        $("#index nav ul").append("<li class='nav-item'><a href='/runner/login'>sign in</a></li>");
    }


    // loading profile info. All processing should be handled by the server, browser receives only the result
    if ($("#profile").length > 0) {

        getImage(usernameFromToken);

        console.log("loading " + usernameFromToken + " profile data");

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

                console.log(parsedResponse);

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

                if (parsedResponse.runsList.length > 0) {
                    var $container = $("#runs-data-container");

                    // convert to date and check if 0 is the most recent run todo
                    var lastRunDate = new Date(parsedResponse.runsList[0].date);

                    document.querySelector("#profile-picture-general div:nth-child(4) p:nth-child(2) span").innerText =
                        parsedResponse.runsList[0].name + " - " + lastRunDate.toDateString();

                    // table
                    for (var i = 0; i < parsedResponse.runsList.length; i++) {
                        // $container.append("<tr><td>" + parsedResponse.runsList[i].date + "</td>" +
                        //     "<td><a href='" + "/runner/runs/" + parsedResponse.runsList[i].id + "'>" + parsedResponse.runsList[i].name + "</a></td>" +
                        //     "<td>" + roundTo(parsedResponse.runsList[i].distance / 1000, 2) + " km</td>" +
                        //     "<td>" + convertTime(parsedResponse.runsList[i].duration) + "</td>" +
                        //     "<td>" + parsedResponse.runsList[i].steps + "</td></tr>");

                        var runDate = new Date(parsedResponse.runsList[i].date);

                        $container.append('<div class="profile-card profile-run-card">' +
                            // '<div class="run-card-title"><h4><a href="/">' + parsedResponse.runsList[i].name + '</a></h4></div>\n' +
                            // '<div class="run-card-date">' + runDate.toDateString() + '</div>\n' +
                            // '<div class="run-card-blocks">\n' +
                            // '<div class="run-card-distance">\n' +
                            // '<div>Distance</div>\n' +
                            // '<div>' + roundTo(parsedResponse.runsList[i].distance / 1000, 2) + ' km</div>\n' +
                            // '</div>\n' +
                            // '<div class="run-card-time">\n' +
                            // '<div>Time</div>\n' +
                            // '<div>' + convertTime(parsedResponse.runsList[i].duration) + '</div>\n' +
                            // '</div>\n' +
                            // '<div class="run-card-steps">\n' +
                            // '<div>Steps</div>\n' +
                            // '<div>' + parsedResponse.runsList[i].steps + '</div>\n' +
                            // '</div>\n' +
                            // '<div class="run-card-see-more"><a class="button-raised" href="' +
                            // "/runner/runs/" + parsedResponse.runsList[i].id + '"><span>More</span></a></div>\n' +
                            // '</div>\n' +
                            // '</div>');

                            '<div class="run-card-blocks"><div><div class="run-card-title"><h4><a href="/">' + parsedResponse.runsList[i].name + '</a></h4></div><div class="run-card-date">' + runDate.toDateString() + '</div></div><div class="run-card-distance"><div>Distance</div><div>' + roundTo(parsedResponse.runsList[i].distance / 1000, 2) + ' km</div></div><div class="run-card-time"><div>Time</div><div>' + convertTime(parsedResponse.runsList[i].duration) + '</div></div><div class="run-card-steps"><div>Steps</div><div>' + parsedResponse.runsList[i].steps + '</div></div><div class="run-card-see-more"><a class="button-raised" href="/runner/runs/' + parsedResponse.runsList[i].id + '"><span><i class="fas fa-angle-right"></i></span></a></div></div></div>');
                    }

                } else {
                    // var $container = $("#runs-data-container");

                    // TODO
                }

                // show the hidden page
                $("#profile").css("display", "flex");
            }
        };
        http.send();
    }


    $("#select-sort-type").on("change", function (event) {
        console.log("sorting stuff");
        var type = $("#select-sort-type").val();

        var toSort = document.getElementById("runs-data-container").children;
        toSort = Array.prototype.slice.call(toSort, 0);

        console.log(toSort);

        if (type === "dateu") {
            toSort.sort(function(a, b) {
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
            toSort.sort(function(a, b) {
                var x = a.querySelector(".run-card-time div:nth-of-type(2)").innerText;
                var y = b.querySelector(".run-card-time div:nth-of-type(2)").innerText;

                var XX = x.split(":");
                var YY = y.split(":");

                var secondx = Number(XX[0])*3600 + Number(XX[1])*60 + Number(XX[2]);
                var secondy = Number(YY[0])*3600 + Number(YY[1])*60 + Number(YY[2]);

                if (secondx > secondy) {
                    return 1;
                } else if (secondx < secondy) {
                    return -1;
                } else {
                    return 0;
                }

            });
        } else if (type === "stepu") {
            toSort.sort(function(a, b) {
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
            toSort.sort(function(a, b) {
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
            toSort.sort(function(a, b) {
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
            toSort.sort(function(a, b) {
                var x = a.querySelector(".run-card-time div:nth-of-type(2)").innerText;
                var y = b.querySelector(".run-card-time div:nth-of-type(2)").innerText;

                var XX = x.split(":");
                var YY = y.split(":");

                var secondx = Number(XX[0])*3600 + Number(XX[1])*60 + Number(XX[2]);
                var secondy = Number(YY[0])*3600 + Number(YY[1])*60 + Number(YY[2]);

                if (secondx < secondy) {
                    return 1;
                } else if (secondx > secondy) {
                    return -1;
                } else {
                    return 0;
                }

            });
        } else if (type === "stepd") {
            toSort.sort(function(a, b) {
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
            toSort.sort(function(a, b) {
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

        for(var i = 0, l = toSort.length; i < l; i++) {
            parent.appendChild(toSort[i]);
        }

    });

    // upload photo in profile management
    // during the waiting time, show loading animation instead of the old photo, the load a new photo
    $("#profile-image-field").on("change", function (event) {
        var input = document.getElementById("profile-image-field");
        var link = input.value;
        var extension = link.substring(link.lastIndexOf('.') + 1).toLowerCase();

        if (input.files && input.files[0] && (extension === "gif" || extension === "png" || extension === "jpeg" || extension === "jpg")) {
            var reader = new FileReader();

            console.log("loading new image...");
            // uploadImage(input);
            reader.onload = function (e) {
                $('#profile-picture-img img').attr('src', e.target.result);
            };

            reader.readAsDataURL(input.files[0]);
        } else {
            // todo
            $('#profile-picture-img img').attr('src', '/img/no_preview.png');
        }
    });

    // $("#save-profile-image").on("click", function () {
    //     var input = document.getElementById("profile-image-field");
    //     uploadImage(input);
    // });

    // todo
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
        console.log("loading " + username + " profile data");

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
                // TODO
            }
            // show the hidden page
        };
        http.send();
    }

    function getImageForSettings(username) {
        console.log("loading " + username + " profile data");

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
                // TODO
            }
            // show the hidden page
        };
        http.send();
    }


    // password confirmation checker
    var check = function () {
        if (document.getElementById('password').value === document.getElementById('confirm_password').value) {
            document.getElementById('message').style.color = 'green';
            document.getElementById('message').innerHTML = 'matching';
        } else {
            document.getElementById('message').style.color = 'red';
            document.getElementById('message').innerHTML = 'not matching';
        }
    };


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
                    message: "can only contain letters, digigts and underscores"
                }
            },
            "first-name": {
                presence: true,
                format: {
                    pattern: "[\\p{L}\\s\\-]+",
                    message: "can only contain letters, spaces and dashes"
                }
            },
            "last-name": {
                presence: true,
                format: {
                    pattern: "[\\p{L}\\s\\-]+",
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

                        } else if (http.status === 404) {
                            // show error
                            console.log("404")
                        } else {
                            console.log("something else: " + http.status);
                        }
                    }
                };

                http.open("GET", "/runner/register/username?" + param, true);
                http.setRequestHeader('Accept', 'application/json'); // TODO
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send();
            });
        };

        var usernameConstraints = {
            username: {
                usernameAvailabilityValidator: true
            }
        };

        var success = function () {
            // alert("The validations passed");
            showErrorsForInput(document.querySelector("#register input[name='username']"), null);
        };


        var error = function (errors) {
            showErrorsForInput(document.querySelector("#register input[name='username']"), [errors]);
        };


        var form = document.querySelector("form");
        form.addEventListener("submit", function (ev) {
            ev.preventDefault();
            handleFormSubmit(form);
        });

        // validate inputs on fly
        var inputs = document.querySelectorAll("input, textarea, select");
        for (var i = 0; i < inputs.length; ++i) {
            inputs.item(i).addEventListener("change", function (event) {
                var errors = validate(form, constraints) || {};

                // if no errors for input username and username was typed, then check it for availability
                if (!errors.hasOwnProperty("username") && event.target.id === "username") {
                    // Will call the success callback
                    validate.async({username: this.value}, usernameConstraints).then(success, error);
                }

                // if not available, add to errors
                showErrorsForInput(this, errors[this.name])
            });
        }

        function handleFormSubmit(form, input) {
            // validate the form against the constraints
            var errors = validate(form, constraints);
            // then we update the form to reflect the results
            showErrors(form, errors || {});
            if (!errors) {
                submitRegistrationForm();
            }
        }

        // Updates the inputs with the validation errors
        function showErrors(form, errors) {
            // We loop through all the inputs and show the errors for that input
            form.querySelectorAll("input[name], select[name]").forEach(function (input) {
                // Since the errors can be null if no errors were found we need to handle that
                showErrorsForInput(input, errors && errors[input.name]);
            });
        }

        // Shows the errors for a specific input
        function showErrorsForInput(input, errors) {
            // This is the root of the input
            var formGroup = closestParent(input.parentNode, "input-group")
                // Find where the error messages will be insert into
                , messages = formGroup.querySelector(".messages");
            // First we remove any old messages and resets the classes
            resetFormGroup(formGroup);
            // If we have errors
            if (errors) {
                // we first mark the group has having errors
                formGroup.classList.add("has-error");

                // add icon and tooltip
                var errorElem = document.createElement("span");
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

                formGroup.appendChild(errorElem);
            } else {
                // otherwise we simply mark it as success
                formGroup.classList.add("has-success");

                var errorElem = formGroup.querySelector("input-error");
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
            // Remove the success and error classes
            formGroup.classList.remove("has-error");
            formGroup.classList.remove("has-success");

            // and remove any old messages
            formGroup.querySelectorAll(".input-error").forEach(function (el) {
                el.remove();
            });
        }

        // Adds the specified error with the following markup
        // <p class="help-block error">[message]</p>
        function addError(errorElem, error) {
            var block = document.createElement("p");
            block.classList.add("help-block");
            block.classList.add("error");
            block.innerText = error;
            errorElem.appendChild(block);
        }
    }


    // send ajax request for user to register
    // $("#register form button button").on("click", function(event) {
    function submitRegistrationForm() {
        // event.preventDefault();

        var username = $("#register form input[name='username']").val().trim();
        var firstName = $("#register form input[name='first-name']").val().trim();
        var lastName = $("#register form input[name='last-name']").val().trim();
        var email = $("#register form input[name='email']").val().trim();
        var password = $("#register form input[name='password']").val().trim();

        if (username !== "" && password !== "") { // etc. TODO
            var params = "username=" + username + "&password=" + password +
                "&first_name=" + firstName + "&last_name=" + lastName + "&email=" + email;

            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200) {
                        console.log("code 200");
                        // redirect to user's newly created profile
                        window.location.replace("login");
                    } else if (http.status === 401) {
                        console.log("code 400");

                        // unauthorized
                    } else {
                        console.log("something else...");
                        // TODO
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


    // loading data to show on settings page
    if ($("#profile-management").length > 0) {
        console.log("loading " + usernameFromToken + " profile data");

        getImageForSettings(usernameFromToken);

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
                $("#first-name-field").val(parsedResponse.firstName);
                $("#last-name-field").val(parsedResponse.lastName);

                // show the hidden page
                $("#profile-management").css("display", "flex");

            } else {
                // something bad happened
                console.log(http.status);
            }
        };
        http.send();
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
                    loadLayoutOption();
                    loadFavoriteLayoutOption();
                }
            }
        };
        http.send();
    }

    function loadLayoutOption() {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {
                    // show overlay
                    var parsedResponse = JSON.parse(http.response);

                    var x = document.getElementById("select-layout-selection");
                    $("#select-layout-selection").empty();
                    for(var i = 0; i < parsedResponse.layoutDataList.length; i++) {
                        var option = document.createElement("option");
                        option.text = parsedResponse.layoutDataList[i].name;
                        option.setAttribute("val", parsedResponse.layoutDataList[i].layoutID);
                        if (parsedResponse.currentLayout === parsedResponse.layoutDataList[i].layoutID) {
                            x.add(option);
                        }
                    }
                    for(var i = 0; i < parsedResponse.layoutDataList.length; i++) {
                        var option = document.createElement("option");
                        option.text = parsedResponse.layoutDataList[i].name;
                        option.setAttribute("val", parsedResponse.layoutDataList[i].layoutID);
                        if (parsedResponse.currentLayout !== parsedResponse.layoutDataList[i].layoutID) {
                            x.add(option);
                        }
                    }
                } else if (http.status === 401) {
                    console.log("code 401");


                } else {
                    console.log("something else...");
                }
            }
        };

        http.open("GET", window.location.href + "/layout/name", true);
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }

    function loadFavoriteLayoutOption() {
        var http = new XMLHttpRequest();

        http.onreadystatechange = function () {
            if (http.readyState === XMLHttpRequest.DONE) {
                if (http.status === 200 || http.status === 204) {
                    var parsedResponse = JSON.parse(http.response);

                    var x = document.getElementById("favorite-layout-selection");
                    $("#favorite-layout-selection").empty();
                    for(var i = 0; i < parsedResponse.layoutDataList.length; i++) {
                        var option = document.createElement("option");
                        option.text = parsedResponse.layoutDataList[i].name;
                        option.setAttribute("val", parsedResponse.layoutDataList[i].layoutID);
                        x.add(option);
                    }
                } else if (http.status === 401) {
                    console.log("code 401");


                } else {
                    console.log("something else...");
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
                    console.log("code 200");

                    window.location.replace("/runner/profiles"); // TODO
                } else if (http.status === 401) {
                    console.log("code 400");

                    // unauthorized
                } else {
                    console.log("something else...");
                }
            }
        };

        http.open("POST", "/runner/premium/join", true);
        http.setRequestHeader('Accept', 'text/html');
        http.setRequestHeader('Cache-Control', 'no-store');
        http.send();
    }


    // dialog on profile management page
    $("#profile-management #upload-new-picture").on("click", function (event) {
        // show overlay
        document.querySelector(".popup-overlay").classList.remove("hidden");

        // show popup
        document.querySelector(".upload-new-picture-dialog").classList.remove("hidden");
    });

    // upload-new-picture-dialog
    $('.upload-new-picture-dialog .upload').on('click', function (event) {
        var input = document.getElementById("profile-image-field");
        uploadImage(input);

        // hide popup
        document.querySelector(".upload-new-picture-dialog").classList.add("hidden");

        // hide overlay
        document.querySelector(".popup-overlay").classList.add("hidden");
    });


    $("#profile-management .cancel").on("click", function (event) {
        // hide
        document.querySelector(".popup-overlay").classList.add("hidden");

        // remove all dialogs
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

});
