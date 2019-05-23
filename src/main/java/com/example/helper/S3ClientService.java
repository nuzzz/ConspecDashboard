package com.example.helper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.extensions.FileFormatException;
import com.example.model.TodoistProject;

public interface S3ClientService {

	void uploadFileToS3Bucket(MultipartFile multipartFile, boolean enablePublicReadAccess) throws FileFormatException;

	void deleteFileFromS3Bucket(String fileName);
	
	void listAllFiles();

	List<String> listAllProjects();

	void saveJsonTo(String location, String content, boolean enablePublicReadAccess);

	void putFile(String location, File newFile);

	String openFileAndGetJsonString(String filepath) throws IOException;

	String getJsonStringFromS3(String taskListLocation);

	File getFileFromS3(String s3_file_location, String file_name);
}