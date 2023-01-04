package com.axis.MovieTicket.service;

import javax.mail.MessagingException;

//import javax.mail.MessagingException;

public interface MailService {

    void sendMail(final String to, final String subject, final String text,
                  final boolean isHtmlContent) throws MessagingException;
}