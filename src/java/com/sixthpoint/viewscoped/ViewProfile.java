package com.sixthpoint.viewscoped;

import com.sixthpoint.utils.FacesUtil;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Sample profile page
 *
 * @author sixthpoint
 */
@ManagedBean
@ViewScoped
public class ViewProfile implements Serializable {

    private String firstName;
    private String lastName;

    /**
     * Basic constructor
     */
    public ViewProfile() {

    }

    /**
     * On submit, we queue a success message
     *
     * @return
     */
    public String submit() {

        FacesUtil.queueMessage("we have sucess", "success");
        FacesUtil.queueMessage("another success", "success");
        return "welcome?faces-redirect=true";

    }

    /**
     * Returns a array of validation options
     *
     * @return
     */
    public List<String> validationOptions() {
        return FacesUtil.getErrorCodes();
    }

    /**
     * Ask for validation messages based on a formId in the component tree
     * (success, warning, danger)
     *
     * @param formId
     * @param level
     * @return
     */
    public Collection<String> validation(String formId, String level) {
        return FacesUtil.getMessages(formId, level);
    }

    /**
     * Get all messages based on no form id
     *
     * @param level
     * @return
     */
    public Collection<String> validation(String level) {
        return FacesUtil.getMessages(level);
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
