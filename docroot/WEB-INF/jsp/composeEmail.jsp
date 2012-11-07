<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ page contentType="text/html" isELIgnored="false" %>
<style type="text/css">
label {
font-size: 16pt;
padding-bottom: 10px;
}
</style>
<script type="text/javascript">
//<!--
function showHideLogos(){

	if ( jQuery("#includeLogo").is(":checked") )
	{
		//show the hidden div
		jQuery("#logo").show();
	}
	else
	{
		jQuery("#logo").hide();
	}
}
//-->
</script>
<portlet:actionURL var="sendEmail" name="sendEmail"/>
<portlet:renderURL var="reset" /> 
<c:if test="${msg ne null}">
	<div class="portlet-msg-success">${msg}</div>
</c:if>
<c:if test="${errormsg ne null}">
	<div class="portlet-msg-error">${errormsg}</div>
</c:if>
<form:form id="composemail" modelAttribute="message" action="${sendEmail}" enctype="multipart/form-data">
	<fieldset>
		<div id="email">
			<form:label path="recipients" cssStyle="display:block">To:</form:label>
			<form:select id="recipients" path="recipients" multiple="multiple" title="Click to Add a Recipient">
				<c:forEach items="${possibleToAddresses}" var="addressOption" varStatus="addressStatus">
					<spring:bind path="recipients[${addressStatus.index}]">
					<form:option label="${addressOption.label}" value="${addressOption.address}" />
					</spring:bind> 
				</c:forEach>
			</form:select> 
			<form:errors path="recipients" cssClass="portlet-msg-error" />		
							
		</div>
		<br/>
		
		<c:if test="${sendAsDean}">
			<div>
				<form:checkbox path="sendAsDean" value="unchecked"  label=" Send on behalf of Dean Lynch"/>				
			</div>
			<br/>
		</c:if>			
		<div>
			<form:label path="copyTo">CC (optional):</form:label>
			<form:input path="copyTo" size="100"/>
			<br/><p style="font-size:small;">Use a semicolon to separate email addresses.</p>
			<form:errors path="copyTo" cssClass="portlet-msg-error" />
		</div>
		<br/>
		<div>
			<form:label path="replyTo">Reply To (optional):</form:label>
			<form:input path="replyTo" size="100"/>
			<br/><p style="font-size:small;">Use a semicolon to separate email addresses.</p>
			<form:errors path="replyTo" cssClass="portlet-msg-error" />
		</div>
		<br/>
		<div>
			<form:label path="subject" cssStyle="display:block">Subject:</form:label>
			<form:input id="subject" path="subject" size="150"/>
			<form:errors path="subject" cssClass="portlet-msg-error" />
		</div>
		<br/>
		

	    <form:checkbox path="includeLogo" id="includeLogo" value="${includeLogo}" label=" Include logo in email" 
	  	onclick="showHideLogos()"/>
	  	
		<br/>
		<br/>
		<div id="logo">		
		  <form:radiobutton path="logo" id="logo1" value="logo1" label="Logo 1" />&nbsp;
		  <form:radiobutton path="logo" id="logo2" value="logo2" label="Logo 2"/>&nbsp;
		</div>
				
		<br/>
		<div>
			<form:label path="body" cssStyle="display:block">Body:</form:label>
			<spring:bind path="message.body"><form:textarea name="body" path="body" id="body" /></spring:bind>
			<form:errors path="body" cssClass="portlet-msg-error" />
		</div>
		
		<br/>
		<div>
			<form:label path="attachments" cssStyle="display:block">Add Attachments (optional):</form:label>
			<form:errors path="attachments" cssClass="portlet-msg-error" />
			<div id="attachmentList">
			</div>
			<input type="button" value="Add Another Attachment" onclick="addAttachmentField()" style="margin-left:5px;" />
		</div>

		<br/>
		<div>
			<input type="submit" name="send" value="Send" onclick="setSubmitType('send')" />&nbsp;<input type="button" id="preview" name="preview" value="Send me Preview" onclick="sendPreview()"/>&nbsp;<a href="${reset}"><Strong>Reset</Strong></a>
			<input type="hidden" id="submitType" name="submitType">
		</div>


	</fieldset>
</form:form>

    <div id="dialog" style="display:none" >
		<div id="dialog-message" title="Preview sent">
			<p>
				Preview email sent.
			</p>
		</div>
		<div id="dialog-subject-error" title="Error">
			<p>
				A subject is required.
			</p>
		</div>
		<div id="dialog-body-error" title="Error">
			<p>
				Please enter a message in the body.
			</p>
		</div>
	</div>	
<script type="text/javascript" >
//<![CDATA[
function addAttachmentField() {   
	count = jQuery(".attachments").length;
	newFieldId = "attachments" + count;
	field = "<div class='" + newFieldId + "'><input  class='attachments' type='file' id='" + newFieldId + "' name='attachments[" + count+ "]' /><input type='button' onclick='removeAttachmentField(&quot;" + newFieldId + "&quot;)' value='Remove' style='margin-left:5px;'><br/></div>";
	jQuery("#attachmentList").append(field);
}
function removeAttachmentField(id) {
	jQuery("." + id).remove();
}

addAttachmentField();

function setSubmitType(submitType){	
	jQuery("#submitType").val(submitType);
}

function sendPreview(){	
	setSubmitType("preview");
	for ( instance in CKEDITOR.instances )
    {
        CKEDITOR.instances[instance].updateElement();
    }
	if(jQuery("#subject").val().length == 0){
		showMessage("dialog-subject-error");
		return;
	}
	if(jQuery("#body").val().length == 0){
		showMessage("dialog-body-error");
		return;
	}
	
	list = jQuery(":file");	
	var listofobjects = jQuery(".attachments");
	var count = listofobjects.length;
	var attachmentPresent = false;
	if(count > 0){
		var i;
		for(i=0; i< count; i++){
			if(listofobjects.eq(i).val().length !=0 ){
				attachmentPresent = true;
				break;
			}
		} 
		if(attachmentPresent === true){
			jQuery("#composemail").ajaxSubmit({ 
				url: '<%=sendEmail%>',
				dataType:  'xml', 
				success: function(response){
					showMessage("dialog-message");
				}
			}); 
		}else {
			jQuery.post('<%=sendEmail%>', 
					jQuery("#composemail").serialize(), 
					function(response){
						showMessage("dialog-message");
		    		});
		}
	}else {
		jQuery.post('<%=sendEmail%>', 
				jQuery("#composemail").serialize(), 
				function(response){
					showMessage("dialog-message");
	    		});
	}

}

function showMessage(dialogname){
	jQuery("#" + dialogname).dialog({
        modal: true,
		buttons: { "Ok": function() { 
			jQuery(this).dialog("close"); } } 
	});
}

jQuery(document).ready(function() {
	CKEDITOR.replace( 'body' );

	jQuery("#recipients").asmSelect({
				addItemTarget: 'bottom',
				animate: true,
				highlight: true,
				sortable: true
      });
	
	showHideLogos();
	
} );



//]]>
</script>
  