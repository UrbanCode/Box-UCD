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
//if the user doesn't set the private key, need the path to the file to be read in and parsed
if (privateKey == null || "".equals(privateKey)) {
	try {
        //String private_key_file = "/Users/bula/Documents/apple_devops/projects/box_plugin/private_key.pem";
        privateKey = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
        encryptionPreferences.setPrivateKey(privateKey);
    } 
    catch (Exception e) {
        System.out.println("Reading private key error. Error message: " + e.getMessage());
        e.printStackTrace();
    }
} 
else {
    encryptionPreferences.setPrivateKey(privateKey);
}

//User info needed for assertion
DeveloperEditionEntityType entityType = DeveloperEditionEntityType.ENTERPRISE;

//attempt creation of BoxDeveloperAPIConnection, bypassing manual UI interaction for authentication
//print out token for post processing capture and use in other scripts
try {
    BoxDeveloperEditionAPIConnection apiDevConnection = new BoxDeveloperEditionAPIConnection(entityId, entityType, clientId, clientSecret, encryptionPreferences);
    String jwtAssertion = apiDevConnection.constructJWTAssertion();
    apiDevConnection.authenticate();
    String enterpriseToken = apiDevConnection.getAccessToken();
    String foundUserId = getAppUserId(enterpriseToken, appUserName); 
    System.out.println("box.app.user.id:" + foundUserId);


} 
catch(Exception e) {
	System.err.println("Exception with JWT assertion. Error message: " + e.getMessage());
    e.printStackTrace();
}


private String getAppUserId(String enterpriseToken, String appUserName) {   
    try {
        BoxAPIConnection apiConnection = new BoxAPIConnection(enterpriseToken);

        //this if block should be taken out if we make appUserNames a mandatory plugin field. 
        if (appUserName == null || "".equals(appUserName)) {
            CreateUserParams params = new CreateUserParams();
            params = params.setSpaceAmount(-1);
            BoxUser.Info appUserInfo = BoxUser.createAppUser(api, "default", params);
            return newAppUser.getID();
        }
        else {
            Iterable<BoxUser.Info> enterpriseUsersIterable = BoxUser.getAllEnterpriseUsers(apiConnection);
            Iterator<BoxUser.Info> enterpriseUserIterator = enterpriseUsersIterable.iterator(); 
            while (enterpriseUserIterator.hasNext()) {
                BoxUser.Info boxUserInfo = enterpriseUserIterator.next();
                if (appUserName.equals(boxUserInfo.getName())) {
                        return boxUserInfo.getID(); 
                }
            }
            
            //do we want to give them the ability to create app users at will? This is necessary for the first time
            //if they were to create an app user outside of the plugin we could remove this 
            System.out.println("Did not find app user for enterprise that matched App User name: [" + appUserName + "]. Creating App User");
            CreateUserParams params = new CreateUserParams();
            params = params.setSpaceAmount(-1);
            BoxUser.Info newAppUser = BoxUser.createAppUser(apiConnection, appUserName, params);
            return newAppUser.getID();
        }
    }
    catch (Exception e) {
        System.err.println("Exception with getting App User ID. Error message: " + e.getMessage());
        e.printStackTrace();
    }

}



