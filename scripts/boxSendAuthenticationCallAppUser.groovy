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
*	Filename: boxSendAuthenticationCallAppUser.groovy
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

String appUserId = props['app_user_id']
String clientId = props['client_id']
String clientSecret = props['client_secret']
String publicKeyId = props['public_key_id']
String privateKey = props['private_key']
String privateKeyPath = props['private_key_path']
String privateKeyPassword = props['private_key_password']

System.out.println("appUserId: " + appUserId);
System.out.println("clientId: " + clientId);
System.out.println("clientSecret: " + clientSecret);
System.out.println("publicKeyId: " + publicKeyId);
System.out.println("privateKeyPath: " + privateKeyPath);
System.out.println("privateKeyPassword: " + privateKeyPassword);

//call java class

//build up preferences for JWTAssertion
JWTEncryptionPreferences encryptionPreferences = new JWTEncryptionPreferences();
encryptionPreferences.setPublicKeyID(publicKeyId);
EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.RSA_SHA_256;
encryptionPreferences.setEncryptionAlgorithm(encryptionAlgorithm);
//if the user doesn't set the private key, need the path to the file to be read in and parsed
//how else should this be done? I had issues pasting it into the UCD config field. 
if (privateKey == null || "".equals(privateKey)) {
	try {
        System.out.println("Reading in private key from path: [" + privateKeyPath + "]");
        privateKey = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
        System.out.println("privateKey: " + privateKey);
        encryptionPreferences.setPrivateKey(privateKey);
    } 
    catch (BoxAPIException e) {
        System.err.println("Reading private key error. Error message: " + e.getResponse());
        e.printStackTrace();
        System.exit(1);
    }
} 
else {
    encryptionPreferences.setPrivateKey(privateKey);
}
encryptionPreferences.setPrivateKeyPassword(privateKeyPassword);

//User correct type for assertion for App User
DeveloperEditionEntityType entityType = DeveloperEditionEntityType.USER;

//attempt creation of BoxDeveloperAPIConnection, bypassing manual UI interaction for authentication
//print out token for post processing capture and use in other scripts
System.out.println("Sending App User authentication request with ID: [" + appUserId + "]");
BoxDeveloperEditionAPIConnection apiDevConnection = new BoxDeveloperEditionAPIConnection(appUserId, entityType, clientId, clientSecret, encryptionPreferences);
try {
    apiDevConnection.authenticate();
} 
catch(BoxAPIException e) {
    System.err.println("Exception authenticating with JWT assertion. Error message: " + e.getResponse());
    System.exit(1);
}
String appUserToken = apiDevConnection.getAccessToken();
System.out.println("Successfully authenticated App User");
System.out.println("app.user.auth.token:" + appUserToken);

