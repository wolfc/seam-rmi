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

import org.jboss.weld.environment.se.Weld;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SimpleTestCase {
    private static final String REMOTE_NAME = "rmi://localhost:1099/BeanInvoker";

    private static Registry registry;
    private static Weld weld;

    @AfterClass
    public static void afterClass() {
        weld.shutdown();
        registry = null;
    }

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        registry = LocateRegistry.createRegistry(1099);
        weld = new Weld();
        weld.initialize();
    }

    @Test
    public void testException() throws Exception {
        final RemotableBeanInvoker remote = (RemotableBeanInvoker) Naming.lookup("rmi://localhost:1099/BeanInvoker");
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final InvocationHandler handler = new RemoteInvocationHandler(remote, Simple.class.getName(), null, Simple.class);
        final Simple bean = (Simple) Proxy.newProxyInstance(loader, new Class<?>[]{Simple.class}, handler);

        try {
            bean.throwUp();
            fail("Should throw an exception");
        } catch (Exception e) {
            assertEquals("Bugger it", e.getMessage());
        }
    }

    @Test
    public void testRemoteCall() throws Exception {
        final RemotableBeanInvoker remote = (RemotableBeanInvoker) Naming.lookup("rmi://localhost:1099/BeanInvoker");
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final InvocationHandler handler = new RemoteInvocationHandler(remote, Simple.class.getName(), null, Simple.class);
        final Simple bean = (Simple) Proxy.newProxyInstance(loader, new Class<?>[]{Simple.class}, handler);

        final String result = bean.sayHi("test");
        assertEquals("Hi test", result);
    }
}
