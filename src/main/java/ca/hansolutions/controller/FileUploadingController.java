package ca.hansolutions.controller;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.*;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Map;

@Controller
@RequestMapping(value = "/gcs")
public class FileUploadingController {

    public static final boolean SERVE_USING_BLOBSTORE_API = false;
    public static final String BUCKET_NAME = "zaomai-1332.appspot.com";
    public static final String HTTPS_STORAGE_CLOUD_GOOGLE_COM = "https://storage.cloud.google.com/";

    /**
     * This is where backoff parameters are configured. Here it is aggressively retrying with
     * backoff, up to 10 times but taking no more that 15 seconds total to do so.
     */
    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)
            .build());

    /**
     * Used below to determine the size of chucks to read in. Should be > 1kb and < 10MB
     */
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    @RequestMapping(value = "/images", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String getImage(Map<String, Object> map, HttpServletRequest req, HttpServletResponse resp) throws IOException {

        GcsFilename fileName = getFileName(req);
        if (SERVE_USING_BLOBSTORE_API) {
            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
            BlobKey blobKey = blobstoreService.createGsBlobKey(
                    "/gs/" + fileName.getBucketName() + "/" + fileName.getObjectName());
            blobstoreService.serve(blobKey, resp);
        } else {
            GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, BUFFER_SIZE);
            copy(Channels.newInputStream(readChannel), resp.getOutputStream());
        }

        return "UserTest";

    }

    @RequestMapping(value = "/images/", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadProfileImage(Map<String, Object> map, HttpServletRequest req) {

        //for acl, we could use "public-read" or "private-project",
//        GcsFileOptions instance = new GcsFileOptions.Builder().mimeType("image/jpeg").acl("public-read").build();
        GcsFileOptions fileOptions = new GcsFileOptions.Builder().mimeType("image/jpeg").build();

        String fileNameString = null;
        try {
            FileItemIterator fileItemIterator = new ServletFileUpload().getItemIterator(req);
            while (fileItemIterator.hasNext()) {
                FileItemStream fileItemStream = fileItemIterator.next();
                fileNameString = fileItemStream.getName();
                GcsFilename gcsFilename = new GcsFilename(BUCKET_NAME, fileNameString);
                GcsOutputChannel outputChannel = gcsService.createOrReplace(gcsFilename, fileOptions);
                InputStream inputStream = fileItemStream.openStream();
                copy(inputStream, Channels.newOutputStream(outputChannel));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder imageUrl = new StringBuilder(HTTPS_STORAGE_CLOUD_GOOGLE_COM).append(BUCKET_NAME).append("/").append(fileNameString);
        map.put("imageUrl", imageUrl.toString());

        return "mainpage";
    }

    private GcsFilename getFileName(HttpServletRequest req) {
        String[] splits = req.getRequestURI().split("/", 4);
        if (!splits[0].equals("") || !splits[1].equals("gcs")) {
            throw new IllegalArgumentException("The URL is not formed as expected. " +
                    "Expecting /gcs/<bucket>/<object>");
        }
        return new GcsFilename(splits[2], splits[3]);
    }

    /**
     * Transfer the data from the inputStream to the outputStream. Then close both streams.
     */
    private void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }

}
