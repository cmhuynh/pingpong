var org = org || {};

org.cmhuynh = org.cmhuynh || {};

org.cmhuynh.pingpong = org.cmhuynh.pingpong || {};

/**
 * Client ID of the application (from the APIs Console).
 * @type {string}
 */
org.cmhuynh.pingpong.CLIENT_ID =
    '594724891312-h9v8svhh468ei3uquo0lha5bamq2op3e.apps.googleusercontent.com';

/**
 * Scopes used by the application.
 * @type {string}
 */
org.cmhuynh.pingpong.SCOPES =
    'https://www.googleapis.com/auth/userinfo.email';

org.cmhuynh.pingpong.handleError = function(resp) {
    if(!resp.code) {
        console.log(resp.message);
    }
};

org.cmhuynh.pingpong.initClubs = function() {
  gapi.client.pingpong.pingpongs.getClubs().execute(
  function(resp) {
    if (!resp.code) {
        var clubs = resp.items || [];
        clubs = clubs.filter(function(value) {
            return value.status;
        });
        clubs.sort(function(c1, c2) {
            return c1.name.toLowerCase < c2.name.toLowerCase;
        });

      var ul = $('ul.j-dropdown-menu-club');
      $.each(resp.items, function(index, element) {
        var li = $('<li/>')
            .prop('clubId', element.clubId)
            .appendTo(ul);
        $('<a/>', {
            "text": element.name,
            click: function(event) {
                var selText = $(this).text();
                $('button.j-dropdown-toggle-club').html(selText+' <span class="caret"></span>');
                var selValue = $(this).parent().prop("clubId");
                $("#selClub").prop("value", selValue);
            }
        }).appendTo(li);
      });
    } else {
        org.cmhuynh.pingpong.handleError(resp);
    }
  });
};

org.cmhuynh.pingpong.loadPlayers = function(clubId, year) {
  gapi.client.pingpong.pingpongs.getPlayers({"clubId": clubId}).execute(
    function(resp) {
        if (!resp.code) {
            var players = resp.items || [];
            players = players.filter(function(value) {
                return value.status;
            });
            // sort by level A, B, C and then by score descending
            players.sort(function(p1, p2) {
                var l1 = p1.level || "A";
                var l2 = p2.level || "A";
                if (l1 != l2) {
                    return l1 - l2;
                } else {
                    return p2.score - p1.score;
                }
            });

            org.cmhuynh.pingpong.renderPlayers(clubId, year, players);
        } else {
            org.cmhuynh.pingpong.handleError(resp);
        }
    });
};

org.cmhuynh.pingpong.renderPlayers = function(clubId, year, players) {
    // reset the player table
    $.each($('div.j-player-list .player-detail'), function(index, element) {
        $(element).remove();
    });

    // build up players list
    var playersDiv = $('div.j-player-list');
    $.each(players, function(index, element) {
        var row = $( "<div/>", {
            "class": "row player-detail"
        }).appendTo( playersDiv );
        var content = $( "<div/>", {
            "class": "container"
        }).appendTo( row );
        var playerRow = $( "<div/>", {
            "class": "row"
        }).appendTo( content );
        var matchRow = $( "<div/>", {
            "class": "row"
        }).appendTo( content );

        // player details
        $( "<div/>", {
            "class": "col-sm-2",
            "text": element.name
        }).appendTo( playerRow );

        if (element.imageUrl) {
            var imageCol = $( "<div/>", {
                "class": "col-sm-2"
            }).appendTo( playerRow );
            $("<img>", {
                "src": element.imageUrl,
                "alt": element.name
            }).appendTo( imageCol );
        } else {
            $( "<div/>", {
                "class": "col-sm-2"
            }).appendTo( playerRow );
        }

        $( "<div/>", {
            "class": "col-sm-2",
            "text": element.score
        }).appendTo( playerRow );

        $( "<div/>", {
            "class": "col-sm-2",
            "text": element.lastScore
        }).appendTo( playerRow );

        if(element.score > element.lastScore) {
            var starCol = $( "<div/>", {
                "class": "col-sm-2"
            }).appendTo( playerRow );
            $("<img>", {
                "src": "/image/star.jpg",
                "width": "16px",
                "height": "16px",
                "alt": "Rising star"
            }).appendTo( starCol );
        } else {
            $( "<div/>", {
                "class": "col-sm-2"
            }).appendTo( playerRow );
        }

        var matchCol = $( "<div/>", {
            "class": "col-sm-2"
        }).appendTo( playerRow );
        $("<a>", {
            "class": "btn btn-default pull-right",
            "href": "#",
            "role": "button",
            "text": "Matches >>",
            click: function() {
                org.cmhuynh.pingpong.loadMatches($(this), matchRow, clubId, year, element.playerId);
            }
        }).appendTo(matchCol);
    });
};

org.cmhuynh.pingpong.loadMatches = function(button, contentDiv, clubId, year, playerId) {
    if ($(contentDiv).hasClass("j-match-load")) {
        if (contentDiv.is(':visible')) {
            contentDiv.hide();
            $(button).text("Matches >>");
        } else {
            contentDiv.show();
            $(button).text("Matches <<");
        }
        return;
    }

    gapi.client.pingpong.pingpongs.getPlayerMatches({"playerId": playerId, "clubId": clubId, "year": year}).execute(
        function(resp) {
            if (!resp.code) {
                // sort by latest match dates
                var matches = resp.items = resp.items || [];
                matches.sort(function(m1, m2) {
                    return m2.matchDate - m1.matchDate;
                });

                org.cmhuynh.pingpong.renderMatches(contentDiv, matches);

                $(button).text("Matches <<");
            } else {
                org.cmhuynh.pingpong.handleError(resp);
            }
        });
};

org.cmhuynh.pingpong.renderMatches = function(matchRow, matches) {
    $.each($(matchRow).children(), function(index, element) {
        element.remove();
    });

    var col = $( "<div/>", {
        "class": "col-sm-12"
    }).appendTo( matchRow );
    var container = $( "<div/>", {
        "class": "container"
    }).appendTo( col );

    $.each(matches, function(index, element) {
        var row = $( "<div/>", {
            "class": "row j-match-detail"
        }).appendTo( container );
        $( "<div/>", {
            "class": "col-sm-2",
            "text": org.cmhuynh.pingpong.dateToString(element.matchDate)
        }).appendTo( row );
        $( "<div/>", {
            "class": "col-sm-3",
            "text": element.matchName
        }).appendTo( row );
        var details = element.p1Name + " | " + element.p1Score + " | " + element.p1Gain + " | " + element.p1Set + " - " + element.p2Set + " | " + element.p2Gain + " | " + element.p2Score + " | " + element.p2Name;
        $( "<div/>", {
            "class": "col-sm-7",
            "text": details
        }).appendTo( row );
    });

    // flag that matches were load
    $(matchRow).toggleClass("j-match-load");
};

org.cmhuynh.pingpong.dateToString = function(dateStr) {
    try {
        var date = Number(dateStr);
        var isoDate = new Date(date).toISOString();
        return isoDate.slice(0, 10) + " " + isoDate.slice(11, 16);
    } catch (e) {
        return "";
    }
};

org.cmhuynh.pingpong.initButtons = function() {
    org.cmhuynh.pingpong.initClubs();

    $(".navbar-form input.j-year-input").val(new Date().getFullYear());

    $(".navbar-form").submit(function (evt) {
        evt.preventDefault();
        var clubId = $("#selClub").val();
        var year = $(".navbar-form input.j-year-input").val();
        org.cmhuynh.pingpong.loadPlayers(clubId, year);
    });
};

/**
 * Initializes the application.
 * @param {string} apiRoot Root of the API's path.
 */
org.cmhuynh.pingpong.init = function(apiRoot) {
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
      org.cmhuynh.pingpong.initButtons();
    }
  };

  apisToLoad = 1; // must match number of calls to gapi.client.load()
  gapi.client.load('pingpong', 'v1', callback, apiRoot);
};
