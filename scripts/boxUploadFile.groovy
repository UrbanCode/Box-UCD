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

BoxFolderTim upload_folder;

def dev_token = props['dev_token']

//generic
def folder_name = props['folder']
def file_path = props['file_path']
def file_name = props['file_name']

//ipa
def component = props['component']
//either from codestation or the build server?
def file_path = props['file_path']
def version = props['version']

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println("auth_token: " + dev_token);//this is not working need help on where to place the property after getting it from the readout
BoxAPIConnection api = new BoxAPIConnection(dev_token);

//print out user information just to confirm that it works
BoxUser.Info user_info = BoxUser.getCurrentUser(api).getInfo();
System.out.format("Welcome, %s <%s>!\n", user_info.getName(), user_info.getLogin());

//get root folder
BoxFolderTim root_folder = BoxFolderTim.getRootFolderTim(api);

//searches the entire box account. Could change this to only search the root or 'app' folder
Iterable<BoxItem.Info> search_results = root_folder.search(folder_name)
if (search_results != null) {
	for (BoxItem.Info item_info : search_results) {
		System.out.println(item_info.getInfo());
		if (item_info instanceof BoxFolder.Info && item_info.getName() == folder_name) {
			upload_folder = item_info.getResource()
		}
	}
} 
else {
	System.out.println("Not able to find matching folder. Creating folder on box");
	BoxFolderTim.Info upload_folder_info = root_folder.createFolder(folder_name);
	upload_folder = upload_folder_info.getResource();
	System.out.println(upload_folder.getInfo());
}

//file to upload
File file = new File(file_path);
file_name = (file_name == null || file_name == "") ? file.getName() : file_name; 
System.out.println("Uploading file: " + file_name);

BoxAPIResponse response = new BoxAPIResponse();
//canUpload returns error if any of the preflight check fails
try {
	response = new_folder.canUploadTim(file_name, file.length()); 
} catch(Exception e) {
	System.err.println("Can't upload. API Error: " + e);
}

if (response.getResponseCode() == 200) {
	try {
		FileInputStream stream = new FileInputStream(file);
		BoxFile.Info uploaded_file_info = component_folder.uploadFile(stream, file_name); 
		stream.close();
		uploaded_file = uploaded_file_info.getResource();
		System.out.println("Uploaded file name: " + uploaded_file.getInfo().getName());
		System.out.println("Uploaded file id: " + uploaded_file.getInfo().getID());	

	} catch(Exception e) {
		System.err.println("Upload file unsuccessful: " + e);
	}
} else {
	System.err.println("Pre flight check failed. Response code: " + response.getResponseCode());
}
