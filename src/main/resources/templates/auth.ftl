<!--<script type="text/javascript" src="${staticpath}/js/jquery-3.1.0.min.js"></script>-->
<!--<link rel="stylesheet" type="text/css" href="${staticpath}/css/bootstrap.css" />-->
<style>
.mybutton {
    width: 140px !important;
    text-align: left;
    margin-bottom:2px;
}
</style>
<div class="panel panel-default">
  <div class="panel-body">
<div id="wrapper">
    <!-- Page Content -->
    <div id="page-content-wrapper">
        <div class="container-fluid">
        <pre>
tokens: "${authToken}"
        </pre>
            <#if (method!"get")=="post">
            <script>window.location = window.location.href;</script>
            </#if>
            <form method="post">
                <#list slots as key, value>
                    <button type="submit" name="action" value=${key}
                        <#if !(value??)> class="btn btn-secondary mybutton"
                        <#elseif value.userName==userName> class="btn btn-primary mybutton"
                        <#else> class="btn btn-secondary mybutton" disabled
                        </#if>
                    >${(key/2)?floor?string["00"]}:${(key%2*30)?string["00"]}
                    <#if (value??)>"${value.userName}"
                    <#else>(free)
                    </#if>
                    </button>
                    <#if (key?index)%4==3>
                    <br/>
                    </#if>
                </#list>
            </form>
        </div>
    </div>
</div>
</script>