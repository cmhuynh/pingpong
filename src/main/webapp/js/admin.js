var org = org || {};

org.cmhuynh = org.cmhuynh || {};

org.cmhuynh.pingpong = org.cmhuynh.pingpong || {};

org.cmhuynh.pingpong.admin = org.cmhuynh.pingpong.admin || {};

/**
 * Client ID of the application (from the APIs Console).
 * @type {string}
 */
org.cmhuynh.pingpong.admin.CLIENT_ID =
    '594724891312-h9v8svhh468ei3uquo0lha5bamq2op3e.apps.googleusercontent.com';

/**
 * Scopes used by the application.
 * @type {string}
 */
org.cmhuynh.pingpong.admin.SCOPES =
    'https://www.googleapis.com/auth/userinfo.email';

/**
 * Email of current signing in user, otherwise undefine
 * @type {String}
 */
org.cmhuynh.pingpong.admin.userEmail = undefined;

/**
 * Loads the application UI after the user has completed auth.
 */
org.cmhuynh.pingpong.admin.userAuthed = function() {
  var request = gapi.client.oauth2.userinfo.get().execute(function(resp) {
    if (!resp.code) {
        org.cmhuynh.pingpong.admin.userEmail = resp.email;
        org.cmhuynh.pingpong.admin.loadClubs(); // backend has access to logging in user, better than sending here
        $("#signinButton").text("Sign out");
    } else {
        org.cmhuynh.pingpong.admin.userEmail = undefined;
        org.cmhuynh.pingpong.admin.resetClubs();
        $("#signinButton").text("Sign in");
    }
  });
};

/**
 * Handles the auth flow, with the given value for immediate mode.
 * @param {boolean} mode Whether or not to use immediate mode.
 * @param {Function} callback Callback to call on completion.
 */
org.cmhuynh.pingpong.admin.signin = function(mode, callback) {
  gapi.auth.authorize({client_id: org.cmhuynh.pingpong.admin.CLIENT_ID,
      scope: org.cmhuynh.pingpong.admin.SCOPES, immediate: mode},
      callback);
};

/**
 * Presents the user with the authorization popup.
 */
org.cmhuynh.pingpong.admin.auth = function() {
  if (!org.cmhuynh.pingpong.admin.userEmail) {
    org.cmhuynh.pingpong.admin.signin(false, org.cmhuynh.pingpong.admin.userAuthed);
  } else {
    org.cmhuynh.pingpong.admin.userEmail = undefined;
    org.cmhuynh.pingpong.admin.resetClubs();
  }
};

org.cmhuynh.pingpong.admin.handleError = function(resp) {
    if(!resp.code) {
        console.log(resp.message);
    }
};

org.cmhuynh.pingpong.admin.resetClubs = function() {
    org.cmhuynh.pingpong.admin._clearDropdownOptions(
        $(".j-form-player ul.j-dropdown-menu-club").children(),
        $(".j-form-player button.j-dropdown-toggle-club"),
        "Your Club",
        $(".j-form-player .j-selected-club")
    );

    org.cmhuynh.pingpong.admin._clearDropdownOptions(
        $(".j-form-match ul.j-dropdown-menu-club").children(),
        $(".j-form-match button.j-dropdown-toggle-club"),
        "Your Club",
        $(".j-form-match .j-selected-club")
    );

    org.cmhuynh.pingpong.admin.resetPlayers();
};

org.cmhuynh.pingpong.admin._clearDropdownOptions = function(options, button, buttonText, input) {
    $.each(options, function(index, element) {
        element.remove();
    });
    $(button).html(buttonText + " <span class='caret'></span>");
    $(input).val("");
};

org.cmhuynh.pingpong.admin._buildClubOptions = function(clubs, ul, button, input, clickCallBack) {
    $.each(clubs, function(index, element) {
        var li = $('<li/>')
            .prop('clubId', element.clubId)
            .appendTo(ul);
        $('<a/>', {
            "text": element.name,
            click: function(event) {
                var selText = $(this).text();
                $(button).html(selText+' <span class="caret"></span>');
                var selValue = $(this).parent().prop("clubId");
                $(input).val(selValue);
                if(clickCallBack) {
                    clickCallBack(selValue);
                }
            }
        }).appendTo(li);
    });
};

org.cmhuynh.pingpong.admin.resetPlayers = function() {
    org.cmhuynh.pingpong.admin._clearDropdownOptions(
        $(".j-form-match ul.j-dropdown-menu-player1").children(),
        $(".j-form-match button.j-dropdown-toggle-player1"),
        "Select Player",
        $(".j-form-match .j-selected-player1")
    );

    org.cmhuynh.pingpong.admin._clearDropdownOptions(
        $(".j-form-match ul.j-dropdown-menu-player2").children(),
        $(".j-form-match button.j-dropdown-toggle-player2"),
        "Select Player",
        $(".j-form-match .j-selected-player2")
    );
};

org.cmhuynh.pingpong.admin.loadClubs = function() {
    org.cmhuynh.pingpong.admin.resetClubs();

    if (!org.cmhuynh.pingpong.admin.userEmail) { // not logging in
        return;
    }

    gapi.client.pingpong.admin.getClubsByAdmin().execute(function(resp) {
        if (!resp.code) {
            // sort club by name
            var clubs = resp.items || [];
            clubs = clubs.filter(function(value) {
                return value.status;
            });
            clubs.sort(function(c1, c2) {
                return c1.name.toLowerCase < c2.name.toLowerCase;
            });

            org.cmhuynh.pingpong.admin._buildClubOptions(clubs,
                $(".j-form-player ul.j-dropdown-menu-club"),
                $(".j-form-player button.j-dropdown-toggle-club"),
                $(".j-form-player .j-selected-club")
            );

            org.cmhuynh.pingpong.admin._buildClubOptions(clubs,
                $(".j-form-match ul.j-dropdown-menu-club"),
                $(".j-form-match button.j-dropdown-toggle-club"),
                $(".j-form-match .j-selected-club"),
                org.cmhuynh.pingpong.admin.loadPlayers // callback to render players on choosing a club
            );
        } else {
            org.cmhuynh.pingpong.admin.handleError(resp);
        }
    });
};

org.cmhuynh.pingpong.admin.loadPlayers = function(clubId) {
    org.cmhuynh.pingpong.admin.resetPlayers();

    gapi.client.pingpong.pingpongs.getPlayers({"clubId": clubId}).execute(
    function(resp) {
        if (!resp.code) {
            var players = resp.items || [];
            players = players.filter(function(value) {
                return value.status;
            });
            players.sort(function(p1, p2) {
                return p2.name.toLowerCase() - p1.name.toLowerCase();
            });

            org.cmhuynh.pingpong.admin._buildPlayerOptions(players,
                $(".j-form-match ul.j-dropdown-menu-player1"),
                $(".j-form-match button.j-dropdown-toggle-player1"),
                $(".j-form-match .j-selected-player1")
            );
            org.cmhuynh.pingpong.admin._buildPlayerOptions(players,
                $(".j-form-match ul.j-dropdown-menu-player2"),
                $(".j-form-match button.j-dropdown-toggle-player2"),
                $(".j-form-match .j-selected-player2")
            );
        } else {
            org.cmhuynh.pingpong.admin.handleError(resp);
        }
    });
};

org.cmhuynh.pingpong.admin._buildPlayerOptions = function(players, ul, button, input) {
    $.each(players, function(index, element) {
        var li = $('<li/>')
            .prop('playerId', element.playerId)
            .appendTo(ul);
        $('<a/>', {
            "text": element.name,
            click: function(event) {
                var selText = $(this).text();
                $(button).html(selText+' <span class="caret"></span>');
                var selValue = $(this).parent().prop("playerId");
                $(input).val(selValue);
            }
        }).appendTo(li);
    });
};

org.cmhuynh.pingpong.admin.initButtons = function() {
    $("#signinButton").click(function() {
        org.cmhuynh.pingpong.admin.auth();
    });

    $(".navbar-form.j-form-player").submit(function (evt) {
        evt.preventDefault();
        org.cmhuynh.pingpong.admin._savePlayer();
    });

    $(".navbar-form.j-form-match").submit(function (evt) {
        evt.preventDefault();
        org.cmhuynh.pingpong.admin._saveMatch();
    });

    $('#datetimepicker1').datetimepicker();
};

org.cmhuynh.pingpong.admin._savePlayer = function() {
    var clubId = $(".j-form-player .j-selected-club").val();
    var playerId = $(".j-form-player .j-player-id").val();
    var playerName = $(".j-form-player .j-player-name").val();
    var imageUrl = $(".j-form-player .j-player-image-url").val();
    var score = $(".j-form-player .j-player-score").val();
    var lastScore = $(".j-form-player .j-player-last-score").val();
    var status = $(".j-form-player .j-player-status").is(":checked");
    var level = $(".j-form-player .j-player-level").val();

    if(!clubId || !playerId || !playerName
        || !org.cmhuynh.pingpong.admin._isValidScore(score)
        || !org.cmhuynh.pingpong.admin._isValidScore(lastScore)
        || !org.cmhuynh.pingpong.admin._isValidLevel(level)) {
        org.cmhuynh.pingpong.admin.showMessage(
            $("div.j-save-player-result"),
            "alert-success hidden",
            "alert-danger",
            "All fields except for optional * are required. Please fill in and retry."
        );
        return;
    }

    gapi.client.pingpong.admin.savePlayer({
        "clubId": clubId,
        "playerId": playerId,
        "name": playerName,
        "imageUrl": imageUrl,
        "score": score,
        "lastScore": lastScore,
        "status": status,
        "level": level
    }).execute(function(resp) {
        if(!resp.code) {
            org.cmhuynh.pingpong.admin.showMessage(
                $("div.j-save-player-result"),
                "alert-danger hidden",
                "alert-success",
                "Well done. Player " + playerName + " was saved successfully"
            );
        } else {
            console.log(resp);
            org.cmhuynh.pingpong.admin.showMessage(
                $("div.j-save-player-result"),
                "alert-success hidden",
                "alert-danger",
                "Failed to save. Please contact system administrator."
            );
        }
    });
};

org.cmhuynh.pingpong.admin._isValidScore = function(score) {
    return $.isNumeric(score)
        && Math.floor(score) == score
        && score > 0;
};

org.cmhuynh.pingpong.admin._isValidLevel = function(level) {
    return level >= "A" && level <= "E";
};

org.cmhuynh.pingpong.admin.showMessage = function(alertDiv, clazzRemoved, clazzAdded, message) {
    $(alertDiv).removeClass(clazzRemoved).addClass(clazzAdded);
    $(alertDiv).text(message);
    $(alertDiv).show();
};

org.cmhuynh.pingpong.admin._saveMatch = function() {
    var clubId = $(".j-form-match .j-selected-club").val();
    var matchName = $(".j-form-match .j-match-name").val();
    var matchDate = $(".j-form-match .j-match-date").data("DateTimePicker").date(); // as moment
    var p1Id = $(".j-form-match .j-selected-player1").val();
    var p1Set = $(".j-form-match .j-match-p1Set").val();
    var p2Set = $(".j-form-match .j-match-p2Set").val();
    var p2Id = $(".j-form-match .j-selected-player2").val();

    if(!clubId || !matchName || !matchDate || !p1Id || !p1Set || !p2Id || !p2Set) {
        org.cmhuynh.pingpong.admin.showMessage(
            $("div.j-save-match-result"),
            "alert-success hidden",
            "alert-danger",
            "All fields are required. Please fill in and retry."
        );
        return;
    }

    gapi.client.pingpong.admin.savePlayerMatch({
        "clubId": clubId,
        "matchName": matchName,
        "matchDate": matchDate.valueOf(), // in mili seconds
        "p1Id": p1Id,
        "p1Set": p1Set,
        "p2Set": p2Set,
        "p2Id": p2Id
    }).execute(function(resp) {
        if(!resp.code) {
            org.cmhuynh.pingpong.admin.showMessage(
                $("div.j-save-match-result"),
                "alert-danger hidden",
                "alert-success",
                "Well done. Match " + matchName + " was saved successfully"
            );
        } else {
            console.log(resp);
            org.cmhuynh.pingpong.admin.showMessage(
                $("div.j-save-match-result"),
                "alert-success hidden",
                "alert-danger",
                "Failed to save. Please contact system administrator."
            );
        }
    });
};

/**
 * Initializes the application.
 * @param {string} apiRoot Root of the API's path.
 */
org.cmhuynh.pingpong.admin.init = function(apiRoot) {
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
      org.cmhuynh.pingpong.admin.signin(true,
          org.cmhuynh.pingpong.admin.userAuthed);
      org.cmhuynh.pingpong.admin.initButtons();
    }
  };

  apisToLoad = 2; // must match number of calls to gapi.client.load()
  gapi.client.load('pingpong', 'v1', callback, apiRoot);
  gapi.client.load('oauth2', 'v2', callback);
};