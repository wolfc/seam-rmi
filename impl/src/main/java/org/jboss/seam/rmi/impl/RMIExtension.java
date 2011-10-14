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

import org.jboss.seam.rmi.Remote;
import org.jboss.seam.rmi.RemoteException;
import org.jboss.solder.logging.Logger;
import org.jboss.solder.reflection.annotated.AnnotatedTypeBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@SuppressWarnings("unused")
public class RMIExtension implements Extension {
    private static final Logger log = Logger.getLogger(RMIExtension.class);

    private static boolean hasRemoteInterfaces(final Class<?> cls) {
        for(Class<?> intf : cls.getInterfaces()) {
            if(intf.isAnnotationPresent(Remote.class))
                return true;
        }
        return false;
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, final BeanManager beanManager) {
        log.info("RMI Extension initializing");
        try {
            final RemotableBeanInvoker beanInvoker = new RemotableBeanInvokerImpl(beanManager);
            final java.rmi.Remote remote = UnicastRemoteObject.exportObject(beanInvoker, 0);
            // TODO: this name must be configurable
            Naming.bind("BeanInvoker", remote);
        } catch (java.rmi.RemoteException e) {
            throw new RemoteException(e);
        } catch (MalformedURLException e) {
            throw new RemoteException(e);
        } catch (AlreadyBoundException e) {
            throw new RemoteException(e);
        }
    }

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event, final BeanManager beanManager) {
        final AnnotatedType<X> type = event.getAnnotatedType();
        if(!type.isAnnotationPresent(Remote.class) && !hasRemoteInterfaces(type.getJavaClass()))
            return;

        if(type.getJavaClass().isInterface())
            return;

        final AnnotatedTypeBuilder<X> builder = new AnnotatedTypeBuilder<X>().readFromType(type);
        builder.addToClass(RemoteInterceptorBindingLiteral.INSTANCE);

        event.setAnnotatedType(builder.create());
    }
}
