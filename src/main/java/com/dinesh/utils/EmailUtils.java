package com.dinesh.utils;

import java.io.FileInputStream;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtils {

	@Autowired
	private JavaMailSender mailSender;
	private Logger logger = LoggerFactory.getLogger(EmailUtils.class);

	public boolean sendEmail(String to, String subject, String body, FileInputStream fis) {
		boolean isMailSent = false;
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);
			helper.addAttachment("planDtls.pdf", new ByteArrayResource(IOUtils.toByteArray(fis)));
		
			isMailSent = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isMailSent;
	}
}
