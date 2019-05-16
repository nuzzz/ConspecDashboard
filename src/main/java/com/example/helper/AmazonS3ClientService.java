package com.example.helper;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.example.extensions.FileFormatException;

public interface AmazonS3ClientService {

	void uploadFileToS3Bucket(MultipartFile multipartFile, boolean enablePublicReadAccess) throws FileFormatException;

	void deleteFileFromS3Bucket(String fileName);
	
	void listAllFiles();

	void listAllProjects();

	void saveJsonTo(String location, String content, boolean enablePublicReadAccess);

	void putFile(String location, File newFile);

	String openFileAndGetJsonString(String filepath) throws IOException;

	String getJsonStringFromS3(String taskListLocation);
}