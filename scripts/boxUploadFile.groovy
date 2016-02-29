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

def dev_token = props['dev_token']
def file_path = props['file_path']
def file_name = props['file_name']

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println("auth_token: " + dev_token);//this is not working need help on where to place the property after getting it from the readout
BoxAPIConnection api = new BoxAPIConnection(dev_token);

//print out user information just to confirm that it works
BoxUser.Info user_info = BoxUser.getCurrentUser(api).getInfo();
System.out.format("Welcome, %s <%s>!\n", user_info.getName(), user_info.getLogin());

//get root level folder to upload file. Should give user flexibility to define the folder in the future?
BoxFolderTim root_folder = BoxFolderTim.getRootFolderTim(api);
System.out.println(root_folder.getInfo());

//file to upload
File file = new File(file_path);
file_name = (file_name == null || file_name == "") ? file.getName() : file_name; 
System.out.println(file_name);

BoxAPIResponse response = new BoxAPIResponse();
//canUpload returns error if any of the preflight check fails
try {
	response = root_folder.canUploadTim(file_name, file.length());
	root_folder.canUploadTim(file_name, file.length());
} catch(Exception e) {
	System.err.println("Can't upload. API Error: " + e);
}

if (response.getResponseCode() == 200) {
	try {
		FileInputStream stream = new FileInputStream(file);
		BoxFile.Info uploaded_file_info = root_folder.uploadFile(stream, file_name); 
		stream.close();
		uploaded_file = uploaded_file_info.getResource();
		System.out.println("Uploaded file name: " + uploaded_file.getInfo().getName());
		System.out.println("Uploaded file name: " + uploaded_file.getInfo().getID());	

	} catch(Exception e) {
		System.err.println("Upload file unsuccessful: " + e);
	}
} else {
	System.err.println("Pre flight check failed. Response code: " + response.getResponseCode());
}
