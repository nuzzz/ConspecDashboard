package com.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.extensions.FileFormatException;
import com.example.helper.AmazonS3ClientService;
import com.google.common.io.Files;

@Component
public class WebApplicationAmazonS3ClientService implements AmazonS3ClientService {
	private String awsS3ConspecBucket;
	private AmazonS3 amazonS3;
	private static final Logger logger = LoggerFactory.getLogger(WebApplicationAmazonS3ClientService.class);

	@Autowired
	public WebApplicationAmazonS3ClientService(Region awsRegion, AWSCredentialsProvider awsCredentialsProvider,
			String awsS3ConspecBucket) {
		this.amazonS3 = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider)
				.withRegion(awsRegion.getName()).build();
		this.awsS3ConspecBucket = awsS3ConspecBucket;
	}

	@Async
	public void saveJsonTo(String content, String location, boolean enablePublicReadAccess) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(this.awsS3ConspecBucket, location, content);

		if (enablePublicReadAccess) {
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
		}

		this.amazonS3.putObject(putObjectRequest);
	}
	
	
	@Async
	public void listAllProjects() {
		ObjectListing objectListing = amazonS3.listObjects(awsS3ConspecBucket);
		for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
			System.out.println(os.getKey());
		}
	}

	public List<String> getObjectslistFromFolder(String bucketName, String folderKey) {

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
				.withPrefix(folderKey + "/");

		List<String> keys = new ArrayList<>();

		ObjectListing objects = amazonS3.listObjects(listObjectsRequest);
		for (;;) {
			List<S3ObjectSummary> summaries = objects.getObjectSummaries();
			if (summaries.size() < 1) {
				break;
			}
			summaries.forEach(s -> keys.add(s.getKey()));
			objects = amazonS3.listNextBatchOfObjects(objects);
		}

		return keys;
	}

	@Async
	public void uploadFileToS3Bucket(MultipartFile multipartFile, boolean enablePublicReadAccess)
			throws FileFormatException {
		String fileName = multipartFile.getOriginalFilename();
		String fileExtension = Files.getFileExtension(fileName);
		if (!fileExtension.equals("mpp")) {
			throw new FileFormatException("Failed to upload, file not mpp");
		} else {
			try {
				// creating the file in the server (temporarily)
				String folder = "Projects/";
				String finalFileName = folder + fileName;
				File file = new File(fileName);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(multipartFile.getBytes());
				fos.close();

				PutObjectRequest putObjectRequest = new PutObjectRequest(this.awsS3ConspecBucket, finalFileName, file);

				if (enablePublicReadAccess) {
					putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
				}

				this.amazonS3.putObject(putObjectRequest);
				// removing the file created in the server
				file.delete();
			} catch (IOException | AmazonServiceException ex) {
				logger.error("error [" + ex.getMessage() + "] occurred while uploading [" + fileName + "] ");
			}
		}
	}

	@Async
	public void deleteFileFromS3Bucket(String fileName) {
		try {
			amazonS3.deleteObject(new DeleteObjectRequest(awsS3ConspecBucket, fileName));
		} catch (AmazonServiceException ex) {
			logger.error("error [" + ex.getMessage() + "] occurred while removing [" + fileName + "] ");
		}
	}

	@Override
	public void listAllFiles() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putFile(String location, File file) {
		this.amazonS3.putObject(awsS3ConspecBucket, location, file);
	}

	@Override
	public String openFileAndGetJsonString(String filepath) throws IOException {
		S3Object s3object = amazonS3.getObject(awsS3ConspecBucket, filepath);
		S3ObjectInputStream inputStream = s3object.getObjectContent();
		String jsonString = "";
		try {
			jsonString = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(s3object!=null){
				s3object.close();
			}
		}
		return jsonString;
	}
}