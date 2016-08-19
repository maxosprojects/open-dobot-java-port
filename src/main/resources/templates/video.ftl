<link rel="stylesheet" href="${sidebarcss}">


<!-- Sidebar -->
    <div id="sidebar-wrapper">
        <ul class="sidebar-nav">
            <li class="sidebar-brand">
                <a href="#">
                    Start Bootstrap
                </a>
            </li>
            <li>
                <a href="#">Dashboard</a>
            </li>
            <li>
                <a href="#">Shortcuts</a>
            </li>
            <li>
                <a href="#">Overview</a>
            </li>
            <li>
                <a href="#">Events</a>
            </li>
            <li>
                <a href="#">About</a>
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
                    <script type="text/javascript" src="${jsmpgpath}"></script>
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
        </div>
    </div>
    <!-- /#page-content-wrapper -->

<!-- /#wrapper -->

<!-- Menu Toggle Script -->
<script>
    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });
</script>
