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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@RemoteInterceptorBinding
@Interceptor
class RemoteInterceptor {
    @Inject
    private BeanManager beanManager;

//    @AroundInvoke
//    public Object aroundInvoke(final InvocationContext invocation) throws Exception {
//        if (isRequestContextActive()) {
//            return invocation.proceed();
//        } else {
//            RequestContext requestContext = Container.instance().deploymentManager().instance().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
//            try {
//                //requestContext.associate(invocation);
//                requestContext.activate();
//                try {
//                    return invocation.proceed();
//                } finally {
//                    requestContext.invalidate();
//                    requestContext.deactivate();
//
//                }
//            } finally {
//                //requestContext.dissociate(invocation);
//            }
//        }
//    }

//    private boolean isRequestContextActive() {
//        for (RequestContext requestContext : Container.instance().deploymentManager().instance().select(RequestContext.class)) {
//            if (requestContext.isActive()) {
//                return true;
//            }
//        }
//        return false;
//    }

    @PostConstruct
    public void postConstruct(final InvocationContext context) throws Exception {
        System.out.println("postConstruct " + context);
        context.proceed();
    }

    @PreDestroy
    public void preDestroy(final InvocationContext context) throws Exception {
        System.out.println("preDestroy " + context);
        context.proceed();
    }
}
