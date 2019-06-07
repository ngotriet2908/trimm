document.addEventListener('DOMContentLoaded', function () {

    var tokenJson = getTokenData(getCookieValue("token"));
    var usernameFromToken;

    if (tokenJson !== null) {
        usernameFromToken = tokenJson.sub;
    }

    $("#login input[type='text'], #login input[type='password']").on('keyup', function () {
        // $("#login input[type='text']").removeClass("incorrect");
        // $("#login input[type='password']").removeClass("incorrect");

        $("#login-error").addClass("login-error-hidden");
    });


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

    function getQueryParam(paramName) {
        var urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(paramName);
    }


    // get specific cookie (from https://www.w3schools.com/js/js_cookies.asp)
    function getCookieValue(cookieName) {
        var name = cookieName + "=";
        var cookies = document.cookie.split(';');
        for(var i = 0; i < cookies.length; i++) {
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
        if( n < 0) {
            negative = true;
            n = n * -1;
        }
        var multiplicator = Math.pow(10, digits);
        n = parseFloat((n * multiplicator).toFixed(11));
        n = (Math.round(n) / multiplicator).toFixed(2);
        if( negative ) {
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


    $("#save-my-information").on("click", function() {
        var firstname = $("#first-name-field").val().trim();
        var lastname = $("#last-name-field").val().trim();

        if(!(firstname === "" && lastname === "" )){
            var params = "firstname=" + firstname + "&lastname=" + lastname;

            var http = new XMLHttpRequest();

            http.onreadystatechange = function() {
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


    function hexString(buffer) {
        const byteArray = new Uint8Array(buffer);

        const hexCodes = [...byteArray].map(value => {
            const hexCode = value.toString(16);
            const paddedHexCode = hexCode.padStart(2, '0');
            return paddedHexCode;
        });


        return hexCodes.join('');
    }


    function digestMessage(message) {
        const encoder = new TextEncoder();
        const data = encoder.encode(message);
        console.log(window.crypto.subtle.digest('SHA-256', data));
        return window.crypto.subtle.digest('SHA-256', data);
    }


    // send ajax request to login
    $("#login button").on("click", function() {
        var username = $("#login input[type='text']").val().trim();
        var password = $("#login input[type='password']").val().trim();
        if( username !== "" && password !== "" ){
            var passwordsha;

            digestMessage(password).then(digestValue => {
                return (hexString(digestValue));
            }).then(hashPassword => {
                console.log(hashPassword);

                var params = "username=" + username + "&password=" + hashPassword;

                var http = new XMLHttpRequest();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 200) {
                            console.log("code 200");

                            usernameFromToken = getTokenData(getCookieValue("token")).sub;

                            window.location.replace("profiles/" + usernameFromToken);
                        } else if (http.status === 401) {
                            console.log("code 401");

                            $("#login-error").text("Incorrect username or password. Please, try again.");
                            $("#login-error").removeClass("login-error-hidden");
                            // $("#login input[type='text']").addClass("incorrect");
                            // $("#login input[type='password']").addClass("incorrect");

                        } else {
                            console.log("something else...");
                        }
                    }
                };

                http.open("POST", "login", true);
                http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                http.setRequestHeader('Accept', 'text/html');
                http.setRequestHeader('Cache-Control', 'no-store');
                http.send(params);
            });
        } else {
            // fields are empty TODO

        }
    });

    if ($("#login").length > 0) {
        displayLoginError();
    }

    function displayLoginError() {
        var error = getQueryParam("error");

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
            $("#login-error").text(error);
            $("#login-error").removeClass("login-error-hidden");
        }

        // show the page? TODO
    }


    $("#password-reset-enter button").on("click", function() {
        var recoveryToken = getQueryParam("token");
        var recoveryPassword = $("#password-reset-enter form input:nth-child(2)").val().trim();
        var recoveryPasswordConfirm = $("#password-reset-enter form input:nth-child(3)").val().trim();

        if( recoveryToken !== "" && recoveryPassword !== "" && recoveryPassword === recoveryPasswordConfirm){
            var passwordsha;

            digestMessage(recoveryPassword).then(digestValue => {
                return (hexString(digestValue));
            }).then(hashPassword => {
                console.log(hashPassword);

                var params = "token=" + recoveryToken + "&password=" + hashPassword;

                var http = new XMLHttpRequest();

                http.onreadystatechange = function () {
                    if (http.readyState === XMLHttpRequest.DONE) {
                        if (http.status === 200) {
                            console.log("code 200");

                            window.location.replace("/runner/login"); // TODO
                        } else if (http.status === 401) {
                            console.log("code 401");
                            $("#login-error").removeClass("login-error-hidden");
                            // $("#login input[type='text']").addClass("incorrect");
                            // $("#login input[type='password']").addClass("incorrect");
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
            });
        } else {
            // fields are empty TODO

        }
    });

    $("#password-reset-request button").on("click", function() {
        var username = $("#password-reset-request form input:nth-child(2)").val().trim();
        if(username !== ""){

            var param = "username=" + username;

            var http = new XMLHttpRequest();

            http.onreadystatechange = function () {
                if (http.readyState === XMLHttpRequest.DONE) {
                    if (http.status === 200) {
                        console.log("code 200");
                        window.location.replace("/");
                        // TODO email with reset was sent, show success message
                    } else if (http.status === 401) {
                        console.log("code 401");
                        $("#login-error").removeClass("login-error-hidden");
                        // $("#login input[type='text']").addClass("incorrect");
                        // $("#login input[type='password']").addClass("incorrect");
                    } else {
                        console.log("something else...");
                    }
                }
            };

            http.open("POST", "/runner/password/reset", true);
            http.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            http.setRequestHeader('Accept', 'text/html'); // TODO
            http.setRequestHeader('Cache-Control', 'no-store');
            http.send(param);
        } else {
            // fields are empty TODO

        }
    });








    // TODO what if session id expired/server restarted?
    // if user is already logged in, show link to user's profile page and not "login"
    // username = getTokenData(getCookieValue("token")).sub;

    if (tokenJson !== null && tokenJson.exp > Math.floor(Date.now()/1000)) {
        $("#index nav ul").append("<li class='nav-item'><a href='/runner/profiles/" + usernameFromToken + "'>profile</a></li>");
        $("#index nav ul").append("<li class='nav-item'><a href='/runner/logout'>sign out</a></li>");
    } else {
        $("#index nav ul").append("<li class='nav-item'><a href='/runner/register'>sign up</a></li>");
        $("#index nav ul").append("<li class='nav-item'><a href='/runner/login'>sign in</a></li>");
    }


    // loading profile info. All processing should be handled by the server, browser receives only the result
    if ($("#profile").length > 0) {

        console.log("loading " + usernameFromToken + " profile data");

        var http = new XMLHttpRequest();
        var url = "/runner/profiles/" + usernameFromToken;

        http.open('GET', url, true);
        http.setRequestHeader('Accept', 'application/json');
        http.setRequestHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
        http.setRequestHeader('Pragma', 'no-cache');
        http.setRequestHeader('Expires', '0');


        http.onreadystatechange = function() {
            if(http.readyState === 4 && http.status === 200) {
                var parsedResponse = JSON.parse(http.response);

                console.log(parsedResponse);

                $("#profile-name span")[0].innerText = parsedResponse.firstName + " " + parsedResponse.lastName;

                if (parsedResponse.isPremium === 1) {
                    $("#profile-name").append('<span id="pro">PRO</span>');
                }

                // convert to km
                $("#overview-distance p span")[0].innerText = Math.round(parsedResponse.totalDistance / 1000);

                // convert to hours
                $("#overview-time p span")[0].innerText = Math.round(parsedResponse.totalTime / 3600);
                $("#overview-steps p span")[0].innerText = Math.round(parsedResponse.totalSteps / 1000) + "k";


                document.querySelector("#profile-picture-general div:nth-child(1) p span:nth-child(2)").innerText = parsedResponse.totalRuns;
                document.querySelector("#profile-picture-general div:nth-child(2) p span:nth-child(2)").innerText = Math.round(parsedResponse.totalDistance / 1000) + "km";
                document.querySelector("#profile-picture-general div:nth-child(3) p span:nth-child(2)").innerText = parsedResponse.totalSteps;

                if (parsedResponse.runsList.length > 0) {
                    var $container = $("#activity-data-container table");

                    // convert to date and check if 0 is the most recent run todo
                    document.querySelector("#profile-picture-general div:nth-child(4) p:nth-child(2) span").innerText =
                        parsedResponse.runsList[0].name + " - " + parsedResponse.runsList[0].date;

                    // table
                    for (var i = 0; i < parsedResponse.runsList.length; i++) {
                        $container.append("<tr><td>" + parsedResponse.runsList[i].date + "</td>" +
                            "<td><a href='" + "/runner/runs/" + parsedResponse.runsList[i].id + "'>" + parsedResponse.runsList[i].name + "</a></td>" +
                            "<td>" + roundTo(parsedResponse.runsList[i].distance / 1000, 2) + " km</td>" +
                            "<td>" + convertTime(parsedResponse.runsList[i].duration) + "</td>" +
                            "<td>" + parsedResponse.runsList[i].steps + "</td></tr>");
                    }

                } else {
                    var $container = $("#activity-data-container");

                    // TODO
                }

                // show the hidden page
                $("#profile").css("display", "flex");
            }
        };
        http.send();
    }




    // upload photo in profile management
    // during the waiting time, show loading animation instead of the old photo, the load a new photo
    $("#profile-image-field").on( "change", function (event) {
        var input = document.getElementById("profile-image-field");
        var link = input.value;
        var extension = link.substring(link.lastIndexOf('.') + 1).toLowerCase();

        if (input.files && input.files[0]&& (extension === "gif" || extension === "png" || extension === "jpeg" || extension === "jpg")) {
            var reader = new FileReader();

            console.log("loading new image...");

            reader.onload = function (e) {
                $('#profile-management-picture-img img').attr('src', e.target.result);
            };

            reader.readAsDataURL(input.files[0]);
        } else {
            // todo
            $('#profile-management-picture-img img').attr('src', '/img/no_preview.png');
        }
    } );


    // todo
    function uploadImage(id) {
        var blobFile = $('#' + id).files[0];
        var image = new FormData();
        image.append("picture", blobFile);

        $.ajax({
            url: "/runner/profile/" + usernameFromToken + "/picture",
            type: "POST",
            data: image,
            processData: false,
            contentType: false,
            success: function(response) {


            },
            error: function(xhr, status, errorMessage) {
                console.log(errorMessage);
            }
        });
    }


    // password confirmation checker
    var check = function() {
        if (document.getElementById('password').value === document.getElementById('confirm_password').value) {
            document.getElementById('message').style.color = 'green';
            document.getElementById('message').innerHTML = 'matching';
        } else {
            document.getElementById('message').style.color = 'red';
            document.getElementById('message').innerHTML = 'not matching';
        }
    };


    // send ajax request for user to register
    $("#register form button").on("click", function() {
        var username = $("#register form input[name='username']").val().trim();
        var firstName = $("#register form input[name='firstName']").val().trim();
        var lastName = $("#register form input[name='lastName']").val().trim();
        var email = $("#register form input[name='email']").val().trim();
        var password = $("#register form input[name='password']").val().trim();

        digestMessage(password).then(digestValue => {
            return (hexString(digestValue));
        }).then(hashPassword => {


            console.log(username);
            console.log(firstName);
            console.log(lastName);
            console.log(email);
            console.log(hashPassword);

            if (username !== "" && password !== "") { // etc. TODO
                var params = "username=" + username + "&password=" + hashPassword +
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
        })
    });


    // loading data to show on settings page
    if ($("#profile-management").length > 0) {

        console.log("loading " + usernameFromToken + " profile data");

        var http = new XMLHttpRequest();
        var url = "/runner/profiles/" + usernameFromToken;

        http.open('GET', url, true);
        http.setRequestHeader('Accept', 'application/json');
        http.setRequestHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
        http.setRequestHeader('Pragma', 'no-cache');
        http.setRequestHeader('Expires', '0');

        http.onreadystatechange = function() {
            if(http.readyState === 4 && http.status === 200) {
                var parsedResponse = JSON.parse(http.response);

                console.log(parsedResponse);

                $("#profile-management-name span")[0].innerText = parsedResponse.firstName + " " + parsedResponse.lastName;

                if (parsedResponse.isPremium === 1) {
                    $("#profile-management-name").append('<span id="pro">PRO</span>');
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
        console.log(url);
        http.open('GET', url, true);
        http.setRequestHeader('Accept', 'application/json');

        http.onreadystatechange = function () {
            if (http.readyState === 4 && http.status === 200) {
                var parsedResponse = JSON.parse(http.response);
                document.querySelector("#dashboard-overview-container-grid div:nth-child(2) span").innerText ="Date \n" + parsedResponse.date;
                document.querySelector("#dashboard-overview-container-grid div:nth-child(5) span").innerText ="Distance \n" + parsedResponse.distance + "m";
                document.querySelector("#dashboard-overview-container-grid div:nth-child(4) span").innerText ="Duration \n" + convertTime(parsedResponse.duration);
                document.querySelector("#dashboard-overview-container-grid div:nth-child(6) span").innerText ="Steps \n" +parsedResponse.steps;
                document.querySelector("#dashboard-overview-container-grid div:nth-child(1) span").innerText ="Title \n" + parsedResponse.name;
            }
        };
        http.send();
    }
});
