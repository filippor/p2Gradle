package it.filippor.p2

import java.util.List
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.slf4j.LoggerFactory

class ServiceProvider {
	val static logger = LoggerFactory.getLogger(ServiceProvider)
	val BundleContext ctx
	val List<ServiceReference<?>> refs = newArrayList()

	new(BundleContext bctx) {
		this.ctx = bctx
	}

	def <T> T getService(Class<T> clazz) {
		var ref = ctx.getServiceReference(clazz)
		if (ref !== null) {
			refs += ref
			val ser = ctx.getService(ref)
			if(ser === null) logger.warn("service for reference {} is null", refs )
			return ser
		} else {
			logger.warn("service {} not found",clazz.name)
			return null
		}
	}
	def Object getService(String service) {
		var ref = ctx.getServiceReference(service)
		if (ref !== null) {
			refs += ref
			val ser = ctx.getService(ref)
			if(ser === null) logger.warn("service for reference {} is null", refs )
			return ser
		} else {
			logger.warn("service {} not found",service)
			return null
		}
	}

	def ungetAll() {
		refs.forEach [
			ctx.ungetService(it)
		]
	}
}
