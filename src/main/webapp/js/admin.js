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
 * Whether or not the user is signed in.
 * @type {boolean}
 */
org.cmhuynh.pingpong.admin.userEmail = undefined;

/**
 * Loads the application UI after the user has completed auth.
 */
org.cmhuynh.pingpong.admin.userAuthed = function() {
  var request = gapi.client.oauth2.userinfo.get().execute(function(resp) {
    if (!resp.code) {
        org.cmhuynh.pingpong.admin.userEmail = resp.email;
    } else {
        org.cmhuynh.pingpong.admin.userEmail = undefined;
    }
    org.cmhuynh.pingpong.admin.initButtons();
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
    org.cmhuynh.pingpong.admin.userEmail = undefined;;
    org.cmhuynh.pingpong.admin.initButtons();
  }
};

org.cmhuynh.pingpong.admin.handleError = function(resp) {
    if(!resp.code) {
        console.log(resp.message);
    }
};

org.cmhuynh.pingpong.admin.initClubs = function() {
  if (!org.cmhuynh.pingpong.admin.userEmail) {
    return;
  }
  gapi.client.pingpong.admin.getClubsByAdmin().execute(
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
        org.cmhuynh.pingpong.admin.handleError(resp);
    }
  });
};

org.cmhuynh.pingpong.admin.initButtons = function() {
    org.cmhuynh.pingpong.admin.initClubs();

    $("#signinButton").click(function() {
        org.cmhuynh.pingpong.admin.auth();
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