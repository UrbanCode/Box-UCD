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
String parentFolder = props['parent_folder']
String uploadFolder = props['upload_folder']
String fileName = props['file_name']
String filePath = props['file_path']


//if parent folder is empty, we assume root of box
parentFolder = (parentFolder == null || "".equals(parentFolder)) ? "root" : parentFolder;

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println("auth_token: " + appUserToken);
BoxAPIConnection apiConnection = new BoxAPIConnection(appUserToken);

//get root folder
BoxFolder boxRootFolder = BoxFolder.getRootFolder(apiConnection);
BoxFolder boxUploadFolder = null; 

if (parentFolder.equals("root")) {
	Iterable<BoxItem.Info> boxFolderItemsIterable = boxRootFolder.getChildren();
	Iterator<BoxItem.Info> boxFolderItemsIterator = boxFolderItemsIterable.iterator();
	
	while (boxFolderItemsIterator.hasNext()) {
		BoxItem.Info boxItemInfo = boxFolderItemsIterator.next();
		if (boxItemInfo.getName().equals(uploadFolder) && boxItemInfo instanceof BoxFolder.Info) {
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
else {
	//search from root folder for upload folder with correct parent
	//if we change this to the id, we can make the search specific to the parent folder
	//ID also ensures that we are creating the new upload folder in the correct parent folder if the
	//folder doesn't exist. User has to figure out the ID of the folder, but it is a better implementation. 
	Iterable<BoxItem.Info> searchResultsIterable = boxRootFolder.search(uploadFolder);
	Iterator<BoxItem.Info> searchResultsIterator = searchResultsIterable.iterator();

	while (searchResultsIterator.hasNext()) {
		BoxItem.Info boxItemInfo = searchResultsIterator.next();
		if ((boxItemInfo.getName().equals(uploadFolder)) && (boxItemInfo.getParent().getName().equals(parentFolder))) {
			boxUploadFolder = boxItemInfo.getResource();
		}
	}
	if (boxUploadFolder == null) {
		System.out.println("Not able to find matching folder in parent. Finding parent and creating folder in it");
		searchResultsIterable = boxRootFolder.search(parentFolder);
		searchResultsIterator = searchResultsIterable.iterator();
		while (searchResultsIterator.hasNext()) {
			BoxItem.Info boxItemInfo = searchResultsIterator.next();
			if (boxItemInfo.getName().equals(parentFolder) && boxItemInfo instanceof BoxFolder.Info) {
				BoxFolder boxParentFolder = boxItemInfo.getResource();
				BoxFolder.Info uploadFolderInfo = boxParentFolder.createFolder(uploadFolder);
				boxUploadFolder = uploadFolderInfo.getResource();
				System.out.println("Created folder: " + uploadFolderInfo.getName());
			}
		}
		if (boxUploadFolder == null) {
			System.err.println("Unable able to create folder: [" + uploadFolder +"] in parent folder [" + parentFolder + "]. Exiting now");
			System.exit(1);
		}
	}
}
//file to upload
File file = new File(filePath);
fileName = (fileName == null || "".equals(fileName)) ? file.getName() : fileName; 
long fileSize = file.length();
System.out.println("Uploading file: " + fileName);


try {
	boxUploadFolder.canUpload(fileName, fileSize);
	try {
		FileInputStream stream = new FileInputStream(file);
		BoxFile.Info uploadedFileInfo = boxUploadFolder.uploadFile(stream, fileName); 
		stream.close();
		System.out.println("Uploaded file name: " + uploadedFileInfo.getName());
		System.out.println("Uploaded file id: " + uploadedFileInfo.getID());	
	} 
	catch (Exception e) {
		System.err.println("Uploading file failed. Exception: " + e.getMessage());
		System.exit(1);
	}
}
catch (Exception e) {
	System.err.println("Prelfight check failed. Exception: " + e.getMessage());
	System.exit(1);
}


