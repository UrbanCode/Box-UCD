/*
*	Licensed Materials - Property of IBM Corp.
*
*	Created by Tim Bula on 2016-02-22.
*	Copyright (c) 2016 IBM. All rights reserved. 
*	
*	U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
*	GSA ADP Schedule Contract with IBM Corp.
*
*	Author: Tim Bula
*	Plugin: Box Utilities
*	Filename: boxUploadFile.groovy
 */

import com.urbancode.air.AirPluginTool
import java.util.Map
import java.util.LinkedHashMap
import com.box.sdk.*
import org.bouncycastle.*
import org.bouncycastle.openssl.*
import com.eclipsesource.json.*
import org.jose4j.*
import java.io.File
import java.io.FileInputStream
import com.mobilefirst.box.*


def apTool = new AirPluginTool(this.args[0], this.args[1])
props = apTool.getStepProperties()
final def workDir = new File('.').canonicalFile

String appUserToken = props['app_user_token']
String parentFolderId = props['parent_folder_id']
String parentFolder = props['parent_folder']
String uploadFolder = props['upload_folder']
String fileName = props['file_name']
String filePath = props['file_path']


//if parent folder is empty, we assume root of box
parentFolder = (parentFolder == null || "".equals(parentFolder)) ? "root" : parentFolder;

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println("Using App User auth_token: " + appUserToken);
System.out.println("Establishing Box API Connection");
BoxAPIConnection apiConnection = new BoxAPIConnection(appUserToken);

//get root folder
BoxFolder boxRootFolder = BoxFolder.getRootFolder(apiConnection);
BoxFolder boxUploadFolder = null; 

if (parentFolderId != null && !("".equals(parentFolderId))) {
	System.out.println("Finding parent folder by ID: [" + parentFolderId + "]");
	BoxFolder boxParentFolder = new BoxFolder(apiConnection, parentFolderId);
	Iterable<BoxItem.Info> getChildrenIterable = boxParentFolder.getChildren();
	Iterator<BoxItem.Info> getChilrenIterator = getChildrenIterable.iterator();
	BoxItem.Info boxItemInfo;
	while (getChilrenIterator.hasNext()) {
		boxItemInfo = getChilrenIterator.next();
		if (boxItemInfo.getName().equals(uploadFolder) && boxItemInfo instanceof BoxFolder.Info) {
			System.out.println("Found matching upload folder: [" + boxItemInfo.getName() + "] in parent folder");
			boxUploadFolder = boxItemInfo.getResource();
		}
	}

	if (boxUploadFolder == null) {
		System.out.println("Not able to find matching upload folder in parent folder. Creating upload folder: [" + uploadFolder + "]");
		BoxFolder.Info uploadFolderInfo = boxParentFolder.createFolder(uploadFolder);
		boxUploadFolder = uploadFolderInfo.getResource();
		System.out.println("Created folder: " + uploadFolderInfo.getName());
		if (boxUploadFolder == null) {
			System.err.println("Unable to use upload folder: [" + parentFolder + "]. Exiting now");
			System.exit(1);
		}		
	}
}
else if (!parentFolder.equals("root")) {
	//parent folder id null search from root folder for upload folder that has matching parent folder name
	System.out.println("Searching for upload folder: [" + uploadFolder + "]");
	Iterable<BoxItem.Info> searchResultsIterable = boxRootFolder.search(uploadFolder);
	Iterator<BoxItem.Info> searchResultsIterator = searchResultsIterable.iterator();
	BoxItem.Info boxItemInfo;
	while (searchResultsIterator.hasNext()) {
		boxItemInfo = searchResultsIterator.next();
		if ((boxItemInfo.getName().equals(uploadFolder)) && (boxItemInfo.getParent().getName().equals(parentFolder))) {
			System.out.println("Found matching upload folder with parent folder: [" + parentFolder + "]");
			boxUploadFolder = boxItemInfo.getResource();
		}
	}
	if (boxUploadFolder == null) {
		System.out.println("Not able to find matching upload folder with correct parent folder. Searching for parent folder: [" + parentFolder + "]");
		searchResultsIterable = boxRootFolder.search(parentFolder);
		searchResultsIterator = searchResultsIterable.iterator();
		while (searchResultsIterator.hasNext()) {
			boxItemInfo = searchResultsIterator.next();
			if (boxItemInfo.getName().equals(parentFolder) && boxItemInfo instanceof BoxFolder.Info) {
				System.out.println("Found parent folder");
				BoxFolder boxParentFolder = boxItemInfo.getResource();
				BoxFolder.Info uploadFolderInfo = boxParentFolder.createFolder(uploadFolder);
				boxUploadFolder = uploadFolderInfo.getResource();		
				System.out.println("Created folder: " + uploadFolderInfo.getName());
			}
		}
		if (boxUploadFolder == null) {
			System.err.println("Unable to find parent folder: [" + parentFolder + "]. Exiting now");
			System.exit(1);
		}
	}
}
//if both parent folder id and parent folder are blank
else {
	System.out.println("Parent folder is root of Box account. Looking for matching upload folder: [" + uploadFolder + "]");
	Iterable<BoxItem.Info> boxFolderItemsIterable = boxRootFolder.getChildren();
	Iterator<BoxItem.Info> boxFolderItemsIterator = boxFolderItemsIterable.iterator();
	
	BoxItem.Info boxItemInfo;
	while (boxFolderItemsIterator.hasNext()) {
		boxItemInfo = boxFolderItemsIterator.next();
		if (boxItemInfo.getName().equals(uploadFolder) && boxItemInfo instanceof BoxFolder.Info) {
			System.out.println("Found matching upload folder");
			boxUploadFolder = boxItemInfo.getResource();
		}
	}
	if (boxUploadFolder == null) {
		System.out.println("Not able to find matching folder. Creating folder on box");
		BoxFolder.Info uploadFolderInfo = boxRootFolder.createFolder(uploadFolder);
		boxUploadFolder = uploadFolderInfo.getResource();
		System.out.println("Created folder: " + uploadFolderInfo.getName());
	}
}

//file to upload
File file = new File(filePath);
fileName = (fileName == null || "".equals(fileName)) ? file.getName() : fileName; 
long fileSize = file.length();

System.out.println("Attempting to upload file: " + fileName);
try {
	boxUploadFolder.canUpload(fileName, fileSize);
	
	FileInputStream stream = new FileInputStream(file);
	BoxFile.Info uploadedFileInfo;
	try {
		uploadedFileInfo = boxUploadFolder.uploadFile(stream, fileName);
	} 
	catch (Exception e) {
		System.err.println("Uploading file failed. Exception: " + e.getMessage());
		System.exit(1);
	} 
	stream.close();
	System.out.println("Uploaded file name: " + uploadedFileInfo.getName());
	System.out.println("box.uploaded.file.id:" + uploadedFileInfo.getID());	
}
catch (Exception e) {
	System.err.println("Prelfight check failed. Exception: " + e.getMessage());
	System.exit(1);
}


