/*
*	Altered by Tyson Lawrie on 2015-03-05.
*	Originally developed by Fiberlink
*	
*	Plugin: MaaS360 Utilities
*	Filename: Authenticator.java
 */

package com.mobilefirst.fiberlink;

import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * Authenticator shows how to authenticate against the MaaS360 authetication web services 
 * using the customer's MaaS administrator user name and password.
 */
public class Authenticator 
{
	// [[ Section to be configured by the caller
	// The base URL for services exposed by MaaS
	// Get the URL for your MaaS instance from Fiberlink (the URL should end with a /)
	//static final String ROOT_WS_URL = "XXXXXXX"; 			// Should look like "https://services.fiberlink.com/"
	//static final String BILLING_ID = "XXXXXXX";			// Billing Identifier - Available after customer enrolls with MaaS360
	//static final String PLATFORM_ID = "3";				// This should be the application platform id provided during application provisioning
	//static final String APP_ID = "com.mycompany.myapp"; 		// Provided during application provisioning
	//static final String APP_VERSION = "1.0";			// Provided during application provisioning 
	//static final String APP_ACCESS_KEY = "uiyt5643gh";		// Generated after the application has been provisioned
	//static final String MAAS_ADMIN_USERNAME = "myusername";  	// The portal administrator principal
	//static final String MAAS_ADMIN_PASSWORD = "mypassword"; 	// The portal administrator's credentials
	// Section to be configured by the caller ]]
	
	//static final String AUTH_ROOT_URL = ROOT_WS_URL + "auth-apis/auth/1.0/authenticate/";
	static final String ADMIN_ROOT_TAG = "maaS360AdminAuth";
	
	public final void sendRequest(PostMethod post) {
		try {
			HttpClient client = new HttpClient();
			int statusCode = client.executeMethod(post);
			System.out.println("------------------------------------Begin Debug: Request Headers----------------------------------------------------------\n");
			Header[] requestHeaders = post.getRequestHeaders();
			for(int cn = 0;cn<requestHeaders.length;cn++) {
				System.out.println(requestHeaders[cn].toString());
			}
			System.out.println("------------------------------------Begin Debug: Response Headers----------------------------------------------------------\n");
			Header[] responseHeaders = post.getResponseHeaders();
			for(int cn = 0;cn<responseHeaders.length;cn++) {
				System.out.println(responseHeaders[cn].toString());
			}
			System.out.println("------------------------------------End Debug----------------------------------------------------------\n");
			if (statusCode != HttpStatus.SC_OK) {
				System.out.println("POST method failed: "
						+ post.getStatusLine());
			} else {
				System.out.println("POST method succeeded: "
						+ post.getStatusLine());
				String httpResponse = post.getResponseBodyAsString();
				System.out.println(httpResponse);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final String createAuthTemplateXML(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<authRequest>").append("\n").append("<")
				.append(getAuthTemplateTag()).append(">").append("\n");

		for (String key : params.keySet()) {
			sb.append("<").append(key).append(">").append(params.get(key))
					.append("</").append(key).append(">").append("\n");
		}
		sb.append("</").append(getAuthTemplateTag()).append(">").append("\n")
				.append("</authRequest>").append("\n");

		return sb.toString();
	}

	public String getAuthTemplateTag() {
		return ADMIN_ROOT_TAG;
	}
	
	//The following method has been appropriated from the Main () class
	//for the purpose of being called from the orchestrating groovy class
	
	public PostMethod createRequest(String xml, String url_auth, String billing_id) {
		PostMethod post = new PostMethod(url_auth + billing_id + "/");
		try {
			RequestEntity requestEntity = new StringRequestEntity(xml,
					"application/xml", "UTF-8");
			post.setRequestEntity(requestEntity);
			post.addRequestHeader("Accept", "application/xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return post;
	}
}
