package com.yn.spring.controller;

import com.yn.spring.autoAnnotation.YNAutowired;
import com.yn.spring.autoAnnotation.YNController;
import com.yn.spring.autoAnnotation.YNRequestMapping;
import com.yn.spring.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;



@YNController
@YNRequestMapping("/yn")
public class DemoController {

    @YNAutowired
    private IDemoService iDemoService;

    @YNRequestMapping("/say")
    public void sayHello(HttpServletRequest req,HttpServletResponse response, Object name) throws IOException {

        String[] nameStr = (String[])name;
        String msg = iDemoService.getMsg(nameStr[0]);
        response.getWriter().print(msg);
    }
}
