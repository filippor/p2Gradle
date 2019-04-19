package it.filippor.p2.impl.util;

import java.util.function.Supplier;

public   class LazyProvider<T> implements Supplier<T>{
    T data;
    final Provider<T> supplier;
    
    public  LazyProvider(Provider<T> supplier) {
      this.supplier = supplier;
    }


    @Override
    public  T get() {
      try {
        if(data == null) {
          synchronized (this) {
            if(data == null)
              data = supplier.get(); 
          }
        }
        return data;
      } catch (Exception e) {
        Utils.sneakyThrow(e);
        return null;
      }
    }
    @FunctionalInterface
    public static interface Provider<T>{
      T get() throws Exception;
    }

  }