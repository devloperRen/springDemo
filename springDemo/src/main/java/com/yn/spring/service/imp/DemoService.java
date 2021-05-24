package com.yn.spring.service.imp;

import com.yn.spring.autoAnnotation.YNService;
import com.yn.spring.service.IDemoService;

@YNService
public class DemoService implements IDemoService {

    @Override
    public String getMsg(String name) {
        String msg = " are you ok?  "+ name;
        return msg;
    }
}
