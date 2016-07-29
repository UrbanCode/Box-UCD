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
*	Filename: boxCreateAppUser.groovy
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
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.impl.*;
import org.slf4j.*;
import groovy.util.logging.Slf4j



def apTool = new AirPluginTool(this.args[0], this.args[1])
props = apTool.getStepProperties()
final def workDir = new File('.').canonicalFile

String enterpriseToken = props['enterprise_token']
String appUserName = props['app_user_name']

System.out.println("enterpriseToken: " + enterpriseToken)
System.out.println("appUserName: " + appUserName);

String appUserId = getAppUserId(enterpriseToken, appUserName); 
System.out.println("box.app.user.id:" + appUserId);

//find app user id using the provided name
//create app user if name is unique to account
String getAppUserId(String enterpriseToken, String appUserName) {
System.out.println("Matching provided App User name to App User in Enterprise");
BoxAPIConnection apiConnection;    
try {
apiConnection = new BoxAPIConnection(enterpriseToken);
}
catch (BoxAPIException e) {
System.err.println("Error establishing Box API connection with enterprise auth token . Error message: " + e.getResponse());
e.printStackTrace();
System.exit(1);
}

Iterable<BoxUser.Info> enterpriseUsersIterable = BoxUser.getAllEnterpriseUsers(apiConnection);
Iterator<BoxUser.Info> enterpriseUsersIterator = enterpriseUsersIterable.iterator();
BoxUser.Info boxUserInfo;
while (enterpriseUsersIterator.hasNext()) {
boxUserInfo = enterpriseUsersIterator.next();
if (appUserName.equals(boxUserInfo.getName())) {
System.out.println("Found matching App User on account with name: [" + appUserName + "]");
return boxUserInfo.getID(); 
}
}

System.out.println("App User name: [" + appUserName + "] is unique for account. Creating App User");
CreateUserParams params = new CreateUserParams();
params = params.setSpaceAmount(-1);
BoxUser.Info newAppUser = BoxUser.createAppUser(apiConnection, appUserName, params);
return newAppUser.getID();

}



