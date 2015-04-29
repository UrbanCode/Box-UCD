/*
*	Licensed Materials - Property of IBM Corp.
*
*	Created by Tyson Lawrie on 2015-03-05.
*	Copyright (c) 2015 IBM. All rights reserved. 
*	
*	U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
*	GSA ADP Schedule Contract with IBM Corp.
*	
*	Plugin: MaaS360 Utilities
*	Filename: distributeAPP.groovy
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
 import org.apache.commons.httpclient.methods.multipart.FilePart
 import org.apache.commons.httpclient.methods.multipart.Part
 import org.apache.commons.httpclient.methods.multipart.StringPart
 
 import com.mobilefirst.fiberlink.WebServiceRequest
 import com.mobilefirst.fiberlink.WebServices
 
 //Pull in properties from Plugin UI
 def apTool = new AirPluginTool(this.args[0], this.args[1])
 props = apTool.getStepProperties()
 final def workDir = new File('.').canonicalFile
 
 def url = props['url']
 def billing_id = props['billing_id']
 def app_type = props['app_type']
 def app_bundle_id = props['app_bundle_id']
 def target_devices = props['target_devices']
 def device_group_id = props['device_group_id']
 def device_id = props['device_id']
 def instant_install = props['instant_install']
 def send_email = props['send_email']
 def outFileName = props['outFile']
 def outFile
 if (outFileName) {
	 outFile = new File(outFileName)
	 if (!outFile.isAbsolute()) {
		 outFile = new File(workDir, outFileName)
	 }
 }
 def auth_token = props['auth_token']
 
 //Start code for creating a WS request
 WebServiceRequest request = new WebServiceRequest()
 
/* // Set request parameters
 Hashtable<String, Object> parametersObjectList = new Hashtable<String, Object>()
 Hashtable<String, String> paramsList = new Hashtable<String, String>()
 paramsList.put("accountType", "Customer")
 paramsList.put("accountName", "TestFLK_IBM_MobileCoC")
 paramsList.put("adminEmailAddress", "mfiosdev@us.ibm.com")
 paramsList.put("billingID", billing_id)
 paramsList.put("appID", app_bundle_id)
 paramsList.put("userName", "mfiosdev@us.ibm.com")
 paramsList.put("password", "appleibm10\$")
 
 parametersObjectList.put("parameters", paramsList)*/
 
 request.billingId = billing_id
 request.url = url
 request.authToken = auth_token
 request.accept =  "application/xml"
 
 //Generate HTTPParameters
 request.parameters.put("appType", app_type);
 request.parameters.put("appId", app_bundle_id);
 request.parameters.put("targetDevices", target_devices);
 request.parameters.put("deviceGroupId", device_group_id);
 request.parameters.put("instantInstall", instant_install);
 request.parameters.put("sendEmail", send_email);
 
 //Create Request
 request.createRequest(WebServices.DistributeAppURI.getURL(), 1, billing_id)
 