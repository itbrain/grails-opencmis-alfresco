<%@ page import="org.apache.chemistry.opencmis.client.api.CmisObjectProperties; org.apache.chemistry.opencmis.client.api.Folder; org.apache.chemistry.opencmis.client.api.Document; org.apache.chemistry.opencmis.client.api.ObjectType" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Listado</title>
</head>

<body>
    <div class="navbar">
        <div class="navbar-inner">
            <div class="container">
                <h2>Path Actual: ${currentPath}</h2>
            </div>
        </div>
    </div>
    <table class="table table-bordered table-striped">
        <thead>
            <tr>
                <td style="width: 10px;"></td>
                <td>Name</td>
                <td>Properties</td>
                <td></td>
            </tr>
        </thead>
        <tbody>
        <g:set var="count" value="${1}" />
        <g:each in="${currentFolder}">
            <g:set var="isFolder" value="${it.getType().getBaseTypeId().value() == ObjectType.FOLDER_BASETYPE_ID}" />
            <tr>
                <td style="width: 10px;">
                    <g:if test="${isFolder}">
                        <i class="icon-folder-open"></i>
                    </g:if>
                    <g:else>
                        <i class="icon-file"></i>
                    </g:else>
                </td>
                <td>
                    <g:if test="${isFolder}">
                        <g:link
                                action="viewFolder"
                                params="${[path: currentPath, link: it.name]}">
                            ${it.name}
                        </g:link>
                    </g:if>
                    <g:else>
                        ${it.name}
                    </g:else>
                </td>
                <td>
                    <g:if test="${!isFolder}">
                        <span class="label label-inverse">Current Version: ${((Document) it).getVersionLabel()}</span>
                    </g:if>
                    <span class="label label-info">Created by: ${((CmisObjectProperties) it).getCreatedBy()}</span>
                    <span class="label label-info">Last Modified by: ${((CmisObjectProperties) it).getLastModifiedBy()}</span>
                    <span class="label label-info">Creation Date: ${((CmisObjectProperties) it).getCreationDate().getTime().format("dd/MM/yyyy")}</span>
                    <span class="label label-info">Last Modification Date: ${((CmisObjectProperties) it).getLastModificationDate().getTime().format("dd/MM/yyyy")}</span>
                </td>
                <td>
                    <g:if test="${!isFolder}">
                        <g:form name="downloadFileForm_${count}" controller="administradorContenido" action="downloadFile" method="GET">
                            <g:hiddenField name="path" value="${currentPath}"></g:hiddenField>
                            <g:hiddenField name="link" value="${it.name}"></g:hiddenField>
                        </g:form>
                        <g:form name="deleteFileForm_${count}" controller="administradorContenido" action="deleteFile" method="DELETE">
                            <g:hiddenField name="path" value="${currentPath}"></g:hiddenField>
                            <g:hiddenField name="link" value="${it.name}"></g:hiddenField>
                        </g:form>
                        <div class="btn-group">
                            <button id="downloadButton_${count}" class="btn-mini"><i class="icon-download"></i></button>
                            <button id="uploadButton_${count}" class="btn-mini"><i class="icon-upload"></i></button>
                            <button id="removeButton_${count}" class="btn-mini"><i class="icon-remove"></i></button>
                        </div>
                        <div id="updateFileDiv_${count}" style="display: none;">
                            <g:uploadForm name="updateFileForm_${count}" controller="administradorContenido" action="updateFile">
                                <g:hiddenField name="path" value="${currentPath}"></g:hiddenField>
                                <g:hiddenField name="link" value="${it.name}"></g:hiddenField>
                                <input type="file" name="archivo">
                                <input type="submit" value="Upload">
                            </g:uploadForm>
                        </div>
                        <script type="text/javascript">
                            $("#downloadButton_${count}").click(function() {
                                $('#downloadFileForm_${count}').submit();
                            });
                            $("#uploadButton_${count}").click(function() {
                                $('#updateFileDiv_${count}').dialog('open');
                            });
                            $("#removeButton_${count}").click(function() {
                                $('#deleteFileForm_${count}').submit();
                            });
                            $("#updateFileDiv_${count}").dialog({
                                bgiframe: false,
                                height: 250,
                                width: 400,
                                autoOpen: false,
                                modal: true,
                                closeOnEscape: true,
                                draggable: false
                            });
                        </script>
                    </g:if>
                </td>
            </tr>
            <g:set var="count" value="${count + 1}" />
        </g:each>
        </tbody>
    </table>
    <div>
        <h2>Upload File</h2>
        <g:uploadForm controller="administradorContenido" action="uploadFile">
            <g:hiddenField name="path" value="${currentPath}"></g:hiddenField>
            <input type="file" name="archivo">
            <input type="submit" value="Upload">
        </g:uploadForm>
    </div>
</body>
</html>