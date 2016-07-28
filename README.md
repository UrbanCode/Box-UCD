# Box Utilities Plugin
***

![Box](http://static.appvn.com/i/uploads/thumbnails/122014/751821432ff9dda060e758d352540a0b-5-icon.png)

# About
***
**Plugin:** Box Utilities  
**Author:** Timothy Bula  
**Date:** 02/05/16

# Purpose
***
The Box Utiliies plugin provides integration with Box via Web Services, utilizing the Box Java-SDK. Version 1 of the the plugin has support for multi-step user authentication and file upload operations.


# Helpful Background Info
***
[Box Java SDK](https://github.com/box/box-java-sdk)   
[Box Content API Documentation](https://box-content.readme.io/reference "General overview")    
[Box Developer Account FAQ](https://box-content.readme.io/docs/developer-account-faq)



# Box Account Requirements
***
  
1. Box Developer Account [Request Form](https://app.box.com/signup/o/default_developer_offer) 
2. Box Developer Application [Create Application](https://www.box.com/developers/services)
3. Box App configuration - App Auth and App Users enabled + more [Guide](https://box-content.readme.io/docs/box-platform "Start of the box platform information")  


# Plugin Scripts
***
##boxSendAuthenticationCallEnterprise.groovy
***
Performs the first leg of OAuth2 authentication

###Parameters  

* **Entity ID** - enterpriseID on account info tab on Admin Console  
* **Client ID** - application’s client_id. Available on application settings webpage
* **Client Secret** - application’s client_secret. Available on application settings webpage
* **Public Key ID** - created when adding public key to application. 
* **Private Key** - generated locally 
* **Private Key Path** - private key must be added to the UCD step to create the file
* **Private Key Password** - set private key in settings if desired.

###Script  

1. Creates Enterprise JWT assertion with provided information from Box app setting  
2. Requests  enterprise authentication  
3. Prints enterprise auth token to the log 
 

##boxCreateAppUser.groovy
***
Retrieves/creates the App User

###Parameters 

* **Enterprise Auth Token** - enterprise auth token from first leg of authentication
* **App User Name** - name of app user on the developer enterprise account. Will be created on the enterprise account if not found. Needed for Content API

###Script 
 
1. Establishes API connection to Box with enterprise auth token
2. Seaches for provided App User name and finds match
3. If cannot find App User, it creates a new one with provided name
4. Prints found/created App User ID to the log


##boxSendAuthentcationCallAppUser.groovy
***
Performs second leg of OAuth2 authentication and allows Content API to be used

###Parameters###

* **App User ID** - ID of the App User. Retrieved in previous script. Can be manually entered.   
* **Client ID** - application’s client_id. Available on application settings webpage
* **Client Secret** - application’s client_secret. Available on application settings webpage
* **Public Key ID** - created when adding public key to application. 
* **Private Key** - generated locally 
* **Private Key Path** - private key must be added to the UCD step to create the file
* **Private Key Password** - set private key in settings if desired.

###Script 
  
1. Uses App User Id to create User JWT assertion  
2. Requests authentication  
3. Prints auth token to log


##boxCreateFolder.groovy
***
Locates/creates folder under specified parent folder

###Parameters###

* **App User Auth Token** - auth token necessary for Box API calls
* **Folder** - folder to be created or found
* **Parent Folder ID** - ID of parent folder. Can be found on the Box web app in the url when inside the folder
* **Parent Folder** - name of parent folder for folder to be located/created within. Will not necessarily find the correct folder. ID is preferred method for specifying folder 

###Script 

1. Establishes connection to Box with app user auth token  
2. Uses one of three methods based on what information is passed to it: Parent Folder ID, Parent Folder Name, Nothing
3. (a) Locates parent folder with the ID. Looks for the new folder name passed to it. Creates folder if cannot find it
4. (b) Searches Box account for parent folder by name. Looks for new folder name passed to it. Creates the new folder if cannot find it. Does not guarentee the correct folder is found if multiple folders have the same name in the account.
5. (c) No parent folder info is passed to it, so it uses the Box root folder as the parent folder. Looks for new folder in it and creates one if it doesn't find it. 
6. Prints found/created folder id to the log

##boxUploadFile.groovy
***
Uploads file to box account

###Parameters

* **App User Auth Token** - auth token necessary for Box API calls
* **File Path** - folder to be created or found
* **File Name** - name of the file to be uploaded. Optional and defaults to file name from File Path
* **Parent Folder ID** - ID of parent folder for file to be uploaded to. Can be found on the Box web app in the url when inside the folder 

###Script 

1. Establishes connection to Box with auth token  
2. Performs preflight check on file to be upload (file size, naming conflicts, etc.)
3. If preflight check fails because of naming collision, find existing file in the parent folder and attempt to upload new version
4. If preflight check is successful, upload file to parent folder 
5. Prints uploaded file id to the log

##boxGetFileDownloadUrl.groovy
***
Gets the direct download url for a file on box

###Parameters

* **App User Auth Token** - auth token necessary for Box API calls   
* **File ID** - ID of file to get the file downloadurl of

###Script 

1. Establishes connection to Box with auth token  
2. Finds file by id
3. Creates sharedlink and gets direct download url
4. Prints direct download url to the log
  

