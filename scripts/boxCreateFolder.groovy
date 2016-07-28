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
*	Filename: boxCreateFolder.groovy
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
String folder = props['folder']

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println("Using App User auth_token: " + appUserToken);
System.out.println("Establishing Box API Connection");
BoxAPIConnection apiConnection = new BoxAPIConnection(appUserToken);

String folderId = "";
if (parentFolderId != null && !("".equals(parentFolderId))) {
System.out.println("Finding/creating folder in parent folder ID: [" + parentFolderId + "]");
folderId = createFolderInParentId(apiConnection, parentFolderId, folder);
}
else if (parentFolder == null || !("".equals(parentFolder))) {
System.out.println("Finding/creating folder in parent folder name: [" + parentFolder + "]");
folderId = createFolderInParentName(apiConnection, parentFolder, folder);
}
//if both parent folder id and parent folder are blank
else {
System.out.println("Finding/creating folder in root folder");
folderId = createFolderInRoot(apiConnection, folder);	
}
System.out.println("box.folder.id:" + folderId);

//find child folder in parent id folder
//create child folder if not found
public String createFolderInParentId(BoxAPIConnection apiConnection, String parentFolderId, String folder) {

BoxFolder boxParentFolder = new BoxFolder(apiConnection, parentFolderId);
Iterable<BoxItem.Info> getChildrenIterable = boxParentFolder.getChildren();
Iterator<BoxItem.Info> getChilrenIterator = getChildrenIterable.iterator();
BoxItem.Info boxItemInfo;
while (getChilrenIterator.hasNext()) {
boxItemInfo = getChilrenIterator.next();
if (boxItemInfo.getName().equals(folder) && boxItemInfo instanceof BoxFolder.Info) {
	System.out.println("Found matching folder: [" + boxItemInfo.getName() + "] in parent folder");
	return boxItemInfo.getID();
}
}
System.out.println("Not able to find matching folder in parent folder. Creating folder: [" + folder + "]");
BoxFolder.Info folderInfo = boxParentFolder.createFolder(folder);
System.out.println("Created folder: " + folderInfo.getName());
return folderInfo.getID();

}

//find child folder in parent folder by name
//create child folder if not found
public String createFolderInParentName(BoxAPIConnection apiConnection, String parentFolder, String folder) {

System.out.println("Searching for folder: [" + folder + "]");

BoxFolder boxRootFolder = BoxFolder.getRootFolder(apiConnection); 
Iterable<BoxItem.Info> searchResultsIterable = boxRootFolder.search(folder);
Iterator<BoxItem.Info> searchResultsIterator = searchResultsIterable.iterator();

BoxItem.Info boxItemInfo;
while (searchResultsIterator.hasNext()) {
boxItemInfo = searchResultsIterator.next();
if ((boxItemInfo.getName().equals(folder)) && (boxItemInfo.getParent().getName().equals(parentFolder)) && (boxItemInfo instanceof BoxFolder.Info)) {
	System.out.println("Found matching folder with parent folder: [" + parentFolder + "]");
	return boxItemInfo.getID();
}
}
System.out.println("Not able to find matching folder with correct parent folder. Searching for parent folder: [" + parentFolder + "]");
searchResultsIterable = boxRootFolder.search(parentFolder);
searchResultsIterator = searchResultsIterable.iterator();

while (searchResultsIterator.hasNext()) {
boxItemInfo = searchResultsIterator.next();
if (boxItemInfo.getName().equals(parentFolder) && boxItemInfo instanceof BoxFolder.Info) {
	System.out.println("Found parent folder");
	BoxFolder boxParentFolder = boxItemInfo.getResource();
	BoxFolder.Info folderInfo = boxParentFolder.createFolder(folder);		
	System.out.println("Created folder: " + folderInfo.getName());
	return folderInfo.getID();
}
}
System.err.println("Unable to find parent folder: [" + parentFolder + "]. Exiting now");
System.exit(1);		
}

//create child folder in root 
public String createFolderInRoot(BoxAPIConnection apiConnection, String folder) {
System.out.println("Parent folder is root of Box account. Looking for matching folder: [" + folder + "]");

BoxFolder boxRootFolder = BoxFolder.getRootFolder(apiConnection); 
Iterable<BoxItem.Info> boxFolderItemsIterable = boxRootFolder.getChildren();
Iterator<BoxItem.Info> boxFolderItemsIterator = boxFolderItemsIterable.iterator();

BoxItem.Info boxItemInfo;
while (boxFolderItemsIterator.hasNext()) {
boxItemInfo = boxFolderItemsIterator.next();
if (boxItemInfo.getName().equals(folder) && boxItemInfo instanceof BoxFolder.Info) {
	System.out.println("Found matching folder");
	return boxItemInfo.getID();
}
}
System.out.println("Not able to find matching folder. Creating folder on box");
BoxFolder.Info folderInfo = boxRootFolder.createFolder(folder);
System.out.println("Created folder: " + folderInfo.getName());
return folderInfo.getID();
}



