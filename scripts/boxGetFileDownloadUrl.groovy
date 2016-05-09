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
*	Filename: boxGetFileDownloadUrl.groovy
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
String fileId = props['file_id']

//call java class

//new connection to box using the dev token. Need to use the set property from the auth call in the future
System.out.println("Using App User auth_token: " + appUserToken);
System.out.println("Establishing Box API Connection");
BoxAPIConnection apiConnection = new BoxAPIConnection(appUserToken);

//get file
System.out.println("Using file id: " + fileId);
BoxFile boxFile = new BoxFile(apiConnection, fileId);
System.out.println("boxfile id: " + boxFile.getInfo().getID());
System.out.println("boxfile name: " + boxFile.getInfo().getName());


BoxSharedLink.Access access = BoxSharedLink.Access.DEFAULT;
BoxSharedLink.Permissions permissions = new BoxSharedLink.Permissions();
permissions.setCanDownload(true);
permissions.setCanPreview(true);
BoxSharedLink sharedLink = boxFile.createSharedLink(access, null, permissions);
System.out.println("box.file.download.url:" + sharedLink.getDownloadURL());



