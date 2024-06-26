package it.filippor.p2.framework;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *Utility class to retrieve a service from osgi framework
 * @author filippo.rossoni
 */
public class ServiceProvider {
  private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

  private final BundleContext ctx;

  private final List<ServiceReference<?>> refs = new ArrayList<>();

  /**
   * Constructor
   * @param bctx bundle context
   */
  public ServiceProvider(final BundleContext bctx) {
    this.ctx = bctx;
  }

  /**
   * retrieve service from context
   * @param <T> service type
   * @param clazz service type
   * @return service instance
   */
  public <T> T getService(final Class<T> clazz) {
    ServiceReference<T> ref = this.ctx.getServiceReference(clazz);
    if ((ref != null)) {
      this.refs.add(ref);
      final T ser = this.ctx.getService(ref);
      if ((ser == null)) {
        logger.warn("service for reference {} is null", this.refs);
      }
      return ser;
    } else {
      logger.warn("service {} not found", clazz.getName());
      return null;
    }
  }

  /**
   * retrieve service from context
   * @param service service id
   * @return service instance
   */
  public Object getService(final String service) {
    ServiceReference<?> ref = this.ctx.getServiceReference(service);
    if ((ref != null)) {
      this.refs.add(ref);
      final Object ser = this.ctx.getService(ref);
      if ((ser == null)) {
        ServiceProvider.logger.warn("service for reference {} is null", this.refs);
      }
      return ser;
    } else {
      ServiceProvider.logger.warn("service {} not found", service);
      return null;
    }
  }

  /**
   * release all service
   */
  public void ungetAll() {
    this.refs.forEach(ctx::ungetService);
  }
}
