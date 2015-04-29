/*
*	Altered by Tyson Lawrie on 2015-03-05.
*	Originally developed by Fiberlink
*	
*	Plugin: MaaS360 Utilities
*	Filename: WebServiceRequest.java
 */

package com.mobilefirst.fiberlink;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class WebServiceRequest {
	private static String servicePath = null;
	public String accept = null;
	protected int statusCode = 0;

	private final HttpClient client = new HttpClient();

	public PostMethod postMethod = null;
	private GetMethod getMethod = null;
	public String url = null;

	private String jsessionId = null;
	public String authToken = null;

	private final static String ADMIN_ROOT_TAG = "maaS360AdminAuth";
	public String responseFilePath = null;
	
	public String url_auth = null;
	
	public String responseBody = null;

//	int partSize = 0;
	public Hashtable<String, String> parameters = new Hashtable<String, String>();
	public Hashtable<String, String> headers = new Hashtable<String, String>();
	public LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
	public Part[] part = null;
	
	String billingId = null;
	String spsBillingId = null;
	String mamBillingId = null;
//	File customerFile = null;
	Properties properties = new Properties();
	
/*	public static void main(String[] args) throws Exception {
		WebServiceRequest webServiceRequest = new WebServiceRequest();
		webServiceRequest.execute();
	}
	
	public WebServiceRequest() throws Exception {
		ftaRunConfig = new FTARunConfig();
		ftaRunConfig.execute();
		testData.putAll(FTARunConfig.envDetails);
		servicePath = testData.get("hostIp");
		switch(testData.get("schema").toLowerCase()) {
			case "xml": accept =  "application/xml"; break;
			case "json": accept =  "application/json"; break;
		}
	}

	public WebServiceRequest(String rootId, String baseDirPath,String ... varArgs) throws Exception {
		ftaRunConfig = new FTARunConfig(rootId);
		ftaRunConfig.loadEnvironmentDetails(rootId, baseDirPath+"funclibraries/WebServiceArtifacts/EnvironmentDetails.xml");
		testData.put("password", "admin@123");
		testData.putAll(FTARunConfig.envDetails);
		servicePath = FTARunConfig.envDetails.get("hostIp");
		accept = "application/xml";
		if (varArgs.length==1){
			testData.put("billingid", varArgs[0]);
		}
		responseFilePath = baseDirPath + "funclibraries/WebServiceArtifacts/Response.xml";
	}

	public void execute() throws Exception {
		FTARunConfig ftaRunConfig = new FTARunConfig();
		ftaRunConfig.execute();
		Hashtable<String, Object> parametersObjectList = new Hashtable<String, Object>();
		
		testData.putAll(FTARunConfig.envDetails);
		
		servicePath = testData.get("hostIp");
		authToken = wsAuthenticationRequest(CustomerType.NORMAL, true);
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss");
		String customerName = "testflk_automation_" + sdf.format(date)+ "_001";
		
		Hashtable<String, String> paramsList = new Hashtable<String, String>();
		paramsList.put("accountType", "Customer");
		paramsList.put("accountName", customerName);
		paramsList.put("adminEmailAddress", testData.get("email"));
		paramsList.put("billingId", testData.get("resellerbillingid"));
		paramsList.put("password", testData.get("password"));
		
		parametersObjectList.put("parameters", paramsList);

		createRequest(WebServices.CreateCustomerAccount, MethodType.POST, testData.get("resellerbillingid"), parametersObjectList);
		String billingId = getPatternMatches("<billingId>(.+)</billingId>", responseBody, false);
		customerFile = new File("./TestData/Customer.properties");
		properties.load(new FileInputStream(customerFile));
		properties.setProperty("companyname", customerName);
		properties.setProperty("loginname", billingId+"_flkautomation");
		properties.setProperty("billingid", billingId);
		properties.store(new FileOutputStream(customerFile), null);
		System.out.println("Customer.properties file updated successfully...");
		
		System.out.println("3 Minutes Delay...for Copy To Customer");
		Thread.sleep(180000);
		System.out.println("Now procedding for APNS upload...");
		
		TestDataController testDataController = new TestDataController();
		testData.putAll(testDataController.getTestData("Customer"));
		
		File file1 = new File("./TestInputFiles/BaseData/apnscertificate.p12");
		File file2 = new File("./TestInputFiles/BaseData/apnscertificatepassword.txt");

		// Set request parameters
		Part[] part = {
				new StringPart("certPassword_xxx", "1"),
				new FilePart("appleMDMCertFile", file1, "multipart/form-data", "UTF-8"),
				new FilePart("certPassword", file2, "multipart/form-data", "UTF-8"),
		};
		
		parametersObjectList.put("parts", part);
		
		createRequest(WebServices.UploadAppleMDMCert, MethodType.POST, testData.get("billingid"), parametersObjectList);
		
		authToken = wsAuthenticationRequest(CustomerType.NORMAL, false);
		createRequest(WebServices.BasicSearch, MethodType.GET, testData.get("billingid"), parametersObjectList);
		
		ResponseValidator.validateResponse(WebServices.BasicSearch, testData.get("schema"));
		
	}
*/
	
	/**
	 * 
	 * @param method
	 * @param responseToVerify
	 * @return
	 * @throws Exception
	 */
	public boolean sendRequest(HttpMethod method, String responseToVerify) throws Exception {
		boolean isResponseVerified = false;
		try {
			statusCode = client.executeMethod(method);
			responseBody = method.getResponseBodyAsString();
			System.out.println("Request URL :: " + method.getURI());
			System.out.println("------------------------------------Begin Debug: Request Headers----------------------------------------------------------\n");
			Header[] requestHeaders = method.getRequestHeaders();
			for(int cn = 0;cn<requestHeaders.length;cn++) {
				System.out.println(requestHeaders[cn].toString());
			}
			System.out.println("------------------------------------Begin Debug: Response Headers----------------------------------------------------------\n");
			Header[] responseHeaders = method.getResponseHeaders();
			for(int cn = 0;cn<responseHeaders.length;cn++) {
				System.out.println(responseHeaders[cn].toString());
			}
			System.out.println("------------------------------------End Debug----------------------------------------------------------\n");
			if (statusCode != HttpStatus.SC_OK) {
				throw new Exception("POST method failed :: " + statusCode + " and ResponseBody :: " + responseBody);
			} else {
				System.out.println("------------------------------------Response Start----------------------------------------------------------\n");
				System.out.println(responseBody+"\n");
				System.out.println("------------------------------------Resoonse End----------------------------------------------------------");
				//Header[] headers = method.getResponseHeaders();
				if(null == jsessionId) {
					for(int cnt=0;cnt<responseHeaders.length;cnt++) {
//						System.out.println(headers[cnt].toString());
						if(responseHeaders[cnt].toString().contains("Set-Cookie: JSESSIONID=")) {
							jsessionId = getPatternMatches("JSESSIONID=(.+); Path", responseHeaders[cnt].toString(), false);
							System.out.println("JESSIONID: " + jsessionId);
							break;
						}
					}
				}
				if(responseBody.toLowerCase().contains(responseToVerify.toLowerCase())) {
//					System.out.println("RESPONSE VERIFIED...." + responseToVerify);
					isResponseVerified = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Exception in sendRequest method..." + e.getMessage());
		}
		return isResponseVerified;
	}
	
	/**
	 * 
	 * @param customerType
	 * @param isForCustomerCreation
	 * @return
	 * @throws Exception
	 */
//	public String wsAuthenticationRequest(CustomerType customerType, boolean isForCustomerCreation) throws Exception {
	public String wsAuthenticationRequest(String customerType, boolean isForCustomerCreation) throws Exception {
		String xml = null;
//		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		/*try {
			// Set request parameters
			if(isForCustomerCreation) {
				if(customerType.equals(CustomerType.NORMAL)) {
					params.put("billingID", testData.get("resellerbillingid"));
					params.put("platformID", testData.get("resellerplatformid"));
					params.put("appID", testData.get("resellerappid"));
					params.put("appAccessKey", testData.get("resellerappaccesskey"));
					params.put("userName", testData.get("resellerusername"));
					params.put("password", testData.get("resellerpassword"));
					params.put("appVersion", testData.get("resellerappversion"));
					postMethod = new PostMethod("https://" + servicePath + authenticationURI + testData.get("resellerbillingid"));
				} else if(customerType.equals(CustomerType.SPS)) {
					params.put("billingID", testData.get("spsresellerbillingid"));
					params.put("platformID", testData.get("spsresellerplatformid"));
					params.put("appID", testData.get("spsresellerappid"));
					params.put("appAccessKey", testData.get("spsresellerappaccesskey"));
					params.put("userName", testData.get("spsresellerusername"));
					params.put("password", testData.get("spsresellerpassword"));
					params.put("appVersion", testData.get("spsresellerappversion"));
					postMethod = new PostMethod("https://" + servicePath + authenticationURI + testData.get("spsresellerbillingid"));
				} else if(customerType.equals(CustomerType.MAM)) {
					params.put("billingID", testData.get("mamresellerbillingid"));
					params.put("platformID", testData.get("mamresellerplatformid"));
					params.put("appID", testData.get("mamresellerappid"));
					params.put("appAccessKey", testData.get("mamresellerappaccesskey"));
					params.put("userName", testData.get("mamresellerusername"));
					params.put("password", testData.get("mamresellerpassword"));
					params.put("appVersion", testData.get("mamresellerappversion"));
					postMethod = new PostMethod("https://" + servicePath + authenticationURI + testData.get("mamresellerbillingid"));
				} 
				xml = createAuthTemplateXML(params);
			} else {
//				if(customerType.equals(CustomerType.NORMAL)) {
				if(customerType.equals("NORMAL")) {
				
//					if(null == billingId) {
//						billingId = testData.get("billingid");
//					}
//					params.put("billingID", billingId);
//					params.put("appID", testData.get("iosappid"));
//					params.put("appAccessKey", testData.get("iosappaccesskey"));
//					params.put("userName", billingId+"_flkautomation");
//					params.put("password", testData.get("password"));
					postMethod = new PostMethod(url_auth + billingId + "/");
				} else if(customerType.equals(CustomerType.SPS)) {
					if(null == spsBillingId) {
						spsBillingId = testData.get("billingid");
					}
					spsBillingId = testData.get("spsbillingid");
					params.put("billingID", spsBillingId);
					params.put("appID", testData.get("spsresellerappid"));
					params.put("appAccessKey", testData.get("spsresellerappaccesskey"));
					params.put("userName", spsBillingId+"_flkautomation");
					params.put("password", testData.get("spspassword"));
					postMethod = new PostMethod("https://" + servicePath + authenticationURI + spsBillingId);
				} else if(customerType.equals(CustomerType.MAM)) {
					if(null == mamBillingId) {
						mamBillingId = testData.get("billingid");
					}
					mamBillingId = testData.get("mambillingid");
					params.put("billingID", mamBillingId);
					params.put("appID", testData.get("mamresellerappid"));
					params.put("appAccessKey", testData.get("mamresellerappaccesskey"));
					params.put("userName", mamBillingId+"_flkautomation");
					params.put("password", testData.get("mampassword"));
					postMethod = new PostMethod("https://" + servicePath + authenticationURI + mamBillingId);
				}
//				params.put("platformID", "3");
//				params.put("appVersion", "1");
				xml = createAuthTemplateXML(params);
			}*/
			postMethod = new PostMethod(url_auth + billingId + "/");
			xml = createAuthTemplateXML(params);
			
			System.out.println(xml);

			// Request Headers
			postMethod.addRequestHeader("Host", url);
			
			// Set Response Schema here
			postMethod.addRequestHeader("Accept", accept);
			
			// Request body if any
			RequestEntity requestEntity = new StringRequestEntity(xml, "application/xml", "UTF-8");
			postMethod.setRequestEntity(requestEntity);
			
			System.out.println("JSESSIONID: " + jsessionId);
			
			if(null != jsessionId) {
				postMethod.addRequestHeader("JSESSIONID", jsessionId);
			}

			// Send request and verify response
			if(sendRequest(postMethod, "")) {
				//switch(accept) {
				//	case "application/xml": authToken = getPatternMatches("<authToken>(.+)</authToken>", responseBody, false); break;
				//	case "application/json": authToken = getPatternMatches("\":\"(.+)\",\"", responseBody, false); break;
				//}
				authToken = getPatternMatches("<authToken>(.+)</authToken>", responseBody, false);
			} else {
				throw new Exception("Authentication Request failed... StatusCode :: " + statusCode + " and ResponseBody :: " + responseBody);
			}
//			writeResponse(WebServices.Authentication.toString());
		/*} catch(Exception e) {
			throw new Exception("Exception in wsAuthenticationRequest method..." + e.getMessage());
		}*/
		return authToken;
	}

	/**
	 * 
	 * @param webServiceName
	 * @param methodType
	 * @param params
	 */
	public void createRequest(String webServiceName, int methodType, String billingId, String ... strings ) {
		try {
			String apiVersion;
			if (strings.length>0){
				
			 if(strings[0].contentEquals("not-found")) {
				apiVersion="1.0";
			 }
			 else{
				 apiVersion= strings[0];
			 }
			}
			else{
				apiVersion="1.0";
			}
			String uri = url + webServiceName.replace("api-version",apiVersion ) + billingId;
			switch(methodType) {
				case 0: 
					// Setting PARAMETERS if any
					if(0 != parameters.size()) {
						uri = formulateGetURI(uri, parameters);
					}
					getMethod = new GetMethod(uri);
					// Setting HEADERS if any
					if(0 != headers.size()) {
						Enumeration<String> keys= headers.keys();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							getMethod.addRequestHeader(key, headers.get(key));
						}
					} 
					// DEFAULT HEADERS
					if(null != accept) {
						postMethod.addRequestHeader("Accept", accept);
					}
					getMethod.addRequestHeader("Authorization", "MaaS token=\""+authToken+"\"");
					System.out.println(">>>Sending GET Request");
					sendRequest(getMethod, "");
					break;
				case 1: postMethod = new PostMethod(uri);
					// Setting PARAMETERS if any
					if(0 != parameters.size()) {
						postMethod.setParams(setMethodParams(postMethod, parameters));
						
					}
					
					// Setting HEADERS if any
					if(0 != headers.size()) {
						Enumeration<String> keys= headers.keys();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							postMethod.addRequestHeader(key, headers.get(key));
						}
					} 
					// DEFAULT HEADERS
					if(null != accept) {
						postMethod.addRequestHeader("Accept", accept);
					}
					postMethod.addRequestHeader("Authorization", "MaaS token=\""+authToken+"\"");
					if(null != part) {
						postMethod.setRequestEntity(new MultipartRequestEntity(part, postMethod.getParams()));
					}
					System.out.println(">>>Sending POST Request");
					sendRequest(postMethod, "");
					break;
			default:
				break;
			}
			parameters.clear();
			headers.clear();
			part = null;
		} catch(Exception e) {
		}
	}
	
	/**
	 * 
	 * @param webServiceName
	 * @param methodType
	 * @param params
	 */
//	public void createRequest(WebServices webServiceName, MethodType methodType, String billingId, Hashtable<String, Object> parametersObjectList, String ...strings ) {
	public void createRequest(String webServiceName, int methodType, String billingId, Hashtable<String, Object> parametersObjectList, String ...strings ) {
		try {
			String apiVersion;
			if (strings.length>0){
				
			 if(strings[0].contentEquals("not-found")) {
				apiVersion="1.0";
			 }
			 else{
				 apiVersion= strings[0];
			 }
			}
			else{
				apiVersion="1.0";
			}
//			String uri = "https://" + servicePath + getWebServiceURI(webServiceName).replace("api-version",apiVersion ) + billingId;
			String uri = url + webServiceName.replace("api-version",apiVersion ) + billingId;
			
			initializeRequestHeadersAndParameters(parametersObjectList);
			switch(methodType) {
				case 0: 
					// Setting PARAMETERS if any
					if(0 != parameters.size()) {
						uri = formulateGetURI(uri, parameters);
					}
					getMethod = new GetMethod(uri);
					// Setting HEADERS if any
					if(0 != headers.size()) {
						Enumeration<String> keys= headers.keys();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							getMethod.addRequestHeader(key, headers.get(key));
						}
					} 
					// DEFAULT HEADERS
					if(null != accept) {
						postMethod.addRequestHeader("Accept", accept);
					}
					getMethod.addRequestHeader("Authorization", "MaaS token=\""+authToken+"\"");
					System.out.println(">>>Sending GET Request");
					sendRequest(getMethod, "");
					break;
				case 1: postMethod = new PostMethod(uri);
					// Setting PARAMETERS if any
					if(0 != parameters.size()) {
						postMethod.setParams(setMethodParams(postMethod, parameters));
						
					}
					
					//IF you need to send request body without any encoding or name pair values : like ws which involves bulk like GetCoreAttributesBulk.
					if(parametersObjectList.containsKey("body")){
						org.apache.commons.httpclient.methods.StringRequestEntity sre;
						@SuppressWarnings("unchecked")
						Hashtable<String, String> a = (Hashtable<String, String>) parametersObjectList.get("body");
					
						
						sre= new StringRequestEntity(a.get("payload"),"application/xml","UTF-8");
						postMethod.setRequestEntity(sre);
					}
					//End of the special case
					
					
					// Setting HEADERS if any
					if(0 != headers.size()) {
						Enumeration<String> keys= headers.keys();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							postMethod.addRequestHeader(key, headers.get(key));
						}
					} 
					// DEFAULT HEADERS
					if(accept != null)
					{postMethod.addRequestHeader("Accept", accept);
					}
					postMethod.addRequestHeader("Authorization", "MaaS token=\""+authToken+"\"");
					if(null != part) {
						postMethod.setRequestEntity(new MultipartRequestEntity(part, postMethod.getParams()));
					}
					System.out.println(">>>Sending POST Request");
					sendRequest(postMethod, "");
					break;
			default:
				break;
			}
//			writeResponse(webServiceName.toString());
		} catch(Exception e) {
			
		}
	}
	
	/* @params :  input xml and indentation you would like to achieve
	 * 
	 */
	public static String prettyFormatXML(String input, Integer indent) {
		try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        //transformerFactory.setAttribute("indent-number", indent);
	        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent.toString());
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e); 
	    }
	}
	
/*	public void writeResponse(String webServiceName) throws IOException {
		try {
			File file;FileWriter fileWriter = null;
			if (accept==null)accept="bytestream";
			switch(accept) {
				case "application/xml": 
					// If it is xml use the below method to pretty print.
					responseBody=prettyFormatXML(responseBody,new Integer("2"));
					if(null != responseFilePath) {
						file = new File(responseFilePath);
					} else {
						file = new File("./Response/XML/"+webServiceName+".xml");
					}
					// if it is json use another method and write to json file
					fileWriter = new FileWriter(file);
					fileWriter.write(responseBody);
					fileWriter.close();
					break;
				case "application/json": 
					ObjectMapper mapper=new ObjectMapper();
					Object json= mapper.readValue(responseBody,Object.class);
					file = new File("./Response/Json/"+webServiceName+".json");
					// if it is json use another method and write to json file
					fileWriter = new FileWriter(file);
					fileWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
					fileWriter.close();
					break;
				case "bytestream":
					file = new File("./Response/"+webServiceName);
					fileWriter = new FileWriter(file);
					fileWriter.write(responseBody);
					
			}
		} catch(Exception e){
			Reporter.log(e.getMessage(), true);
		}
	}*/
	
	/**
	 * 
	 * @param webServiceName
	 * @return
	 */
/*	public String getWebServiceURI(String webServiceName) {
		String webServiceURI = null;
		switch(webServiceName) {
		
			case Authentication: webServiceURI = authenticationURI; break;
			case CreateCustomerAccount: webServiceURI = createCustomerAccountURI; break;
			case BasicSearch: webServiceURI = searchURI; break;
			case UploadAppleMDMCert: webServiceURI = uploadAppleMDMCertURI; break;
			case AddITunesApp: webServiceURI = iTunesAppStoreAppURI; break;
			case AddIOSEnterpriseApp: webServiceURI = iOSEnterpriseAppURI; break;
			case AddIOSEnterpriseAppPlus: webServiceURI = AddIOSEnterpriseAppPlus;break;
			case DeleteApp: webServiceURI = DeleteApp; break;
			
			case Policies: webServiceURI = Policies;break;
			case SearchUser: webServiceURI = SearchUser;break;
			case AddPlayApp:webServiceURI = AddPlayAppURI; break;
			case GetCustomerConfig: webServiceURI = GetCustomerConfig; break;
			case ConvertToCustomer:webServiceURI =  ConvertToCustomer; break;
			case ExpireAccount: webServiceURI = ExpireAccount; break;
			case AddAndroidEnterpriseApp:webServiceURI =  AddAndroidEnterpriseAppURI; break;
			case GetAppDetails: webServiceURI = GetAppDetailsURI; break;
			case GetDeviceGroups: webServiceURI = GetDeviceGroupsURI;break;
			case SearchApp: webServiceURI = SearchAppURI;break;
			case GetWatchLists:webServiceURI = GetWatchListsURI; break;	
			case SetCustomerConfig: webServiceURI = SetCustomerConfig;break;
			case CheckAccountAvailability: webServiceURI = CheckAccountAvailability; break;
			case CreateAdministrator: webServiceURI = CreateAdministrator; break;
			case ExtendAccount: webServiceURI = ExtendAccount; break;
			case AuthenticateAdministrator: webServiceURI= AuthenticateAdministrator; break;
			case CheckAdminAccountAvailability: webServiceURI = CheckAdminAccountAvailability; break;

			case GetDeviceEnrollmentSettings: webServiceURI = GetDeviceEnrollmentSettings; break;
			case ConfigureDeviceEnrollSettings: webServiceURI= ConfigureDeviceEnrollSettings; break;
			case CreatePartnerAccount: webServiceURI =CreatePartnerAccount; break;
			case SetPartnerAccountConfig: webServiceURI = SetPartnerAccountConfig; break; 
			case GetPartnerAccountConfig: webServiceURI = GetPartnerAccountConfig; break;
			case GetSignedCSR: webServiceURI = GetSignedCSR; break;
			case SearchApps: webServiceURI = SearchAppsURI; break;
			case DistributeApp: webServiceURI = DistributeAppURI; break;
			case SearchDistributions: webServiceURI = SearchDistributionsURI ; break;
			case GetAppDistributionByDevice: webServiceURI = GetAppDistributionByDeviceURI; break;
			case LockDevice: webServiceURI = LockDeviceURI; break ;
			case LocateDevice: webServiceURI= LocateDeviceURI; break ;
			case WipeDevice: webServiceURI= WipeDeviceURI; break ;
			case SelectiveWipeDevice: webServiceURI = SelectiveWipeDeviceURI; break;
			case SendMessage: webServiceURI = SendMessageURI; break ;
			case GetCore: webServiceURI = GetCore; break;

			case EnrollDevice: webServiceURI = deviceEnrollURI;break;
			case ApproveDeviceMessagingSystem: webServiceURI = ApproveDeviceMessagingSystem;break;
			case GetSummaryAttributes: webServiceURI = GetSummaryAttributes; break;
			case GetCoreBulk: webServiceURI = GetCoreBulk; break;
			case HardwareInventory: webServiceURI = HardwareInventoryURI;break;
			case SoftwareInstalled: webServiceURI = SoftwareInstalledURI; break;
			case MdSecurityCompliance: webServiceURI = MdSecurityComplianceURI; break ;
			case SecurityApplications: webServiceURI = SecurityApplicationsURI; break;
			case BulkSummary: webServiceURI = BulkSummary; break;
			case PackageDistributionHistory: webServiceURI = PackageDistributionHistory; break;
			case Identity: webServiceURI = IdentityURI; break;
			case RevokeSelectiveWipe: webServiceURI = RevokeSelectiveWipe; break;
			case CancelPendingWipe: webServiceURI = CancelPendingWipe; break;
			case RemoveDevice: webServiceURI = RemoveDevice; break;
			case CheckActionStatus:webServiceURI= CheckActionStatus;break;
			case RefreshDeviceInformation: webServiceURI = RefreshDeviceInformationURI; break;
			case SearchActionHistory: webServiceURI=SearchActionHistory; break;
			case StopAppDistribution: webServiceURI = StopAppDistribution; break;
			case SetCustomAtribute: webServiceURI=SetCustomAtributeURI; break;

			case ManageDeviceEnrollments: webServiceURI = ManageDeviceEnrollments; break; 
			case SearchByDeviceGroup: webServiceURI = SearchByDeviceGroup; break;
			
			case DeviceActions: webServiceURI = DeviceActionsURI; break;
			case UpgradeApp: webServiceURI = UpgradeAppURI; break;
			case ResetDevicePasscode: webServiceURI = ResetDevicePasscodeURI; break;
			case DeviceDataView: webServiceURI = DeviceDataViewURI; break;
			case SearchByWatchList: webServiceURI = SearchByWatchList; break;
			case ChangeDevicePolicy: webServiceURI = ChangeDevicePolicy; break;

			case MdNetworkInformation: webServiceURI = MdNetworkInformation;break;
			case CellularDataUsage:webServiceURI= CellularDataUsage;break;

			case UpdateProvisioningProfile: webServiceURI = UpdateProvisioningProfileURI; break;
			case AppUploadRequestStatus: webServiceURI = AppUploadRequestStatus; break;

		}	
		return webServiceURI;
	}
	*/
	/**
	 * formulateGetURI method formulates the get request URI
	 * @param URI		uri to be formulated
	 * @param params	uri to be formulated using params
	 * @return			returns the formulated uri
	 */
	public String formulateGetURI(String URI, Hashtable<String, String> params) {
		String newURI = null;
		String postURI = "?";
		try {
			Enumeration<String> keys= params.keys();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				postURI = postURI + key +"="+params.get(key)+"&"; 
			}
			postURI=(String) postURI.subSequence(0, postURI.length()-1);
			newURI = URI + postURI.replaceAll(" ", "%20"); 
			//TO DO url encode
			
			
		} catch(Exception e) {
			
		}
		return newURI;
	}

	/**
	 * setMethodParams sets methods parameters
	 * @param method	method whose parameters to be set
	 * @param params	parameter list to be set
	 * @return	HttpMethodParams	Object of HttpMethodParams
	 */
	public HttpMethodParams setMethodParams(HttpMethod method, Hashtable<String, String> params) {
		HttpMethodParams httpMethodParams = new HttpMethodParams();
		Enumeration<String> keys= params.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			if(method instanceof PostMethod) {
				((PostMethod) method).setParameter(key, params.get(key));
				System.out.println(key + " :: " + params.get(key) + "\n");
			}
		}
		return httpMethodParams;
	}
	
	/**
	 * getAuthTemplateTag method returns the AuthTemplate Root tag 
	 * @return String root tag of AuthTemplate
	 */
	public String getAuthTemplateTag() {
		return ADMIN_ROOT_TAG;
	}
	
	/**
	 * createAuthTemplateXML creates AuthTemplate xml
	 * @param params HashMap of parameters for AuthTemplate
	 * @return	String AuthTemplate 
	 */
	public final String createAuthTemplateXML(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<authRequest>").append("\n").append("<").append(getAuthTemplateTag()).append(">").append("\n");

		for (String key : params.keySet()) {
			sb.append("<").append(key).append(">").append(params.get(key)).append("</").append(key).append(">").append("\n");
		}

		sb.append("</").append(getAuthTemplateTag()).append(">").append("\n").append("</authRequest>").append("\n");

		return sb.toString();
	}
	
	/**
	 * 
	 * @param patternToMatch
	 * @param response
	 * @param relpaceWhiteSpaces
	 * @return
	 */
	public String getPatternMatches(String patternToMatch, String response, boolean relpaceWhiteSpaces) {
		Pattern pattern = null;
		String patternValue = null;
		try {
			patternToMatch = "(?i)"+patternToMatch+"(?i)";
			if(relpaceWhiteSpaces) {
				response = response.replaceAll("\\s","");
			}

			pattern = Pattern.compile(patternToMatch);

			// Now create matcher object.
			Matcher matcher = pattern.matcher(response);
			while (matcher.find()) {
				patternValue = matcher.group(1);
				break;
			}
			if(null != patternValue) {
				if(patternValue.contains("</string>")) {
					patternValue = patternValue.split("</string>")[0];
				}
			} 
		} catch(Exception e) {
			System.out.println("Exception in getPatternMatches method..." + e.getMessage());
		}
		return patternValue;
	}
	
	@SuppressWarnings("unchecked")
	public void initializeRequestHeadersAndParameters(Hashtable<String, Object> objectList) {
		try {
			if(null != objectList) {
				if(0 != objectList.size()) {
					if(null != objectList.get("parameters")) {
						parameters.clear();
						parameters = (Hashtable<String, String>) objectList.get("parameters");
					}
					if(null != objectList.get("headers")) {
						headers.clear();
						headers = (Hashtable<String, String>) objectList.get("headers");
					}
				}
			}
		} catch(Exception e){
		}
	}
	
	/**
	 * 
	 * @param webServiceName
	 * @param methodType
	 * @param params
	 */
/*	public void createRequest(WebServices webServiceName, MethodType methodType, String billingId, String ... strings ) {
		try {
			String apiVersion;
			if (strings.length>0){
				
			 if(strings[0].contentEquals("not-found")) {
				apiVersion="1.0";
			 }
			 else{
				 apiVersion= strings[0];
			 }
			}
			else{
				apiVersion="1.0";
			}
			String uri = "https://" + servicePath + getWebServiceURI(webServiceName).replace("api-version",apiVersion ) + billingId;
			switch(methodType) {
				case GET: 
					// Setting PARAMETERS if any
					if(0 != parameters.size()) {
						uri = formulateGetURI(uri, parameters);
					}
					getMethod = new GetMethod(uri);
					// Setting HEADERS if any
					if(0 != headers.size()) {
						Enumeration<String> keys= headers.keys();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							getMethod.addRequestHeader(key, headers.get(key));
						}
					} 
					// DEFAULT HEADERS
					getMethod.addRequestHeader("Accept", accept);
					getMethod.addRequestHeader("Authorization", "MaaS token=\""+authToken+"\"");
					sendRequest(getMethod, "");
					break;
				case POST: postMethod = new PostMethod(uri);
					// Setting PARAMETERS if any
					if(0 != parameters.size()) {
						postMethod.setParams(setMethodParams(postMethod, parameters));
						
					}
//					//IF you need to send request body without any encoding or name pair values : like ws which involves bulk like GetCoreAttributesBulk.
//					if(parametersObjectList.containsKey("body")){
//						org.apache.commons.httpclient.methods.StringRequestEntity sre;
//						@SuppressWarnings("unchecked")
//						Hashtable<String, String> a = (Hashtable<String, String>) parametersObjectList.get("body");
//					
//						
//						sre= new StringRequestEntity(a.get("payload"),"application/xml","UTF-8");
//						postMethod.setRequestEntity(sre);
//					}
//					//End of the special case
					
					// Setting HEADERS if any
					if(0 != headers.size()) {
						Enumeration<String> keys= headers.keys();
						while(keys.hasMoreElements()) {
							String key = keys.nextElement();
							postMethod.addRequestHeader(key, headers.get(key));
						}
					} 
					// DEFAULT HEADERS
					if(null != accept) {
						postMethod.addRequestHeader("Accept", accept);
					}
					postMethod.addRequestHeader("Authorization", "MaaS token=\""+authToken+"\"");
					if(null != part) {
						postMethod.setRequestEntity(new MultipartRequestEntity(part, postMethod.getParams()));
					}
					sendRequest(postMethod, "");
					break;
			default:
				break;
			}
			writeResponse(webServiceName.toString());
			parameters.clear();
			headers.clear();
			part = null;
		} catch(Exception e) {
		}
	}*/
	/**
	   * createAuthTemplateXML creates AuthTemplate xml
	   * @param params HashMap of parameters for AuthTemplate
	   * @return String AuthTemplate
	   */
	public final String createTemplateXML(String xmlParent, Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(xmlParent).append(">").append("\n");

		for (String key : params.keySet()) {
			sb.append("<").append(key).append(">").append(params.get(key)).append("</").append(key).append(">").append("\n");
		}

		sb.append("</").append(xmlParent).append(">").append("\n");

		return sb.toString();
	}
}