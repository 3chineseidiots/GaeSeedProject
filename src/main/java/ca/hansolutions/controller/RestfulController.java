package ca.hansolutions.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/rest")
public class RestfulController {

    @RequestMapping(value = "/company", method= RequestMethod.GET)
    public List<String> getResponse(){

        List<String> stringList = new ArrayList();
        stringList.add("han");
        stringList.add("solutions");
        stringList.add("inc");

        return stringList;
    }


}
