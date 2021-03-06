/*
 * Copyright 2016 Axel Faust
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package de.axelfaust.alfresco.simplecontentstores.repo.aop;

import java.util.Collection;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.axelfaust.alfresco.simplecontentstores.repo.store.context.ContentStoreContext;
import de.axelfaust.alfresco.simplecontentstores.repo.store.context.ContentStoreContext.ContentStoreOperation;
import de.axelfaust.alfresco.simplecontentstores.repo.store.context.ContentStoreContextInitializer;

/**
 * This interceptor initializes the {@link ContentStoreContext} on any call to the root {@link ContentStore content store} and thus
 * guarantees an active context is present for use within the configured chain of content stores.
 *
 * @author Axel Faust
 */
public class InitContentStoreContextInterceptor implements MethodInterceptor, ApplicationContextAware
{

    protected ApplicationContext applicationContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        return ContentStoreContext.executeInNewContext(new ContentStoreOperation<Object>()
        {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object execute()
            {
                final Object[] arguments = invocation.getArguments();
                final Collection<ContentStoreContextInitializer> initializers = InitContentStoreContextInterceptor.this.applicationContext
                        .getBeansOfType(ContentStoreContextInitializer.class, false, false).values();

                for (final Object argument : arguments)
                {
                    if (argument instanceof ContentContext)
                    {
                        for (final ContentStoreContextInitializer initializer : initializers)
                        {
                            initializer.initialize((ContentContext) argument);
                        }
                    }
                }

                try
                {
                    return invocation.proceed();
                }
                catch (final Throwable ex)
                {
                    throw new ContentIOException("Error during executing call on ContentStore API", ex);
                }
            }

        });
    }

}
