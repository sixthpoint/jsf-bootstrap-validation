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
     * This is our session name
     */
    private static final String sessionToken = "MULTIPAGE-MESSAGES";

    /**
     * Options allowed for errors, taken from bootstrap classes
     */
    private static List<String> errorCodes = new ArrayList<String>() {
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
        HashMap<String, FacesMessage> messages = new HashMap<>();
        for (Iterator<String> iter = facesContext.getClientIdsWithMessages(); iter.hasNext();) {

            String clientId = iter.next();
            Iterator<FacesMessage> facesIterator = facesContext.getMessages(clientId);

            while (facesIterator.hasNext()) {
                messages.put(clientId, facesIterator.next());
                iter.remove();
            }
        }

        if (messages.isEmpty()) {
            return;
        }

        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        HashMap<String, FacesMessage> existingMessages = (HashMap<String, FacesMessage>) sessionMap.get(sessionToken);
        if (existingMessages != null) {
            existingMessages.putAll(messages);
        } else {
            sessionMap.put(sessionToken, messages);
        }
    }

    /**
     * Finds all the messages in the sessionToken and adds them
     *
     * @param facesContext
     */
    private static void restoreMessages(final FacesContext facesContext) {
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        HashMap<String, FacesMessage> messages = (HashMap<String, FacesMessage>) sessionMap.remove(sessionToken);

        if (messages == null) {
            return;
        }

        Iterator it = messages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            Object fm = pairs.getValue();
            facesContext.addMessage(pairs.getKey().toString(), (FacesMessage) fm);
            it.remove();
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
