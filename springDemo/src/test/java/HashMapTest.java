import javaEE.Controller.LoginController;
import javaSE.People;
import javaSE.SpringConfigClass2;
import javaSE.SpringConfigureClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Field;
import java.util.*;

public class HashMapTest {

    @Autowired
    private People people;


   public static void main(String[] args) {
//        int num = 1;
//        Map m = new HashMap();
//        m.put("key1",1);
//        m.put("key2",1);
//        int n = m.size();
//       int no = (int)m.get("key1");
//        System.out.println(n);

//        String [] arr = {"1","2"};
//        List list = new ArrayList(Arrays.asList(arr));
//        list.add(1,"3");
//        System.out.println(list.size());

//    try{
//        System.out.println(111);
//        System.exit(0);
//        return;
//
//    }catch(Exception e){
//
//    }finally{
//        System.out.println(2222);
//    }

       ApplicationContext ap = new AnnotationConfigApplicationContext(SpringConfigureClass.class);
       SpringConfigClass2  c = ap.getBean(SpringConfigClass2.class);
       c.sayhello();


//       Field []  fields = new Field[0];
//       try {
//           fields = Class.forName("HashMapTest").getDeclaredFields();
//       } catch (ClassNotFoundException e) {
//           e.printStackTrace();
//       }
//       for(Field field:fields){
//           Autowired a =field.getAnnotation(Autowired.class);
//
//       }

   }

}
