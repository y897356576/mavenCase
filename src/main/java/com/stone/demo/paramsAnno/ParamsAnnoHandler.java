package com.stone.demo.paramsAnno;

import com.alibaba.fastjson.JSONObject;
import com.stone.core.model.User;
import com.stone.demo.servletContext.GetServletContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 石头 on 2017/6/20.
 */
@Controller
@RequestMapping("/annotationHandler")
public class ParamsAnnoHandler {

    private Map map = new HashMap<String, String>(){{ put("result", "success"); }};

    @RequestMapping(method = RequestMethod.GET)
    public void getRequestAnnotationTest(@RequestParam(value = "paramFir")String param1, @RequestParam("paramSec")String param2,
                                         HttpServletRequest request, HttpServletResponse response){
        new GetServletContext().getServletContext();
        System.out.println("param1:" + param1);
        System.out.println("param2:" + param2);
//        response.setHeader("ResponseHeaderTest", "responseHeaderContent");
//        return "/html/front/index.html";
    }

    @RequestMapping(value = "/PathVal/{param}", method = RequestMethod.GET)
    public void getRequestAnnotationTest_PathVal(@PathVariable String param,
                                         HttpServletRequest request, HttpServletResponse response){
        System.out.println("param1:" + param);
//        response.setHeader("ResponseHeaderTest", "responseHeaderContent");
//        return "/html/front/index.html";
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map postRequestAnnotationTest_User(@RequestBody User user, String nameInUrl){
        System.out.println("userNameInUrl:" + nameInUrl);
        System.out.println("user:" + user);
        System.out.println("userNameInModel:" + user.getUserName());
        return map;
    }

    @RequestMapping(value = "/formData", method = RequestMethod.POST)
    @ResponseBody
    public Map postRequestFormDataTest_User(User user, String nameInUrl){
        System.out.println("userNameInUrl:" + nameInUrl);
        System.out.println("user:" + user);
        System.out.println("userNameInModel:" + user.getUserName());
        return map;
    }

    @RequestMapping(value = "/JSONObject", method = RequestMethod.POST)
    @ResponseBody
    public Map postRequestAnnotationTest_JSONObject(@RequestBody JSONObject obj, String nameInUrl){
        System.out.println("userNameInUrl:" + nameInUrl);
        System.out.println("user:" + obj);
        System.out.println("userName:" + obj.get("userName"));
        return map;
    }


    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map putRequestAnnotationTest(@RequestHeader("Host")String hHost,
                                   @RequestHeader("Accept")String hAccept,
                                   @RequestHeader(value = "ContentTest", required = false)String ContentTest,
                                   @RequestBody String user){
        System.out.println("Header_Host:" + hHost);
        System.out.println("Header_Accept:" + hAccept);
        System.out.println("Header_ContentTest:" + ContentTest);
        System.out.println("user:" + user);
        JSONObject jsonObj = JSONObject.parseObject(user);
        User u = JSONObject.toJavaObject(jsonObj, User.class);
        return map;
    }

    @RequestMapping(value = "/httpEntity", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity httpEntityAnnotationTest(HttpEntity<byte[]> httpEntity) throws UnsupportedEncodingException {
        HttpHeaders headers = httpEntity.getHeaders();
        for (Map.Entry entry : headers.entrySet()){
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        byte[] bodyByte = httpEntity.getBody();
        String bodyStr = new String(bodyByte, "utf-8");

        HttpHeaders headersResponse = new HttpHeaders();
        headersResponse.add("responseHeaderTest", "responseHeaderContent");
        return new ResponseEntity<String>("Hello, this is responseBody.", headersResponse, HttpStatus.CREATED);
    }
}
