var colors = ["grey", "green", "blue", "#800000", "#00eeee", "#E66100", "gold", "#ff00ff"];
            // grey,   green,  d-blue,  maroon,     aqua,     orange,    gold,   fuchsia
function resetColor(ctx) {
    ctx.fillStyle = 'black';
    ctx.strokeStyle = 'black';
    ctx.lineWidth = 3;
    ctx.setLineDash([]);
}

function drawLine(ctx, x_start, y_start, x_end, y_end, color) {
    ctx.beginPath();
    ctx.strokeStyle = color;
    if (color == 'black') {
        ctx.setLineDash([3, 5]);
    }

    ctx.moveTo(x_start, y_start);
    ctx.lineTo(x_end, y_end);
    ctx.stroke();
    resetColor(ctx);
}

function drawTownName(ctx, name, x, y) {
    ctx.beginPath();
    ctx.arc(x, y, 1, 0*Math.PI, 2*Math.PI);
    ctx.fillText(name, x+3, y+20);
    ctx.stroke();
}

function drawPlayers(ctx, players) {
    var x = 500;
    var y = 20;

    ctx.beginPath();

    for (var i=0; i<players.length; ++i) {
        ctx.fillStyle = player_colors[players[i]];
        ctx.fillText(players[i] + ":", x, y);
        y += 20;
    }

    resetColor(ctx);
    ctx.stroke();
}

function drawInfo(ctx, players, info) {
    var x = 500;
    var y = 20;

    ctx.beginPath();

    for (var i=0; i<players.length; ++i) {
        ctx.fillStyle = player_colors[players[i]];
        
        if (info[players[i]] != undefined) {
            ctx.fillText(players[i] + ": " + info[players[i]], x, y);
        } else {
            ctx.fillText(players[i] + ": ", x, y);
        }

        y += 40;
    }

    resetColor(ctx);
    ctx.stroke();
}

var player_pos = [];
var player_colors = [];
var players = [];
var towns = [];
var links = [];

var x_start;
var y_start;

function process(data) {
    var result = JSON.parse(data)
    var refresh = parseFloat(result.refresh);

    canvas = document.getElementById('canvas');
    ctx = canvas.getContext('2d');
    ctx.font = '20px Arial';
    ctx.lineWidth = 2;

    if (result.geo !== undefined) {
        var budget = result.budget;
        players = result.players.split(',');   
        var info = [];

        for (var i=0; i<players.length; ++i) {
            player_colors[players[i]] = colors[i];
            info[players[i]] = budget;
        }

        player_colors["None"] = "black";

        drawInfo(ctx, players, info);

        var geo = result.geo.split(';')
        var infra = result.infra.split(';')

        x_start = 10;
        var y_max = 0;
        for (var i=0; i<geo.length; ++i) {
            var elems = geo[i].split(',');

            // x,y in cartesian.
            towns[elems[0]] = {
                x : x_start + Number(elems[1]),
                y : Number(elems[2])
            };

            if (Number(elems[2]) > y_max) {
                y_max = Number(elems[2]);
            }
        }

        // Reorganize the space on the grid.
        y_start = y_max + 50;

        for (var key in towns) {
            towns[key].y = y_start - towns[key].y;
            drawTownName(ctx, key, towns[key].x, towns[key].y);
        }

        for (var i=0; i<infra.length; ++i) {
            var elems = infra[i].split(',');

            links.push(elems[0] + '_' + elems[1]);

            var townA = towns[elems[0]];
            var townB = towns[elems[1]];

            drawLine(ctx, townA.x, townA.y, townB.x, townB.y, 'black');
        }

        return refresh;
    }

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    var budget = result.budget.split(";");
    var info = [];
    for (var i=0; i<budget.length; ++i) {
        var elems = budget[i].split(",");
        info[elems[0]] = elems[1];
    }

    drawInfo(ctx, players, info);

    for (var key in towns) {
        drawTownName(ctx, key, towns[key].x, towns[key].y);
    }

    var owners = result.owners.split(";");
    for (var i=0; i<owners.length; ++i) {
        var elems = owners[i].split(",");

        var townA = towns[elems[0]];
        var townB = towns[elems[1]];

        drawLine(ctx, townA.x, townA.y, townB.x, townB.y, player_colors[elems[2]]);
    }

    return refresh;
}

var latest_version = -1;

function ajax(version, retries, timeout) {
    console.log("Version " + version);
    var xhttp = new XMLHttpRequest();
    xhttp.onload = (function() {
            var refresh = -1;
            try {
                if (xhttp.readyState != 4)
                    throw "Incomplete HTTP request: " + xhttp.readyState;
                if (xhttp.status != 200)
                    throw "Invalid HTTP status: " + xhttp.status;
                //console.log(xhttp.responseText);
                refresh = process(xhttp.responseText);
                if (latest_version < version)
                    latest_version = version;
                else refresh = -1;
            } catch (message) {
                alert(message);
            }

            console.log(refresh);
            if (refresh >= 0)
                setTimeout(function() { ajax(version + 1, 10, 100); }, refresh);
        });
    xhttp.onabort = (function() { location.reload(true); });
    xhttp.onerror = (function() { location.reload(true); });
    xhttp.ontimeout = (function() {
            if (version <= latest_version)
                console.log("AJAX timeout (version " + version + " <= " + latest_version + ")");
            else if (retries == 0)
                location.reload(true);
            else {
                console.log("AJAX timeout (version " + version + ", retries: " + retries + ")");
                ajax(version, retries - 1, timeout * 2);
            }
        });
    xhttp.open("GET", "data.txt", true);
    xhttp.responseType = "text";
    xhttp.timeout = timeout;
    xhttp.send();
}

ajax(1, 10, 100);

// process('{"refresh":0, "grp_a":"g1", "grp_b":"g2", "grp_a_round":"1,2,3,4,5", "grp_b_round":"4,5,6,7,8",' +
//    '"grp_a_skills":"4,3,2,5,6,7,3", "grp_b_skills":"2,3,5,7,5,4,3", "grp_a_dist":"1,2;4,3;6,7", "grp_b_dist":"3,4;5,1;8,7", ' +
//    '"grp_a_score":"3", "grp_b_score":"0"}');

