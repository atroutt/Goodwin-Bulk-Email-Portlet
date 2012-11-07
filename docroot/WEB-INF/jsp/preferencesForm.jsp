<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ page contentType="text/html" isELIgnored="false"%>
<portlet:defineObjects />
<h1>Edit Email Recipient Options</h1>
<portlet:actionURL var="addAddressUrl" name="addAddress" />
<portlet:actionURL var="savePermissionsUrl" name="savePermissions" />
<style>
table.prefsTable {

	border-collapse: collapse;
}
.prefsTable, .prefsTable th, .prefsTable td {
	border: 1px solid black;
	padding: 5px;
}
</style>


<form:form modelAttribute="permissions" action="${savePermissionsUrl}" enctype="multipart/form-data">
<input type="submit" value="Save Changes" />&nbsp;<input type="reset" value="Reset" />
<br/>
<br/>
<table class="prefsTable">
	<tr>
		<td></td>
		<c:forEach items="${people}" var="user" varStatus="index">
			<th>${user.fullName}  (${user.screenName})</th>
		</c:forEach>
	</tr>
	<c:forEach items="${emailAddresses}" var="emailAddressOption" varStatus="index">
		<tr> 
			<td>
			    <p>${emailAddressOption.label} 
			    <c:if test="${emailAddressOption.label!= sendAsDeanLabel}">	
			    	&lt;${emailAddressOption.address}&gt;
			    </c:if>	
				    <input type="hidden" name="id" id="id" value="${emailAddressOption.id}">
				    <input type="hidden" name="label" id="label" value="${emailAddressOption.label}">
				    <input type="hidden" name="address" id="address" value="${emailAddressOption.address}">
				    
				    <c:if test="${emailAddressOption.label!= sendAsDeanLabel}">	
				    <a href="<portlet:actionURL>
								<portlet:param name="myaction" value="removeAddress" />		
								<portlet:param name="addressId" value="${emailAddressOption.id}" />
								<portlet:param name="addresslabel" value="${emailAddressOption.label}" />
								<portlet:param name="addressEmail" value="${emailAddressOption.address}" />				
								</portlet:actionURL>" onclick="return confirm('Are you sure you want to delete this address?');">Remove</a>	
						</c:if>
				 	</p>				    
				
			</td>
		
			
			<c:forEach items="${people}" var="user" varStatus="index">
			<td align="center">
			<spring:bind path="permissions.map[${emailAddressOption.id}][${user.screenName}].isAuthorized">
			  <form:checkbox path="${status.expression}" value="${status.value}"/>
			  </spring:bind>
			  </td>
			 </c:forEach>
		</tr>
	</c:forEach>
</table>
<br/>
<input type="submit" value="Save Changes"/>&nbsp;<input type="reset" value="Reset" />
</form:form>

<h2>Add Address</h2>

<form:form modelAttribute="emailAddress" action="${addAddressUrl}" enctype="multipart/form-data">
	<div>
		<form:label path="label" cssStyle="display:block">Label:</form:label>
		<form:input path="label" id="label" size="50" />
		<form:errors path="label" cssClass="portlet-msg-error" />
	</div>
	<br />
	<div>
		<form:label path="address" cssStyle="display:block">Address:</form:label>
		<form:input path="address" id="address" size="50" />
		<form:errors path="address" cssClass="portlet-msg-error" />
	</div>
	<br />
	<div>
		<input type="submit" value="Add" />
	</div>
</form:form>
