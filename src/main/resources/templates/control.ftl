<script type="text/javascript" src="${staticpath}/js/jsmpg.js"></script>
<script type="text/javascript" src="${staticpath}/js/jquery-3.1.0.min.js"></script>
<script type="text/javascript" src="${staticpath}/js/reconnecting-websocket.js"></script>

<div class="panel panel-default">
  <div class="panel-body">
<div id="wrapper">
    <!-- Page Content -->
    <div id="page-content-wrapper">
        <div class="container-fluid">
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
                        <input type="text" class="form-control" id="z" style="width: 50px" value="75">
                    </div>
                    <button type="button" class="btn btn-default" id="sendXYZ">Move</button>
               </div>
            </form>

        </div>
        <div class="row">
            <div class="form-group">
                <label for="comment">Sequence:</label>
                <textarea class="form-control" rows="10" id="txtSequence">
#comment
reset
sleep 1
valveOn true
valveOn false
pumpOn true
pumpOn false
move 200 0 170
move 200 0 100
move 200 0 120
move 250 0 120
move 250 100 120
move 200 100 120
move 200 0 120
                </textarea>
            </div>
            <button type="button" class="btn btn-default" id="btnSequence">Run</button>
        </div>
    </div>
    <!-- /#page-content-wrapper -->

</div>
<!-- /#wrapper -->
</div>
</div>

<script>
    $('#btnReset').on('click', function(event) {
        event.preventDefault(); // To prevent following the link (optional)
        console.log('btnReset');
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: '${publicURL}/reset',
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
            url: '${publicURL}/valveOn',
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
            url: '${publicURL}/pumpOn',
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
            url: '${publicURL}/test1',
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
            url: '${publicURL}/move',
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
            url: '${publicURL}/sequence',
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