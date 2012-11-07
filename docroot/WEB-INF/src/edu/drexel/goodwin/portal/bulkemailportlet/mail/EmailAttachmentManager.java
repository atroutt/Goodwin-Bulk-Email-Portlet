package edu.drexel.goodwin.portal.bulkemailportlet.mail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.portlet.util.PortletUtils;

@Service
public class EmailAttachmentManager {

	private final Logger logger = Logger.getLogger(this.getClass());

	public void addOneUploadedFileAsAttachment(MultipartFile uploadedFile, MimeMessageHelper helper, PortletRequest request) throws IOException {
		if (uploadedFile == null || uploadedFile.isEmpty()) {
			logger.debug("empty or null file");
			return;
		}

		File tempFile = createTempFileForAttachment(request, uploadedFile);
		uploadedFile.transferTo(tempFile);
		try {
			helper.addAttachment(tempFile.getName(), tempFile);
		} catch (MessagingException e) {
			logger.error("Error adding attachment", e);
		}
		logger.debug("added attachment");
	}

	public void addAllUploadedFilesAsAttachments(Message message, PortletRequest request, MimeMessageHelper helper) throws IOException {
		final List<MultipartFile> attachments = message.getAttachments();
		if (attachments == null || attachments.size() == 0) {
			logger.debug("no attachments found");
			return;
		}

		for (MultipartFile uploadedFile : attachments) {
			addOneUploadedFileAsAttachment(uploadedFile, helper, request);
		}
	}

	private File createTempFileForAttachment(PortletRequest request, MultipartFile uploadedFile) throws IOException {
		String filenameWithoutPath = getOriginalFileNameWithoutPath(uploadedFile);
		File tempDir = PortletUtils.getTempDir(request.getPortletSession().getPortletContext());

		logger.debug("creating temp file for " + filenameWithoutPath);

		return new File(tempDir, filenameWithoutPath);
	}

	private String getOriginalFileNameWithoutPath(MultipartFile uploadedFile) {
		String originalFilename = uploadedFile.getOriginalFilename();

		String[] tokens = originalFilename.split("[/|\\\\]");

		if (tokens == null || tokens.length == 0) {
			logger.error("unparsable filename: " + originalFilename);
			throw new RuntimeException("unparsable filename: " + originalFilename);
		}

		return tokens[tokens.length - 1];
	}

}
