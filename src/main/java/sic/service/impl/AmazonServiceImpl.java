package sic.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sic.service.IAmazonService;
import sic.service.ServiceException;

@Service
public class AmazonServiceImpl implements IAmazonService {

    @Value("${AWS_ACCESS_KEY_ID}")
    private String accessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String accessKeySecret;

    @Value("${BUCKET_NAME}")
    private String bucketName;

    @Value("${AWS_DEFAULT_REGION}")
    private String region;

    private Credentials sessionCredentials;

    private final String amazonS3url = "https://s3.amazonaws.com/";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private AmazonS3 getAmazonS3Client() {
        BasicSessionCredentials basicSessionCredentials = this.getBasicSessionCredentials();
        return AmazonS3ClientBuilder.standard()
                .withRegion(this.region)
                .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                .build();
    }

    private BasicSessionCredentials getBasicSessionCredentials() {
        if (sessionCredentials == null || sessionCredentials.getExpiration().before(new Date())) {
            sessionCredentials = this.getSessionCredentials();
        }
        return new BasicSessionCredentials(sessionCredentials.getAccessKeyId(),
                sessionCredentials.getSecretAccessKey(),
                sessionCredentials.getSessionToken());
    }

    private Credentials getSessionCredentials() {
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKeyId, accessKeySecret);
        AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard()
                .withRegion(this.region)
                .withCredentials(new AWSStaticCredentialsProvider(creds)).build();
        GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest().withDurationSeconds(43200);
        sessionCredentials = sts.getSessionToken(getSessionTokenRequest).getCredentials();
        return sessionCredentials;
    }

    @Override
    public String saveFileIntoS3Bucket(String key, InputStream file, String contentType) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        try {
            this.getAmazonS3Client()
                    .putObject(new PutObjectRequest(bucketName, key, file, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (SdkClientException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_acceso_S3_error"), ex);
        }
        return amazonS3url + bucketName + "/" + key;
    }

    @Override
    public String getFileFromS3Bucket(String key) {
        try {
            return amazonS3url + bucketName + "/" + key;
        } catch (SdkClientException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_acceso_S3_error"), ex);
        }
    }

    @Override
    public void deleteFileFromS3Bucket(String key) {
        try {
            this.getAmazonS3Client().deleteObject(bucketName, key);
        } catch (SdkClientException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_acceso_S3_error"), ex);
        }
    }

    @Override
    public List<String> getBucketResources() {
        try {
            ObjectListing objectListing = this.getAmazonS3Client()
                    .listObjects(new ListObjectsRequest().withBucketName(bucketName));
            return objectListing.getObjectSummaries()
                    .stream()
                    .map(a -> amazonS3url + a.getBucketName() + "/" + a.getKey())                    
                    .collect(Collectors.toList());
        } catch (SdkClientException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_acceso_S3_error"), ex);
        }
    }
}
