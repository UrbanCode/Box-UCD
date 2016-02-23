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

def apTool = new AirPluginTool(this.args[0], this.args[1])
props = apTool.getStepProperties()
final def workDir = new File('.').canonicalFile

def dev_token = props['dev_token']
def file_path = props['file_path']
def file_name = props['file_name']

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println(dev_token);
BoxAPIConnection api = new BoxAPIConnection(dev_token);

//print out user information
BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
System.out.format("Welcome, %s <%s>!\n\n", userInfo.getName(), userInfo.getLogin());

//get root level folder to upload file. Should give user flexibility to define the folder in the future?
BoxFolder rootFolder = BoxFolder.getRootFolder(api);
System.out.println(rootFolder.getInfo());

//file to upload
File file = new File("file_path");

//check to see if a file can be uploaded
/*
try {
	BoxAPIResponse response = rootFolder.canUpload("test_box_api2.txt", file.length());
	System.out.println(response);
}catch(Exception e){
	System.out.println(e);
}
*/

//attempt to upload file
//BoxFile uploadedFile = null; 
try {
	FileInputStream stream = new FileInputStream(file);
	BoxFile.Info uploadedFileInfo = rootFolder.uploadFile(stream, file_name); 
	stream.close();
	uploadedFile = uploadedFileInfo.getResource();
	System.out.println(uploadedFile.getDownloadURL());
	System.out.println(uploadedFile.getInfo().getID());
	System.out.println(uploadedFile.getInfo().getName());

} catch(Exception e) {
	System.err.println(e);
}
