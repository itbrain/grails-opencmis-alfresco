<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Propiedades</title>
</head>
<body>
    <h2>Properties:</h2>
    <table width="50%" class="table table-bordered table-striped">
        <tbody>
            <g:each in="${properties}">
                <tr>
                    <td><span class="label label-info">${it.getDefinition().getDisplayName()}</span></td>
                    <td>${it.getValuesAsString()}</td>
                </tr>
            </g:each>
        </tbody>
    </table>
</body>
</html>