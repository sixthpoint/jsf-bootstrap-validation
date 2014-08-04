jsf-bootstrap-validation
========================

Displaying errors with JSF 2.2 and Bootstrap CSS the simple way.

Background
------------

A project called for multiple forms on a page with different areas being validated. This class was made to create a very simply way to display errors based on formId. Bootstrap 3 was used to highlight invalid fields and render errors.

Usage
------------

In some backing java class queue up a message.


```java
FacesUtil.queueMessage("we have sucess", "success");
```


Then loop through all errors displaying them to the end user on your JSF page.

```html
<h:panelGroup layout="block" id="messages">
            <c:forEach var="errorCode" items="#{viewProfile.validationOptions()}">
                <h:panelGroup layout="block" rendered="#{viewProfile.validation('profileForm', errorCode).size() gt 0}">
                    <h:panelGroup layout="block" styleClass="alert alert-#{errorCode} alert-dismissable">
                        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&#xD7;</button>
                        <ul>
                            <c:forEach var="m" items="#{viewProfile.validation('profileForm', errorCode)}">
                                <li>#{m}</li>
                            </c:forEach>
                        </ul>
                    </h:panelGroup>
                </h:panelGroup>
            </c:forEach>
</h:panelGroup>
```

A full explanation of the code is available at: [http://blog.sixthpoint.com/jsf-bootstrap-style-validation/](http://blog.sixthpoint.com/jsf-bootstrap-style-validation/)
