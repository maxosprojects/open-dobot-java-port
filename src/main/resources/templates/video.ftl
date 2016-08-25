<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, shrink-to-fit=no, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>OMiLAB OMiRob</title>

    <!-- Bootstrap Core CSS -->
    <link href="static/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="static/css/simple-sidebar.css" rel="stylesheet">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>

<body>

<div id="wrapper">

    <!-- Sidebar -->
    <div id="sidebar-wrapper">
        <ul class="sidebar-nav">
            <li class="sidebar-brand">
                <a href="#">
                    OMiRob
                </a>
            </li>
            <li>
                <a href="#">Live</a>
            </li>
            <li>
                <a href="#">Services</a>
            </li>
            <li>
                <a href="#">Contact</a>
            </li>
        </ul>
    </div>
    <!-- /#sidebar-wrapper -->

    <!-- Page Content -->
    <div id="page-content-wrapper">
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-12">
                    <canvas id="videoCanvas" width="640" height="480">
                        <p>
                            Please use a browser that supports the Canvas Element, like
                            <a href="http://www.google.com/chrome">Chrome</a>,
                            <a href="http://www.mozilla.com/firefox/">Firefox</a>,
                            <a href="http://www.apple.com/safari/">Safari</a> or Internet Explorer 10
                        </p>
                    </canvas>
                    <script type="text/javascript" src="/static/js/jsmpg.js"></script>
                    <script type="text/javascript">
                        // Setup the WebSocket connection and start the player
                        function url(s) {
                            var l = window.location;
                            return ((l.protocol === "https:") ? "wss://" : "ws://") + l.host + l.pathname + s;
                        }
                        var client = new WebSocket( url('stream/output') );

                        var canvas = document.getElementById('videoCanvas');
                        var player = new jsmpeg(client, {canvas:canvas});
                    </script>
                </div>
            </div>
            <div class="row">
                <button type="button" class="btn btn-primary btn-warning" id="btnReset">Reset</button>
                <button type="button" class="btn btn-primary btn-warning" id="btnTest">Test</button>
            </div>
            <div class="row">
                <label class="checkbox-inline"><input type="checkbox" value="" id="cbValve">Valve</label>
                <label class="checkbox-inline"><input type="checkbox" value="" id="cbPump">Pump</label>
            </div>
            <form class="form-inline">
                <div class="row">
                    <div class="form-group  ">
                        <label for="x">XYZ</label>
                        <input type="text" class="form-control" id="x" style="width: 50px" value="200">
                        <input type="text" class="form-control" id="y" style="width: 50px" value="0">
                        <input type="text" class="form-control" id="z" style="width: 50px" value="70">
                    </div>
                    <button type="button" class="btn btn-default" id="sendXYZ">Move</button>
               </div>
            </form>

        </div>
        <div class="row">
            <div class="form-group">
                <label for="comment">Sequence:</label>
                <textarea class="form-control" rows="10" id="txtSequence"></textarea>
            </div>
            <button type="button" class="btn btn-default" id="btnSequence">Run</button>
        </div>
    </div>
    <!-- /#page-content-wrapper -->

</div>
<!-- /#wrapper -->

<!-- jQuery -->
<script src="static/js/jquery-3.1.0.min.js"></script>

<!-- Bootstrap Core JavaScript -->
<script src="static/js/bootstrap.min.js"></script>

<!-- Menu Toggle Script -->
<script>
    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $('#btnReset').on('click', function(event) {
        event.preventDefault(); // To prevent following the link (optional)
        console.log('btnReset');
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: '/reset',
            dataType: "json",
            //data: formToJSON(),
            success: function(data, textStatus, jqXHR){
                console.log('btnReset success');
            },
            error: function(jqXHR, textStatus, errorThrown){
                console.log('btnReset error'+errorThrown);
            }
        });
    });

    $('#cbValve').change(function(event) {
        event.preventDefault(); // To prevent following the link (optional)
        console.log('cbValve');
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: '/valveOn',
            dataType: "json",
            data: JSON.stringify(this.checked),
            success: function(data, textStatus, jqXHR){
                console.log('cbValve success');
            },
            error: function(jqXHR, textStatus, errorThrown){
                console.log('cbValve error'+errorThrown);
            }
        });
    });

    $('#cbPump').change(function(event) {
        event.preventDefault(); // To prevent following the link (optional)
        console.log('cbPump');
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: '/pumpOn',
            dataType: "json",
            data: JSON.stringify(this.checked),
            success: function(data, textStatus, jqXHR){
                console.log('cbPump success');
            },
            error: function(jqXHR, textStatus, errorThrown){
                console.log('cbPump error'+errorThrown);
            }
        });
    });

    $('#btnTest').on('click', function(event) {
        event.preventDefault(); // To prevent following the link (optional)
        console.log('btnTest');
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: '/test1',
            dataType: "json",
            //data: formToJSON(),
            success: function(data, textStatus, jqXHR){
                console.log('btnTest success');
            },
            error: function(jqXHR, textStatus, errorThrown){
                console.log('btnTest error');
            }
        });
    });
    $('#sendXYZ').on('click', function(event) {
        event.preventDefault(); // To prevent following the link (optional)
        console.log('btnMove');
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: '/move',
            dataType: "json",
            data: formToJSON(),
            success: function(data, textStatus, jqXHR){
                console.log('btnMove success');
            },
            error: function(jqXHR, textStatus, errorThrown){
                console.log('btnMove error');
            }
        });
    });

    $('#btnSequence').on('click', function(event) {
        event.preventDefault(); // To prevent following the link (optional)
        console.log('btnSequence');
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: '/sequence',
            dataType: "json",
            data: JSON.stringify($('#txtSequence').val()),
            success: function(data, textStatus, jqXHR){
                console.log('btnSequence success');
            },
            error: function(jqXHR, textStatus, errorThrown){
                console.log('btnSequence error');
            }
        });
    });

    function formToJSON() {
        return JSON.stringify({
            "x": $('#x').val(),
            "y": $('#y').val(),
            "z": $('#z').val(),
        });
    }
</script>

</body>

</html>
