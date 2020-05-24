package io.rukou.local;

import java.util.Base64;
import java.util.Map;

public class EnvClassLoader extends ClassLoader {

  final Map<String,String> env;

  public EnvClassLoader(){
    env = System.getenv();
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    String className = "CLASS_"+name.toUpperCase().replaceAll("\\.","_");
    System.out.println("looking for class "+name+" ("+className+")");
    if(env.containsKey(className)){
      String b64 = env.get(className);
      byte[] bytes = Base64.getDecoder().decode(b64);
      return defineClass(name,bytes,0,bytes.length);
    }
    return super.loadClass(name);
  }
}
