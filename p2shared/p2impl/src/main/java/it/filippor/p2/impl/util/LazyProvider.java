package it.filippor.p2.impl.util;

import org.eclipse.core.runtime.IProgressMonitor;

public   class LazyProvider<T> {
    T data;
    final Provider<T> supplier;
    
    public  LazyProvider(Provider<T> supplier) {
      this.supplier = supplier;
    }


    public  T get(IProgressMonitor monitor) {
      try {
        if(data == null) {
          synchronized (this) {
            if(data == null)
              data = supplier.get(monitor); 
          }
        }
        return data;
      } catch (Exception e) {
        Utils.sneakyThrow(e);
        return null;
      }
    }
    @FunctionalInterface
    public interface Provider<T>{
      T get(IProgressMonitor monitor) throws Exception;
    }

  }