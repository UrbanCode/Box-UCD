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
*	Filename: boxDeleteFile.groovy
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
def file_name = props['file_name']
//def file_id = props['file_id'] for the future if we want to save the file id property somehow
//how many levels down you want to search
def MAX_DEPTH = props['max_depth']

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println(dev_token);//this is not working need help on where to place the property after getting it from the readout
BoxAPIConnection api = new BoxAPIConnection(dev_token);

//print out user information just to confirm that it works
BoxUser.Info user_info = BoxUser.getCurrentUser(api).getInfo();
System.out.format("Welcome, %s <%s>!\n", userInfo.getName(), userInfo.getLogin());

//get root level folder to upload file. Should give user flexibility to define the folder in the future?
BoxFolder root_folder = BoxFolder.getRootFolder(api);
System.out.println(root_folder.getInfo());

listFolderContents(root_folder, 0, file_name)

private static void searchAndDeleteFile(BoxFolder root_folder, int depth, file) {
	for (BoxItem.Info itemInfo : folder) {
		if (itemInfo instanceof BoxFile.Info && itemInfo.getName() == file) {
			itemInfo.getResource.delete();
		} else if (itemInfo instanceof BoxFolder.Info) {
	        BoxFolder childFolder = (BoxFolder) itemInfo.getResource();
	        if (depth < MAX_DEPTH) {
	            listFolder(childFolder, depth + 1);
	        }
	     }
	}
}

