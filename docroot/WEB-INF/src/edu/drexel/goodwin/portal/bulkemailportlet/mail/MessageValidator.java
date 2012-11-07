package edu.drexel.goodwin.portal.bulkemailportlet.mail;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component(value = "validator")
public class MessageValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Message.class.equals(clazz);
	}

	@Override
	public void validate(Object o, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "subject", "message.subject.blank", "A subject is required.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "body", "message.body.blank", "Please enter a message in the body.");

		Message message = (Message) o;
		if (CollectionUtils.isEmpty(message.getRecipients())) {
			errors.rejectValue("recipients", "message.toAddress.blank", "Please select at least one recipient.");
		}
	}

}
