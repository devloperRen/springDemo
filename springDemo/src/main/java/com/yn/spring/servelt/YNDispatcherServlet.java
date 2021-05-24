package com.yn.spring.servelt;


import com.yn.spring.autoAnnotation.YNAutowired;
import com.yn.spring.autoAnnotation.YNController;
import com.yn.spring.autoAnnotation.YNRequestMapping;
import com.yn.spring.autoAnnotation.YNService;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

@RequestMapping
public class YNDispatcherServlet extends HttpServlet {
    /**
     * 解析配置文件用
     */
    private Properties properties = new Properties();
    /**
     * 缓存类名
     */
    private List<String> classNameList = new ArrayList<String>();
    /**
     * IOC
     */
    private Map<String,Object> ioc = new HashMap<String, Object>();

    /**
     * 存放 Map<路径,方法名>
     */
    private Map<String,Method> handlerMappingMap = new HashMap<String, Method>();



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //这里调用具体方法、根据输入的url 通过response返回到页面上
        try {
            doDispatchServlet(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(" 500 server error. reason: "+ Arrays.toString(e.getStackTrace()));
        }


    }

    /**
     * @Description:  处理前端发送的请求
     * @Author: yangqh
     * @Version: 1.0
     * @Date:  2021/5/21 14:13
     * @Parameters:
     * @Return
     * @Throws
     */
    private void doDispatchServlet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String reqUrl = req.getRequestURI();
        String context = req.getContextPath();
        reqUrl = reqUrl.replaceAll(context,"").replaceAll("","");

        if(!handlerMappingMap.containsKey(reqUrl)){
            throw new Exception("404 not found");
        }

        //这里根据路径指向具体方法
        Method method = handlerMappingMap.get(reqUrl);
        //调用方法，传参
        String beanName = converLowerCase(method.getDeclaringClass().getSimpleName());
        //这里获取request携带的参数
        Map<String, String[]> param = req.getParameterMap();
        method.invoke(ioc.get(beanName),new Object[]{req,resp,param.get("name")});
    }


    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、加载配置文件 
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2、扫描类，传入配置文件里的配置信息
        doScanner(properties.getProperty("scanPackage"));
        //3、初始化IOC容器，将扫描到的类初始化到IOC容器中
        doInstance();
        //4、todo AOP
        //5、依赖注入
        doInject();
        //6、handlerMapping
        doInitHandlerMapping();
        //7、调用doPost()
        System.out.println("spring framework init over ");

    }

    


    /**
     * @Description: 加载配置文件
     * @Author: yangqh
     * @Version: 1.0
     * @Date:  2021/5/20 21:55
     * @Parameters:
     * @Return
     * @Throws
     */
    private void doLoadConfig(String ContextConfig) {
        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(ContextConfig);
            //如果配置文件是xml，那么saxReader解析
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(null != inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * @Description: 读取配置文件里的信息，递归扫描文件夹，找到里面的class文件。存储在缓存里(享元模式，各种池用的就是这个模式)
     * @Author: yangqh
     * @Version: 1.0
     * @Date:  2021/5/20 22:04
     * @Parameters:
     * @Return
     * @Throws
     */
    private void doScanner(String scanPackage) {
        //1、解析路径，将其变成文件路径 xx.xxx --->  /xx/xxx
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        //遍历当前目录下的所有文件，如果是目录，继续遍历，取出class文件的类名放入缓存中，以便后续实例化Class.forName("className")
        for(File f : classPath.listFiles()){
            if(f.isDirectory()){
                doScanner(scanPackage+"."+f.getName());
            }else{
                if(!f.getName().endsWith(".class")){ continue; }
                //为了防止多个包下面的类名一样，故存储值格式为包名+类名
                String className = scanPackage+"."+f.getName().replace(".class","");
                classNameList.add(className);
            }
        }

    }

    /**
     * @Description: 创建实例，并放入IOC容器中，以便后续注入
     * @Author: yangqh
     * @Version: 1.0
     * @Date:  2021/5/20 22:44
     * @Parameters:
     * @Return
     * @Throws
     */
    private void doInstance() {
        if(classNameList.isEmpty()){
            return;
        }

        for(String className: classNameList){
            try {
                Class<?> clazz =  Class.forName(className);
                if(clazz.isAnnotationPresent(YNController.class)){
                    //new 一个实例
                    Object instance = clazz.newInstance();
                    //以首字母小写的类名作为key，实例作为value存入IOC容器中
                    String beanName = converLowerCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);
                }else if(clazz.isAnnotationPresent(YNService.class) ){
                    //service 这里需要考虑到不同包下的相同类名。
                    //1、这里如果有相同的service 名称，那么优先取自定义命名
                    String beanName = clazz.getAnnotation(YNService.class).value();
                    if("".equals(beanName.trim())){
                        beanName = converLowerCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                    //2、如果是接口，且有多个实现类，就抛出异常
                    //获取当前类实现的接口
                    for( Class<?> cls : clazz.getInterfaces()){
                            //如果ioc里已经有这个接口类名了，说明之前已经有实现类实现了该接口
                        if(ioc.containsKey(cls.getName())){
                            throw new Exception("The "+cls.getName() + " is exists!!!");
                        }
                        ioc.put(cls.getName(),instance);
                    }


                }else{
                    continue;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * @Description: 将类名首字母转小写
     * @Author: yangqh
     * @Version: 1.0
     * @Date:  2021/5/20 22:54
     * @Parameters:
     * @Return
     * @Throws
     */
    private String converLowerCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //ASCII码中 大写字母排位于65～90  小写字母 97～122 故取类名的第一个字母再加上32，就得到了对应的小写字母
        chars[0] += 32;
        return String.valueOf(chars);
    }


    /**
     * @Description: 注入
     * @Author: yangqh
     * @Version: 1.0
     * @Date:  2021/5/21 10:40
     * @Parameters:
     * @Return
     * @Throws
     */
    private void doInject() {
        if(ioc.isEmpty()){
            return;
        }
        //遍历ioc容器里的每个类。
        //获取当前类的所有字段，如果类似有 @Autowired注解，则将ioc里的实例注入到该字段里。方便后续调用该类里的属性、方法
        for(Map.Entry<String,Object> entry:ioc.entrySet()){
            //取出每个实例，并获取其下所有的字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for(Field field : fields){
                if(!field.isAnnotationPresent(YNAutowired.class)){
                    continue;
                }

                YNAutowired ynAutowired = field.getAnnotation(YNAutowired.class);
                //@YNAutowired("value=xxx ")
                String beanName = ynAutowired.value().trim();
                //优先取自定义的命名,如果没有，就按照类型注入
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }

                //由于类里面声明字段往往都由private修饰，所以外界访问、操作时(后续的field.set(xxx,xxx) )需要强制访问
                field.setAccessible(true);

                //往当前类里注入字段的具体实例
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * @Description:  将路径作为key、方法作为value 放入容器中。后续根据路径访问的时候，直接调对应方法
     * @Author: yangqh
     * @Version: 1.0
     * @Date:  2021/5/21 11:30
     * @Parameters: 
     * @Return 
     * @Throws 
     */
    private void doInitHandlerMapping() {
        if(ioc.isEmpty()){
            return;
        }

        //获取@RequestMapping 修饰的方法
        for(Map.Entry<String,Object> entry : ioc.entrySet()){

            Class<?> clz = entry.getValue().getClass();

            //先看当前类有没有@YNController  注解。如果没有，就不需要纳入控制反转
            if(!clz.isAnnotationPresent(YNController.class)){
                continue;
            }
            //取出类上面@Controller 的注解
            YNRequestMapping requestMapping = clz.getAnnotation(YNRequestMapping.class);

            String baseUrl = requestMapping.value().trim();

            //这里只获取public 修饰的方法，故调用的是getMethods()
            for(Method method : clz.getMethods()){
                //如果方法上没有@YNRequestMapping 注解,略过
                if(!method.isAnnotationPresent(YNRequestMapping.class)){
                    continue;
                }
                YNRequestMapping ynRequestMapping = method.getAnnotation(YNRequestMapping.class);
                String url =("/"+ baseUrl+"/"+ynRequestMapping.value().trim()).replaceAll("/+","/");
                //将路径url作为key，方法作为value 存入handlerMappingMap
                handlerMappingMap.put(url,method);

            }
        }

    }

}
