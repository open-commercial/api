package sic.service;

import java.io.InputStream;
import java.util.List;

public interface IAmazonService {     
    
    List<String> getBucketResources();       
    
    String saveFileIntoS3Bucket(String key, InputStream file, String contentType);
    
    String getFileFromS3Bucket(String key);
    
    void deleteFileFromS3Bucket(String key);
}
