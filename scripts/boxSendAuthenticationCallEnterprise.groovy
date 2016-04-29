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
*	Filename: boxSendAuthenticationCall.groovy
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

String entityId = props['entity_id']
String clientId = props['client_id']
String clientSecret = props['client_secret']
String publicKeyId = props['public_key_id']
String privateKey = props['private_key']
String privateKeyPath = props['private_key_path']
String privateKeyPassword = props['private_key_password']
String appUserName = props['app_user_name']

System.out.println("entityId: " + entityId);
System.out.println("clientId: " + clientId);
System.out.println("clientSecret: " + clientSecret);
System.out.println("publicKeyId: " + publicKeyId);
System.out.println("privateKeyPath: " + privateKeyPath);
System.out.println("privateKeyPassword: " + privateKeyPassword);
System.out.println("appUserName: " + appUserName);

//build up preferences for JWTAssertion
JWTEncryptionPreferences encryptionPreferences = new JWTEncryptionPreferences();
encryptionPreferences.setPublicKeyID(publicKeyId);
encryptionPreferences.setPrivateKeyPassword(privateKeyPassword);
EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.RSA_SHA_256;
encryptionPreferences.setEncryptionAlgorithm(encryptionAlgorithm);

if (privateKey == null || "".equals(privateKey)) {
	try {
        privateKey = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
        encryptionPreferences.setPrivateKey(privateKey);
    } 
    catch (Exception e) {
        System.out.println("Reading private key error. Error message: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }
} 
else {
    encryptionPreferences.setPrivateKey(privateKey);
}

//User info needed for assertion
DeveloperEditionEntityType entityType = DeveloperEditionEntityType.ENTERPRISE;

//attempt creation of BoxDeveloperAPIConnection, bypassing manual UI interaction for authentication
//print out token for post processing capture and use in other scripts

BoxDeveloperEditionAPIConnection apiDevConnection = new BoxDeveloperEditionAPIConnection(entityId, entityType, clientId, clientSecret, encryptionPreferences);
try {
    apiDevConnection.authenticate();
} 
catch(Exception e) {
    System.err.println("Exception authenticating enterprise with JWT assertion. Error message: " + e.getMessage());
    System.exit(1);
}   
String enterpriseToken = apiDevConnection.getAccessToken();
String foundUserId = getAppUserId(enterpriseToken, appUserName); 
System.out.println("box.app.user.id:" + foundUserId);

String getAppUserId(String enterpriseToken, String appUserName) {
    System.out.println("Matching provided App User name to App User in Enterprise");
    BoxAPIConnection apiConnection;    
    try {
        apiConnection = new BoxAPIConnection(enterpriseToken);
    }
    catch (Exception e) {
        System.err.println("Exception establishing Box API connection with enterprise auth token . Error message: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }

    Iterable<BoxUser.Info> enterpriseUsersIterable = BoxUser.getAllEnterpriseUsers(apiConnection);
    Iterator<BoxUser.Info> enterpriseUserIterator = enterpriseUsersIterable.iterator(); 
    while (enterpriseUserIterator.hasNext()) {
        BoxUser.Info boxUserInfo = enterpriseUserIterator.next();
        if (appUserName.equals(boxUserInfo.getName())) {
            System.out.println("Found matching App User");
            return boxUserInfo.getID(); 
        }
    }
    
    System.out.println("Did not find app user in enterprise that matched App User name: [" + appUserName + "]. Creating App User");
    CreateUserParams params = new CreateUserParams();
    params = params.setSpaceAmount(-1);
    BoxUser.Info newAppUser = BoxUser.createAppUser(apiConnection, appUserName, params);
    return newAppUser.getID();

}



