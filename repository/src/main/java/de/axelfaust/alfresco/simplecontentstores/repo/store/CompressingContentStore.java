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
package de.axelfaust.alfresco.simplecontentstores.repo.store;

import java.util.Collection;
import java.util.Set;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.PropertyCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Axel Faust
 */
public class CompressingContentStore extends CommonFacadingContentStore
{

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressingContentStore.class);

    protected ContentStore temporaryStore;

    protected String compressionType;

    protected Collection<String> mimetypesToCompress;

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet()
    {
        super.afterPropertiesSet();

        PropertyCheck.mandatory(this, "temporaryStore", this.temporaryStore);
    }

    /**
     * @param temporaryStore
     *            the temporaryStore to set
     */
    public void setTemporaryStore(final ContentStore temporaryStore)
    {
        this.temporaryStore = temporaryStore;
    }

    /**
     * @param compressionType
     *            the compressionType to set
     */
    public void setCompressionType(final String compressionType)
    {
        this.compressionType = compressionType;
    }

    /**
     * @param mimetypesToCompress
     *            the mimetypesToCompress to set
     */
    public void setMimetypesToCompress(final Collection<String> mimetypesToCompress)
    {
        this.mimetypesToCompress = mimetypesToCompress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentReader getReader(final String contentUrl)
    {
        final CompressingContentReader reader = new CompressingContentReader(super.getReader(contentUrl), this.compressionType,
                this.mimetypesToCompress);
        return reader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentWriter getWriter(final ContentContext context)
    {
        final ContentWriter backingWriter = super.getWriter(context);

        // this is a new URL so register for rollback handling
        final Set<String> urlsToDelete = TransactionalResourceHelper.getSet(StoreConstants.KEY_POST_ROLLBACK_DELETION_URLS);
        urlsToDelete.add(backingWriter.getContentUrl());

        final ContentWriter writer = new CompressingContentWriter(backingWriter.getContentUrl(), context, this.temporaryStore,
                backingWriter, this.compressionType, this.mimetypesToCompress);
        return writer;
    }

}
