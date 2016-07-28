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
String fileName = props['file_name']
String filePath = props['file_path']

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println("Using App User auth_token: " + appUserToken);
System.out.println("Establishing Box API Connection");
BoxAPIConnection apiConnection = new BoxAPIConnection(appUserToken);
BoxFolder boxParentFolder = new BoxFolder(apiConnection, parentFolderId);
System.out.println("boxParentFolder name: " + boxParentFolder.getInfo().getName());

//file to upload
File file = new File(filePath);
fileName = (fileName == null || "".equals(fileName)) ? file.getName() : fileName; 
long fileSize = file.length();

System.out.println("Attempting to upload file: " + fileName);	
FileInputStream stream = new FileInputStream(file);

//check to see if file can be uploaded
//upload new version if fails b/c of name collision
//exit if preflight check fails for other reason
try {
boxParentFolder.canUpload(fileName, fileSize);
}
catch (BoxAPIException e) {
if (e.getResponseCode() == 409) {
	System.err.println("API Response 409. Likely name collision in folder");
	uploadVersion(boxParentFolder, fileName, stream);
} else {
	System.err.println("Preflight check failed. Exception: " + e.getResponse())
	e.printStackTrace();
	System.exit(1);
}
}

//attempt to upload file
BoxFile.Info uploadedFileInfo;
try {
uploadedFileInfo = boxParentFolder.uploadFile(stream, fileName);
} 
catch (BoxAPIException e) {
System.err.println("Uploading file failed. Exception: " + e.getResponse());
e.printStackTrace();
System.exit(1);
} 

stream.close();
System.out.println("Uploaded file name: " + uploadedFileInfo.getName());
System.out.println("box.uploaded.file.id:" + uploadedFileInfo.getID());	


//function to find previous version of file in parent folder
//upload new version of file if found
public void uploadVersion(BoxFolder boxParentFolder, String fileName, FileInputStream stream) {
System.out.println("Searhcing for existing box file");
Iterable<BoxItem.Info> getChildrenIterable = boxParentFolder.getChildren();
Iterator<BoxItem.Info> getChilrenIterator = getChildrenIterable.iterator();
BoxItem.Info boxItemInfo;
while (getChilrenIterator.hasNext()) {
	boxItemInfo = getChilrenIterator.next();
	if (boxItemInfo.getName().equals(fileName) && boxItemInfo instanceof BoxFile.Info) {
		System.out.println("Found matching file: [" + boxItemInfo.getName() + "] in parent folder");
		System.out.println("Uploading new version");
		BoxFile existingBoxFile = boxItemInfo.getResource();
		existingBoxFile.uploadVersion(stream);
		System.out.println("New version uploaded");
		System.out.println("Uploaded file name: " + boxItemInfo.getName());
		System.out.println("box.uploaded.file.id:" + boxItemInfo.getID());	
		System.exit(0);
	}
}
System.out.println("Not able to find matching file in parent folder to update. Exiting out");
System.exit(1);

}


