package mx.com.itbrain.poc.alfresco

import org.apache.chemistry.opencmis.commons.SessionParameter
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.apache.chemistry.opencmis.commons.enums.BindingType
import org.apache.chemistry.opencmis.client.api.Folder
import org.apache.chemistry.opencmis.client.api.CmisObject
import org.apache.chemistry.opencmis.client.api.ItemIterable
import org.apache.chemistry.opencmis.client.api.Document
import javax.servlet.ServletOutputStream
import org.springframework.web.multipart.MultipartFile
import org.apache.chemistry.opencmis.commons.data.ContentStream
import org.apache.chemistry.opencmis.commons.PropertyIds
import org.apache.chemistry.opencmis.commons.enums.VersioningState

class AdministradorContenidoController {
    def cmisSessionFactory

    def alfrescoParams = [
        (SessionParameter.USER): ConfigurationHolder.config.cmis.user,
        (SessionParameter.PASSWORD): ConfigurationHolder.config.cmis.password,
        (SessionParameter.ATOMPUB_URL): ConfigurationHolder.config.cmis.url,
        (SessionParameter.BINDING_TYPE): BindingType.ATOMPUB.value(),
        (SessionParameter.REPOSITORY_ID): ConfigurationHolder.config.cmis.repositoryId,
        (SessionParameter.LOCALE_ISO3166_COUNTRY): "mx",
        (SessionParameter.LOCALE_ISO639_LANGUAGE): "es",
        (SessionParameter.LOCALE_VARIANT): ""]

    def index() {
        // Obtener el listado de contenido del folder raiz del repositorio
        def session = cmisSessionFactory.createSession(alfrescoParams)
        def rootFolder = (Folder) session.getObjectByPath("/");
        ItemIterable<CmisObject> children = rootFolder.getChildren()
        def list = [] as List
        for (CmisObject o : children) {
            list.add(o)
        }
        [currentPath: rootFolder.getPath(), currentFolder: list]
    }

    def viewFolder() {
        def session = cmisSessionFactory.createSession(alfrescoParams)
        log.debug("params.path = [${params.path}]")
        log.debug("params.link = [${params.link}]")
        def fpath = params.path + (params.path == "/" ? "" : "/") + (params.link ?: "")
        log.debug("Folder Path = [${fpath}]")
        def folder = (Folder) session.getObjectByPath(fpath)
        ItemIterable<CmisObject> children = folder.getChildren()
        def list = [] as List
        for (CmisObject o : children) {
            list.add(o)
        }
        render(view: "index", model: [currentPath: folder.getPath(), currentFolder: list])
    }

    def downloadFile() {
        def session = cmisSessionFactory.createSession(alfrescoParams)
        def doc = (Document) session.getObjectByPath(params.path + (params.path == "/" ? "" : "/") + params.link)
        response.setContentLength((int) doc.getContentStreamLength())
        response.setHeader("Content-disposition", "attachment; filename=\"" + doc.getName() + "\"")
        ServletOutputStream outputStream = null
        DataInputStream docStream = null
        int length = 0
        outputStream = response.getOutputStream()
        docStream = new DataInputStream(doc.getContentStream().getStream())
        byte[] bbuf = new byte[1024]
        while ((docStream != null) && ((length = docStream.read(bbuf)) != -1)) {
            outputStream.write(bbuf, 0, length)
        }
        outputStream?.flush()
        docStream?.close()
        outputStream?.close()
        // Debido a un bug de grails se asigna el content type despues de haber escrito al outputStream del response
        response.setContentType(doc.getContentStreamMimeType())
        null
    }

    def deleteFile() {
        def session = cmisSessionFactory.createSession(alfrescoParams)
        def doc = (Document) session.getObjectByPath(params.path + (params.path == "/" ? "" : "/") + params.link)
        // Borra la actual version y todas las anteriores
        doc.deleteAllVersions();
        redirect(action: 'viewFolder', params: [path: params.path])
    }

    def uploadFile() {
        def session = cmisSessionFactory.createSession(alfrescoParams)
        MultipartFile f = request.getFile("archivo")
        if (f.empty) {
            redirect(action: 'viewFolder', params: [path: params.path])
        }
        def buf = f.getBytes()
        ByteArrayInputStream input = new ByteArrayInputStream(buf);
        ContentStream contentStream = session.getObjectFactory().createContentStream(f.originalFilename,
            buf.length, f.contentType, input);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, f.originalFilename);
        def folder = (Folder) session.getObjectByPath(params.path)
        folder.createDocument(properties, contentStream, VersioningState.MAJOR);
        redirect(action: 'viewFolder', params: [path: params.path])
    }

    def updateFile() {
        def session = cmisSessionFactory.createSession(alfrescoParams)
        MultipartFile f = request.getFile("archivo")
        if (f.empty) {
            redirect(action: 'viewFolder', params: [path: params.path])
        }
        def buf = f.getBytes()
        ByteArrayInputStream input = new ByteArrayInputStream(buf);
        ContentStream contentStream = session.getObjectFactory().createContentStream(f.originalFilename,
            buf.length, f.contentType, input);
        def doc = (Document) session.getObjectByPath(params.path + (params.path == "/" ? "" : "/") + params.link)
        doc.setContentStream(contentStream, true);
        redirect(action: 'viewFolder', params: [path: params.path])
    }
}
