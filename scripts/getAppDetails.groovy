/*
*	Licensed Materials - Property of IBM Corp.
*
*	Created by Tyson Lawrie on 2015-03-05.
*	Copyright (c) 2015 IBM. All rights reserved. 
*	
*	U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
*	GSA ADP Schedule Contract with IBM Corp.
*
*	Author: Tyson Lawrie & Glen Hickman	
*	Plugin: MaaS360 Utilities
*	Filename: getDeviceGroups.groovy
 */

import com.urbancode.air.AirPluginTool
import java.util.Map
import java.util.LinkedHashMap
import java.io.File;
import java.util.Hashtable;
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.RequestEntity
import org.apache.commons.httpclient.methods.StringRequestEntity

import com.mobilefirst.fiberlink.WebServiceRequest
import com.mobilefirst.fiberlink.WebServices

//Pull in properties from Plugin UI
def apTool = new AirPluginTool(this.args[0], this.args[1])
props = apTool.getStepProperties()
final def workDir = new File('.').canonicalFile

def url = props['url']
def billing_id = props['billing_id']
def auth_token = props['auth_token']
def app_bundle_id = props['app_bundle_id']
def app_type = props['app_type']
def outFileName = props['outFile']
def outFile
if (outFileName) {
	outFile = new File(outFileName)
	if (!outFile.isAbsolute()) {
		outFile = new File(workDir, outFileName)
	}
}

//Start code for creating a WS request
WebServiceRequest request = new WebServiceRequest()

// Set request parameters
request.billingId = billing_id
request.url = url
request.authToken = auth_token
request.accept =  null

//Generate HTTPParameters
request.parameters.put("appType", app_type);
request.parameters.put("appId", app_bundle_id);

//Create Request
request.createRequest(WebServices.GetAppDetailsURI.getURL(), 0, billing_id)