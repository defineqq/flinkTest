package org.apache.flink.client;

import apps.FeatureVideoRatioDuration;
import org.apache.flink.runtime.rest.RestServerEndpoint;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class fanshetest {
    @Test
    public static void main(String[] args) throws Exception {
//        FeatureVideoRatioDuration clazz = new FeatureVideoRatioDuration();//RestServerEndpoint.class;

        Field[] fields = RestServerEndpoint.class.getDeclaredFields();
//        field.setAccessible(true);
//        value = field.get('restBaseUrl').toString();

//        for (int i = 0; i < fields.length; i++) {
//            String name = fields[i].getName();	// 属性名
//            String typeName = fields[i].getGenericType().getTypeName();	//属性类型名
//            System.out.println(name+"@@@@@"+typeName);
//        }


        Field f = RestServerEndpoint.class.getDeclaredField("restBaseUrl");;//获取变量
        f.setAccessible(true);//使私有成员允许访问
        System.out.println(f.toString());

//        f.set(实例,值);//为指定实例的变量赋值,静态变量,第一参数给null
//        f.get(实例);//访问指定实例变量的值,静态变量,第一参数给null

//        Object o = clazz.newInstance();
//        Constructor<?>[] cs = clazz.getConstructors();
//        //3.通过高效for循环遍历数组
//        for(Constructor c : cs) {
//            System.out.println(c.getName());//打印本轮遍历到的构造方法的名字
//            Class[] pt = c.getParameterTypes();//通过本轮遍历到的构造函数对象获取构造函数的参数类型
//            System.out.println(Arrays.toString(pt));//打印参数类型
//        }



        //获取包名、类名
//        clazz.getPackage().getName()//包名
//        clazz.getSimpleName()//类名
//        clazz.getName()//完整类名
//
////获取成员变量定义信息
//        getFields()//获取所有公开的成员变量,包括继承变量
//        getDeclaredFields()//获取本类定义的成员变量,包括私有,但不包括继承的变量
//        getField(变量名)
//        getDeclaredField(变量名)
//
////获取构造方法定义信息
//        getConstructor(参数类型列表)//获取公开的构造方法
//        getConstructors()//获取所有的公开的构造方法
//        getDeclaredConstructors()//获取所有的构造方法,包括私有
//        getDeclaredConstructor(int.class,String.class)
//
////获取方法定义信息
//        getMethods()//获取所有可见的方法,包括继承的方法
//        getMethod(方法名,参数类型列表)
//        getDeclaredMethods()//获取本类定义的的方法,包括私有,不包括继承的方法
//        getDeclaredMethod(方法名,int.class,String.class)
//
////反射新建实例
//        clazz.newInstance();//执行无参构造创建对象
//        clazz.newInstance(222,"韦小宝");//执行有参构造创建对象
//        clazz.getConstructor(int.class,String.class)//获取构造方法
//
////反射调用成员变量
//        clazz.getDeclaredField(变量名);//获取变量
//        clazz.setAccessible(true);//使私有成员允许访问
//        f.set(实例,值);//为指定实例的变量赋值,静态变量,第一参数给null
//        f.get(实例);//访问指定实例变量的值,静态变量,第一参数给null
//
////反射调用成员方法
//        Method m = Clazz.getDeclaredMethod(方法名,参数类型列表);
//        m.setAccessible(true);//使私有方法允许被调用
//        m.invoke(实例,参数数据);//让指定实例来执行该方法
    }

}

