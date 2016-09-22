<!--<script type="text/javascript" src="${staticpath}/js/jquery-3.1.0.min.js"></script>-->
<!--<link rel="stylesheet" type="text/css" href="${staticpath}/css/bootstrap.css" />-->


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
                    <div class="row">
                        <button type="submit" name="action" value=${key}
                            <#if !(value??)> class="btn btn-secondary"
                            <#elseif value.userName==userName> class="btn btn-primary"
                            <#else> class="btn btn-secondary" disabled
                            </#if>
                        >${key},
                        <#if (value??)>"${value.userName}"
                        <#else>(free)
                        </#if>
                        </button>
                    </div>
                </#list>
            </form>
        </div>
    </div>
</div>
</script>