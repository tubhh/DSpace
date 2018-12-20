/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.handle.HandleManager;
import org.dspace.identifier.DOI;
import org.dspace.identifier.Handle;
import org.dspace.identifier.IdentifierService;
import org.dspace.utils.DSpace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public class VersioningConsumer implements Consumer
{

    /**
     * Configuration key to find the metadata field referencing the previous version.
     */
    public final static String PREVIOUS_ITEM_FIELD_CONFIG_KEY = "metadata.previous.item";

    /**
     * Configuration key to find the metadata field referencing the next newer version.
     */
    public final static String NEXT_ITEM_FIELD_CONFIG_KEY = "metadata.next.item";

    public final static String CONFIG_MODULE = "versioning";

    private final static Logger log = Logger.getLogger(VersioningConsumer.class);

    protected static Set<Item> itemsToProcess;

    private transient IdentifierService identifierService;
    protected transient MetadataField field_previous;
    protected transient MetadataField field_next;

    public void initialize() throws Exception {}

    public void finish(Context ctx) throws Exception {}

    public void consume(Context ctx, Event event) throws Exception
    {
        if (itemsToProcess == null)
        {
            itemsToProcess = new HashSet<Item>();
        }

        int st = event.getSubjectType();
        int et = event.getEventType();

        if (st == Constants.ITEM && et == Event.INSTALL)
        {
            Item item = (Item) event.getSubject(ctx);
            if (item != null && item.isArchived())
            {
                VersionHistory history = retrieveVersionHistory(ctx, item);
                if (history != null)
                {
                    Version latest = history.getLatestVersion();
                    Version previous = history.getPrevious(latest);
                    if (previous != null)
                    {
                        Item previousItem = previous.getItem();
                        if (previousItem != null)
                        {
                            createMetadata(ctx, previousItem, item);
                            previousItem.setArchived(false);
                            itemsToProcess.add(previousItem);
                            //Fire a new modify event for our previous item
                            //Due to the need to reindex the item in the search 
                            //and browse index we need to fire a new event
                            ctx.addEvent(new Event(Event.MODIFY,
                                                   previousItem.getType(),
                                                   previousItem.getID(),
                                                   null,
                                                   previousItem.getIdentifiers(ctx)));
                        }
                    }
                }
            }
        }
    }

    public void createMetadata(Context context, Item previous, Item item)
    {
        log.debug("Will reference between item " + previous.getID() + " and item " + item.getID() + ".");
        // load metadata fields
        loadMetadataFields(context);

        if (this.identifierService == null)
        {
            this.identifierService = new DSpace().getSingletonService(IdentifierService.class);
            if (this.identifierService == null)
            {
                log.warn("Cannot create version metadata between " + previous.getID() + " and " + item.getID() + "." + " Unable to load identifier service.");
                return;
            }
        }

        String previousIdentifier = null;
        String currentIdentifier = null;
        try
        {
            previousIdentifier = this.identifierService.lookup(context, previous, DOI.class);
            currentIdentifier = this.identifierService.lookup(context, item, DOI.class);
            if (StringUtils.isNotBlank(previousIdentifier))
            {
                previousIdentifier = DOI.DOIToExternalForm(previousIdentifier);
            }
            if (StringUtils.isNotBlank(currentIdentifier))
            {
                currentIdentifier = DOI.DOIToExternalForm(currentIdentifier);
            }
        }
        catch (Exception ex)
        {
            // we want to be sure to not interrupt the session. Therefore don't throw an excpetion, just log it.
            log.error("Caught an exception while trying to reference prvious/next versions of an item.", ex);

            // Log full stack trace if debug is enabled.
            if (log.isDebugEnabled())
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                log.debug(sw.toString());
            }
        }
        try
        {
            if (previousIdentifier == null)
            {
                previousIdentifier = this.identifierService.lookup(context, previous, Handle.class);
                if (StringUtils.isNotBlank(previousIdentifier))
                {
                    previousIdentifier = HandleManager.resolveToURL(context, previousIdentifier);
                }
            }
            if (currentIdentifier == null)
            {
                currentIdentifier = this.identifierService.lookup(context, item, Handle.class);
                if (StringUtils.isNotBlank(currentIdentifier))
                {
                    currentIdentifier = HandleManager.resolveToURL(context, currentIdentifier);
                }
            }
        }
        catch (Exception ex)
        {
            // we want to be sure to not interrupt the session. Therefore don't throw an excpetion, just log it.
            log.error("Caught an exception while trying to reference prvious/next versions of an item.", ex);

            // Log full stack trace if debug is enabled.
            if (log.isDebugEnabled())
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                log.debug(sw.toString());
            }
        }

        // reference the previous item in the next version's metadata
        try
        {
            referenceVersion(context, field_previous, item, previousIdentifier);
            // remind to call update on the changed items
            itemsToProcess.add(item);
        }
        catch (IllegalArgumentException ex)
        {
            log.debug("Either the field to reference the previous version couldn't be loaded or no previous version's " +
                              "identifer was found. Unable to link item " + item.getID() + " to its previous version.");
        }
        catch (SQLException ex)
        {
            // we want to be sure to not interrupt the session. Therefore don't throw an excpetion, just log it.
            log.error("Caught an exception while trying to reference prvious versions of an item.", ex);

            // Log full stack trace if debug is enabled.
            if (log.isDebugEnabled())
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                log.debug(sw.toString());
            }
        }

        // reference the previous item in the next version's metadata
        try
        {
            referenceVersion(context, field_next, previous, currentIdentifier);
            // remind to call update on the changed items
            itemsToProcess.add(previous);
        }
        catch (IllegalArgumentException ex)
        {
            log.debug("Either the field to reference the next version couldn't be loaded or no next version's " +
                              "identifer was found. Unable to link item " + previous.getID() + " to its next version.");
        }
        catch (SQLException ex)
        {
            // we want to be sure to not interrupt the session. Therefore don't throw an excpetion, just log it.
            log.error("Caught an exception while trying to reference next versions of an item.", ex);

            // Log full stack trace if debug is enabled.
            if (log.isDebugEnabled())
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                log.debug(sw.toString());
            }
        }

    }

    protected void referenceVersion(Context context, MetadataField field, Item item, String identifier)
            throws SQLException
    {
        if (StringUtils.isBlank(identifier) || field == null)
        {
            throw new IllegalArgumentException();
        }
        // we know that the field is not null (see if clause above), don't have to check that again
        // put the schema into a string to make the code more readable.
        String schema = MetadataSchema.find(context, field.getSchemaID()).getName();

        // If the identifier is not in the field, clear the field and set the identifier.
        // We need to clear the field as metadata fields are copied when a new version is created.
        if (! ArrayUtils.contains(item.getMetadata(schema,
                                                   field.getElement(),
                                                   field.getQualifier(),
                                                   Item.ANY),
                                  identifier))
        {
            item.clearMetadata(schema, field.getElement(), field.getQualifier(), null);
            item.addMetadata(schema,
                             field.getElement(),
                             field.getQualifier(),
                             null,
                             identifier);
        }
    }

    protected void loadMetadataFields(Context context)
    {
        if (field_previous != null && field_next != null)
        {
            // alreday initialized
            return;
        }

        field_previous = null;
        field_next = null;

        // initialize variables containing the schema, element and qualifier of the metadata fields referencing the next
        // or the previous version. The qualifier might be null, but should never be the empty string.
        String[] previous_field_config = parseField(ConfigurationManager.getProperty(CONFIG_MODULE, PREVIOUS_ITEM_FIELD_CONFIG_KEY));
        String[] next_field_config = parseField(ConfigurationManager.getProperty(CONFIG_MODULE, NEXT_ITEM_FIELD_CONFIG_KEY));
        String previous_version_schema = previous_field_config[0];
        String previous_version_element = previous_field_config[1];
        String previous_version_qualifier = previous_field_config[2];
        if (StringUtils.isEmpty(previous_version_qualifier))
        {
            previous_version_qualifier = null;
        }
        String next_version_schema = next_field_config[0];
        String next_version_element = next_field_config[1];
        String next_version_qualifier = next_field_config[2];
        if (StringUtils.isEmpty(next_version_qualifier))
        {
            next_version_qualifier = null;
        }

        if (StringUtils.isBlank(previous_version_schema) || StringUtils.isBlank(previous_version_element))
        {
            log.debug("Schema or element of the metadata field to reference the previous version of an item could" +
                              "not be loaded. Won't reference the previous version in the item metadata.");
            this.field_previous = null;
        } else
        {
            try
            {
                MetadataSchema schema_previous = MetadataSchema.find(context, previous_version_schema);
                this.field_previous = MetadataField.findByElement(context,
                                                                  schema_previous.getSchemaID(),
                                                                  previous_version_element,
                                                                  previous_version_qualifier);
            }
            catch (SQLException ex)
            {
                log.error("Unable to load metadata schema to reference an item's previous version: ", ex);
                // we should not throw an exception here, as this may lead to not store a new version at all.
                // log it and don't reference the item.
                this.field_previous = null;
            }
        }

        if (StringUtils.isBlank(next_version_schema) || StringUtils.isBlank(next_version_element))
        {
            log.debug("Schema or element of the metadata field to reference the next version of an item could" +
                              "not be loaded. Won't reference the next version in the item metadata.");
            this.field_next = null;
        } else
        {
            try
            {
                MetadataSchema schema_next = MetadataSchema.find(context, next_version_schema);
                this.field_next = MetadataField.findByElement(context,
                                                                  schema_next.getSchemaID(),
                                                                  next_version_element,
                                                                  next_version_qualifier);
            }
            catch (SQLException ex)
            {
                log.error("Unable to load metadata schema to reference an item's next version: ", ex);
                // we should not throw an exception here, as this may lead to not store a new version at all.
                // log it and don't reference the item.
                this.field_next = null;
            }
        }
    }

    protected static String[] parseField(String field)
    {
        String[] parts = StringUtils.split(field, '.');
        String schema = null, element = null, qualifier = null;
        if (parts.length >= 1)
        {
            schema = parts[0];
        }
        if (parts.length >= 2)
        {
            element = parts[1];
        }
        if (parts.length >= 3)
        {
            qualifier = parts [2];
        }

        return new String[] {schema, element, qualifier};
    }

    public void end(Context ctx) throws Exception {
        if(itemsToProcess != null){
            for(Item item : itemsToProcess){
                ctx.turnOffAuthorisationSystem();
                try {
                    item.update();
                } finally {
                    ctx.restoreAuthSystemState();
                }
            }
            ctx.getDBConnection().commit();
        }

        itemsToProcess = null;
    }


    private static org.dspace.versioning.VersionHistory retrieveVersionHistory(Context c, Item item) {
        VersioningService versioningService = new DSpace().getSingletonService(VersioningService.class);
        return versioningService.findVersionHistory(c, item.getID());
    }
}
