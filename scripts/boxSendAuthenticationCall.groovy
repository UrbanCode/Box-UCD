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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


def apTool = new AirPluginTool(this.args[0], this.args[1])
props = apTool.getStepProperties()
final def workDir = new File('.').canonicalFile

def entity_id = props['entity_id']
def client_id = props['client_id']
def client_secret = props['client_secret']
def public_key_id = props['public_key_id']
def private_key = props['private_key']
def private_key_path = props['private_key_path']
def private_key_password = props['private_key_password']
def username = props['username']
def password = props['password']

//build up preferences for JWTAssertion
JWTEncryptionPreferences encryption_preferences = new JWTEncryptionPreferences();
encryption_preferences.setPublicKeyID(public_key_id);
encryption_preferences.setPrivateKeyPassword(private_key_password);
EncryptionAlgorithm encryption_algorithm = EncryptionAlgorithm.RSA_SHA_256;
encryption_preferences.setEncryptionAlgorithm(encryption_algorithm);
//if the user doesn't set the private key, need the path to the file to be read in and parsed
if (private_key == null || private_key == "") {
	try {
        //String private_key_file = "/Users/bula/Documents/apple_devops/projects/box_plugin/private_key.pem";
        private_key = new String(Files.readAllBytes(Paths.get(private_key_path)));
        encryption_preferences.setPrivateKey(private_key);
    } 
    catch (Exception e) {
        System.out.println("Reading private key error. Error message: " + e.getMessage());
        e.printStackTrace();
    }
} 
else {
    encryption_preferences.setPrivateKey(private_key);
}


//User info needed for assertion
DeveloperEditionEntityType entity_type = DeveloperEditionEntityType.ENTERPRISE;

//attempt creation of BoxDeveloperAPIConnection, bypassing manual UI interaction for authentication
//print out token for post processing capture and use in other scripts
try {
    BoxDeveloperEditionAPIConnection api_dev = new BoxDeveloperEditionAPIConnection(entity_id, entity_type, client_id, client_secret, encryption_preferences);
    String jwt_assertion = api_dev.constructJWTAssertion();
	System.out.println(jwt_assertion);
    apiDev.authenticate();
    String api_token = api_dev.getAccessToken();
    System.out.println("box.auth.token:" + api_token);

} 
catch(Exception e) {
	System.err.println("Exception with JWT assertion. Error message: " + e.getMessage());
    e.printStackTrace();
}
