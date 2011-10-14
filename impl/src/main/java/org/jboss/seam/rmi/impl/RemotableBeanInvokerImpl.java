/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.rmi.impl;

import org.jboss.weld.Container;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
class RemotableBeanInvokerImpl implements RemotableBeanInvoker {
    private final BeanManager beanManager;

    RemotableBeanInvokerImpl(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null)
            return Thread.currentThread().getContextClassLoader();
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    return null;
                }
            }
        });
    }

    @Override
    public Object invoke(String beanName, String[] qualifiers, Class<?> invokedInterface, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
//        final RequestContext requestContext = ((RequestContext) beanManager.getContext(RequestScoped.class));
        RequestContext requestContext = Container.instance().deploymentManager().instance().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
        try {
            requestContext.activate();

            Set<Bean<?>> beans = beanManager.getBeans(beanName);
            if(beans.isEmpty() || beans.size() > 1) {
                final Class<?> beanClass = loadClass(beanName);
                // TODO: qualifiers
                beans = beanManager.getBeans(beanClass);
            }
            if(beans.isEmpty() || beans.size() > 1) {
                throw new RemoteException("Ambiguous bean " + beanName);
            }
            final Bean<?> bean = beans.iterator().next();
            final CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
            try {
                final Object instance = beanManager.getReference(bean, bean.getBeanClass(), ctx);
                final Method method = invokedInterface.getMethod(methodName, parameterTypes);
                return method.invoke(instance, parameters);
            } finally {
                ctx.release();
            }
        } catch (ClassNotFoundException e) {
            throw new java.rmi.RemoteException("Can't load bean " + beanName, e);
        } catch (InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof Exception)
                throw (Exception) targetException;
            if (targetException instanceof Error)
                throw (Error) targetException;
            throw new RuntimeException(targetException);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            requestContext.invalidate();
            requestContext.deactivate();
        }
    }

    private static Class<?> loadClass(final String name) throws ClassNotFoundException {
        final ClassLoader loader = getContextClassLoader();
        try {
            if (loader != null)
                return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return Class.forName(name);
    }
}
