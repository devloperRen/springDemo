import javaSE.People;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 测试hashcode 不重写的情况会发生什么
 *经过测试得知：
 * 两个对象看起来一样，但是比较时，结果却为false，为什么呢？
 * 原来未重写前的equals方法用的是object里面的(this == obj)，比的是栈里面的引用地址，而引用地址又是通过hashCode函数算出来的，两个对象的hash值不一样，自然就不相等。
 * 比如长沙有个张三，深圳有个张三，名字虽然相同，但是却不是同一个人。 要想让它们相同，唯有重写equals方法(其实就是在原来的基础上，比较它们之间的内容，内容相同，就认为是相同的)
 * 其实重写完equals方法后，两个相同属性的对象相比返回的是true了。但是一旦你这些数据要存入到hash相关的数据结构里，会出现问题。比如hashmap，hashset等等。
 * 按照我们以往的使用，hashMap里put元素时，如果key值一样，后续put的值会覆盖前一个put的值 如：put(p1,1),put(p2,2), 这时候map里面只有一个元素，但事实上会有两个元素
 * 那是因为没重写hashCode的方法。hashMap存储元素的时候，他会先根据key值算出hash值，根据hash值给刚put进来的key-value找个位置存起来。{0,1,key-value,3,4,5,6,7,8}
 * put进来的key-value hash值不一样，存储的位置就不一样，也就可以认为是不同的对象，如果hash值一样(hash冲突)，那就用equals比较key，相同就替换值，不相同则生成一个链表，并将值追加到链表最后端
 *
 */
public abstract class PeopleHashTest {

    public static void main(String[] args) {
//        People p1 = new People("bob","1");
//        People p2 = new People("bob","1");
//        //native 修饰的方法，具体实现是在C语言里，但可以肯定的是，这两个对象的hash值肯定是不一样的。
//        int hash1 = p1.hashCode();
//        int hash2 = p2.hashCode();
//        System.out.println("hash1: "+hash1);
//        System.out.println("hash2: "+hash2);
//        boolean flag = p1.equals(p2)?true:false;
//        System.out.println(flag);

        Map m = new HashMap();
//        m.put(p1,1);
//        m.put(p2,2);
        m.put(null,3);
        m.put(null,4);
        m.put("1",null);
        System.out.println(m.size());
        System.out.println(m.get("1"));

//
//        HashSet set = new HashSet();
//        set.add("5");
//        System.out.println(set.size());

    }

    public abstract void say();
}
