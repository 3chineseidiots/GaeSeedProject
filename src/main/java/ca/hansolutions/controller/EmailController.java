package ca.hansolutions.controller;

import ca.hansolutions.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
@RequestMapping(value = "/email")
public class EmailController {

    @Autowired
    MailService mailService;

    @RequestMapping(value = "/send", method= RequestMethod.POST)
    public String sendEmail(Map<String, Object> map, String receiverUsername, String receiverAddress, String title, String content) throws Exception {

        mailService.sendSingleEmail(title, content, receiverAddress, receiverUsername);

        map.put("message", "The email is send to " + receiverUsername + " successfully!");

        return "mainpage";
    }


}
