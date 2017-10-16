package sic.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import org.springframework.web.multipart.MultipartFile;

public class BASE64DecodedMultipartFile implements MultipartFile {

    private final byte[] imgContent;
    private final String name;

    public BASE64DecodedMultipartFile(byte[] imgContent, String name) {
        this.imgContent = imgContent;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.name;
    }

    @Override
    public String getContentType() {
        try {
            return URLConnection.guessContentTypeFromStream(this.getInputStream());
        } catch (IOException ex) {            
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return imgContent == null || imgContent.length == 0;
    }

    @Override
    public long getSize() {
        return imgContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return imgContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        new FileOutputStream(dest).write(imgContent);
    }
}
