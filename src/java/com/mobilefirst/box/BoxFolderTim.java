package com.box.sdk;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Represents a folder on Box. This class can be used to iterate through a folder's contents, collaborate a folder with
 * another user or group, and perform other common folder operations (move, copy, delete, etc.).
 *
 * <p>Unless otherwise noted, the methods in this class can throw an unchecked {@link BoxAPIException} (unchecked
 * meaning that the compiler won't force you to handle it) if an error occurs. If you wish to implement custom error
 * handling for errors related to the Box REST API, you should capture this exception explicitly.</p>
 */
public class BoxFolderTim extends BoxFolder {
    /**
     * An array of all possible folder fields that can be requested when calling {@link #getInfo()}.
     */
    public static final String[] ALL_FIELDS = {"type", "id", "sequence_id", "etag", "name", "created_at", "modified_at",
        "description", "size", "path_collection", "created_by", "modified_by", "trashed_at", "purged_at",
        "content_created_at", "content_modified_at", "owned_by", "shared_link", "folder_upload_email", "parent",
        "item_status", "item_collection", "sync_state", "has_collaborations", "permissions", "tags",
        "can_non_owners_invite"};

    private static final URLTemplate CREATE_FOLDER_URL = new URLTemplate("folders");
    private static final URLTemplate COPY_FOLDER_URL = new URLTemplate("folders/%s/copy");
    private static final URLTemplate DELETE_FOLDER_URL = new URLTemplate("folders/%s?recursive=%b");
    private static final URLTemplate FOLDER_INFO_URL_TEMPLATE = new URLTemplate("folders/%s");
    private static final URLTemplate UPLOAD_FILE_URL = new URLTemplate("files/content");
    private static final URLTemplate ADD_COLLABORATION_URL = new URLTemplate("collaborations");
    private static final URLTemplate GET_COLLABORATIONS_URL = new URLTemplate("folders/%s/collaborations");
    private static final URLTemplate GET_ITEMS_URL = new URLTemplate("folders/%s/items/");
    private static final URLTemplate SEARCH_URL_TEMPLATE = new URLTemplate("search");

    /**
     * Constructs a BoxFolder for a folder with a given ID.
     * @param  api the API connection to be used by the folder.
     * @param  id  the ID of the folder.
     */
    public BoxFolderTim(BoxAPIConnection api, String id) {
        super(api, id);
    }

     /**
     * Gets the current user's root folder.
     * @param  api the API connection to be used by the folder.
     * @return     the user's root folder.
     */
    public static BoxFolderTim getRootFolderTim(BoxAPIConnection api) {
        return new BoxFolderTim(api, "0");
    }

    /**
     * Checks if the file can be successfully uploaded by using the preflight check.
     * @param  name        the name to give the uploaded file.
     * @param  fileSize    the size of the file used for account capacity calculations.
     */

    public BoxAPIResponse canUploadTim(String name, long fileSize) {
        URL url = UPLOAD_FILE_URL.build(this.getAPI().getBaseURL());
        BoxJSONRequest request = new BoxJSONRequest(this.getAPI(), url, "OPTIONS");

        JsonObject parent = new JsonObject();
        parent.add("id", this.getID());

        JsonObject preflightInfo = new JsonObject();
        preflightInfo.add("parent", parent);
        preflightInfo.add("name", name);

        preflightInfo.add("size", fileSize);

        request.setBody(preflightInfo.toString());
        BoxAPIResponse response = request.send();
        response.disconnect();
        return response;
    }
}
