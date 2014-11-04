package com.sixthpoint.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

public class FacesUtil {

    /**
     * Failure message
     */
    public static final String MESSAGE_FAILURE = "danger";

    /**
     * Warning message
     */
    public static final String MESSAGE_WARNING = "warning";

    /**
     * Success message
     */
    public static final String MESSAGE_SUCCESS = "success";

    /**
     * Information message
     */
    public static final String MESSAGE_INFO = "info";

    /**
     * This is our session name
     */
    private static final String sessionToken = "MULTIPAGE-MESSAGES";

    /**
     * Options allowed for errors, taken from bootstrap classes
     */
    private static List<String> errorCodes = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("danger");
            add("warning");
            add("success");
        }
    };

    /**
     * Saves all messages queued to the sessionToken
     *
     * @param facesContext
     */
    private static void saveMessages(final FacesContext facesContext) {

        // get existing messages map from session
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        HashMap<String, List<FacesMessage>> messagesByIDMap = (HashMap<String, List<FacesMessage>>) sessionMap.get(sessionToken);

        // If it doesn't exist make one
        if (messagesByIDMap == null) {
            messagesByIDMap = new HashMap<>();
            sessionMap.put(sessionToken, messagesByIDMap);
        }

        // Iterate through all messages that are waiting in queue
        for (Iterator<String> iter = facesContext.getClientIdsWithMessages(); iter.hasNext();) {

            String clientId = iter.next();
            List<FacesMessage> messageList = messagesByIDMap.get(clientId);

            // If no messages for client ID yet create empty arrayList
            if (messageList == null) {
                messageList = new ArrayList<>();
                messagesByIDMap.put(clientId, messageList);
            }

            Iterator<FacesMessage> facesIterator = facesContext.getMessages(clientId);

            // Add all the messages for that client ID to the session
            while (facesIterator.hasNext()) {
                messageList.add(facesIterator.next());
            }
        }
    }

    /**
     * Finds all the messages in the sessionToken and adds them
     *
     * @param facesContext
     */
    private static void restoreMessages(final FacesContext facesContext) {

        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        HashMap<String, List<FacesMessage>> messagesByIDMap = (HashMap<String, List<FacesMessage>>) sessionMap.remove(sessionToken);

        if (messagesByIDMap == null) {
            return;
        }

        // Add all messages by client ID back to the facesContext
        for (String clientID : messagesByIDMap.keySet()) {
            List<FacesMessage> messages = messagesByIDMap.get(clientID);
            for (FacesMessage facesMessage : messages) {
                facesContext.addMessage(clientID, facesMessage);
            }
        }
    }

    /**
     * The default way to queue a message, it takes a error level of "warning"
     *
     * @param str
     */
    public static void queueMessage(String str) {
        queueMessage(str, "warning");
    }

    /**
     * Adds a message to the faces queue depending on the level of severity
     * defined (danger, warning, success)
     *
     * @param str
     * @param severity
     */
    public static void queueMessage(String str, String severity) {

        // Did they enter a valid severity
        if (!errorCodes.contains(severity)) {
            Logger.getLogger(FacesUtil.class.getName()).log(Level.SEVERE, null, "Improper api usage");
            return;
        }

        FacesMessage message = new FacesMessage(str);

        switch (severity) {
            case "danger":
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                break;
            case "warning":
                message.setSeverity(FacesMessage.SEVERITY_WARN);
                break;
            default:
                message.setSeverity(FacesMessage.SEVERITY_INFO);
                break;
        }

        FacesContext.getCurrentInstance().addMessage(UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getClientId(), message);
        saveMessages(FacesContext.getCurrentInstance());
    }

    /**
     * Gets all messages based on severity (danger, warning, success)
     *
     * @param severity
     * @return
     */
    public static Collection<String> getMessages(String severity) {
        return getMessages("", severity);
    }

    /**
     * Gets all messages in a array list of a given severity type. Specify
     * severity as "error", "warn", or "info"
     *
     * @param formId
     * @param severity
     * @return
     */
    public static Collection<String> getMessages(String formId, String severity) {

        // Get the Faces Context
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // Restore other messages
        restoreMessages(facesContext);

        // Get the root of the UIComponent
        UIViewRoot root = facesContext.getViewRoot();

        // Iterator to iterate through the client ID's
        Iterator<String> iterator = facesContext.getClientIdsWithMessages();

        // Map all the messages by error level
        Map<String, Collection<String>> map = new HashMap<>();

        // Did they enter a valid severity
        if (!errorCodes.contains(severity)) {
            Logger.getLogger(FacesUtil.class.getName()).log(Level.SEVERE, null, "Improper api usage");
            return map.get(severity);
        }

        // Determine error categories
        String errorLevel = "";
        switch (severity) {
            case "danger":
                errorLevel = "ERROR 2";
                break;
            case "warning":
                errorLevel = "WARN 1";
                break;
            default:
                errorLevel = "INFO 0";
                break;
        }

        // Iterate through the messages
        while (iterator.hasNext()) {

            // Retrieve the client ID
            String clientId = iterator.next();

            // Retrieve the item UI component
            UIComponent baseComponent = root.findComponent(clientId);

            // Find the the form UI component
            UIComponent parentForm = findParentForm(baseComponent);

            // If the form ID was specified and its not the true parent, we skip this iteration
            if (!formId.equals("") && (parentForm == null || (!formId.equals("") && !parentForm.getId().equals(formId)))) {
                continue;
            }

            // If the formId is not specified we always display it as a global message
            if (formId.equals("") || baseComponent != null) {

                // Return an Iterator over the FacesMessages
                Iterator<FacesMessage> facesIterator = facesContext.getMessages(clientId);

                // Build a list to return based on type specified
                while (facesIterator.hasNext()) {

                    FacesMessage facesMessage = facesIterator.next();
                    Collection<String> values = map.get(errorLevel);
                    if (values == null) {
                        values = new ArrayList<>();
                        map.put(errorLevel, values);
                    }

                    // Only add if we don't have that value already
                    if (facesMessage.getSeverity().toString().equals(errorLevel) && !values.contains(facesMessage.getDetail())) {
                        values.add(facesMessage.getDetail());
                    }
                }
            }
        }

        return map.get(errorLevel);
    }

    /**
     * Finds if the component belongs to a form
     *
     * @param component
     * @return
     */
    public static UIComponent findParentForm(UIComponent component) {
        if (component instanceof UIForm) {
            return component;
        }
        if (component == null) {
            return null;
        }
        return findParentForm(component.getParent());
    }

    /**
     * Get all error codes
     *
     * @return the errorCodes
     */
    public static List<String> getErrorCodes() {
        return Collections.unmodifiableList(errorCodes);
    }

    /**
     * Set a list of error codes
     *
     * @param errors
     */
    public static void setErrorCodes(List<String> errors) {
        errorCodes = errors;
    }

}
