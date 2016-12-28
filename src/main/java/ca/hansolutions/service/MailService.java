package ca.hansolutions.service;

import java.util.List;

/**
 * Created by Nate on 2016-12-17.
 */
public interface MailService {

    void sendSingleEmail(String title, String content, String receiverAddress, String receiverUsername) throws Exception;

    void sendEmails(String title, String content, List<String> receiverAddresses, List<String> receiverUsernames) throws Exception;
}
