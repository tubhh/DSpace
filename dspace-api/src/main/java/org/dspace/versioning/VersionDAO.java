/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Pascal-Nicolas Becker (dspace at pascal dash becker dot de)
 */
public class VersionDAO
{

    protected final static String TABLE_NAME = "versionitem";
    protected final static String VERSION_ID = "versionitem_id";
    protected final static String ITEM_ID = "item_id";
    protected final static String VERSION_NUMBER = "version_number";
    protected final static String EPERSON_ID = "eperson_id";
    protected final static String VERSION_DATE = "version_date";
    protected final static String VERSION_SUMMARY = "version_summary";
    protected final static String HISTORY_ID = "versionhistory_id";

    /**
     * Returns the number the next new version should get. If a version was deleted, we don't assign that number again,
     * unless we are reinstating the deleted version. This method ensures that we assign new version number only to new
     * versions.
     *
     * @param context DSpace's context object
     * @param versionHistory The VersionHistory to which the new version will belong to.
     * @throws SQLException If the database makes funky things
     * @return The next number a new version should get.
     */
    public int getNextVersionNumber(Context context, VersionHistory versionHistory) throws SQLException
    {
        String query = "SELECT (COALESCE(MAX(" + VERSION_NUMBER + "), 0) + 1) AS next FROM " + TABLE_NAME + " WHERE "
                + HISTORY_ID +" = ?";
        TableRow tr = DatabaseManager.querySingle(context, query, versionHistory.getVersionHistoryId());
        return tr.getIntColumn("next");
    }

    public VersionImpl find(Context context, int id) {
        try
        {
            TableRow row = DatabaseManager.findByUnique(context, TABLE_NAME, VERSION_ID, id);

            if (row == null)
            {
                return null;
            }

            return new VersionImpl(context, row);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public VersionImpl findByItem(Context c, Item item) {
        return findByItemId(c, item.getID());
    }

    public VersionImpl findByItemId(Context context, int itemId) {
        try {
            if (itemId == 0 || itemId == -1)
            {
                return null;
            }

            VersionImpl fromCache = (VersionImpl) context.fromCache(VersionImpl.class, itemId);
            if (fromCache != null)
            {
                return fromCache;
            }

            TableRow row = DatabaseManager.findByUnique(context, TABLE_NAME, ITEM_ID, itemId);
            if (row == null)
            {
                return null;
            }

            return new VersionImpl(context, row);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public VersionImpl findByVersionHistoryAndVersionNumber(Context context, VersionHistory vh, int versionNumber) throws SQLException
    {
        String query = "SELECT * FROM " + VersionDAO.TABLE_NAME + " WHERE " + VersionDAO.HISTORY_ID + " = ? AND "
                + VersionDAO.VERSION_NUMBER + " = ?";

        TableRow row = DatabaseManager.querySingle(context, query, vh.getVersionHistoryId(), versionNumber);

        if (row == null)
        {
            return null;
        }
        return new VersionImpl(context, row);
    }

    /**
     * This method returns versions that have an item associated. Instead of deleting versions
     * we set the item, date, summary and eperson null but preserve the version number. This is
     * necessary as some IdentifierProviders rely on unique version numbers. This method
     * returns only versions that were not (soft) deleted before. See
     * {@link #findAllByVersionHistory(Context, int)} if you're looking for a method returning
     * all versions.
     *
     * @param context DSpace's context object
     * @param versionHistoryId The version history's id
     * @return A list of all versions that have associated items and are connected to the provided version history.
     */
    public List<Version> findByVersionHistory(Context context, int versionHistoryId) {
        TableRowIterator tri = null;
        try
        {
            tri = DatabaseManager.query(context, "SELECT * FROM " + TABLE_NAME + " where " + HISTORY_ID + "="
                    + versionHistoryId + " AND " + ITEM_ID + " IS NOT NULL ORDER BY " + VERSION_NUMBER + " DESC");

            List<Version> versions = new ArrayList<Version>();
            while (tri.hasNext())
            {
                TableRow tr = tri.next();

                VersionImpl fromCache = (VersionImpl) context.fromCache(VersionImpl.class, tr.getIntColumn(VERSION_ID));

                if (fromCache != null)
                {
                    versions.add(fromCache);
                }
                else
                {
                    versions.add(new VersionImpl(context, tr));
                }
            }
            return versions;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        finally
        {
            if (tri != null)
            {
                tri.close();
            }
        }
    }

    /**
     * This method returns all versions of an version history, including soft deleted versions.
     * Instead of deleting a version, we set the item, date, summary and eperson null but
     * preserve the version number. This is necessary as some IdentifierServices rely on unique
     * version numbers. If a version gets deleted, we should not apply the same version number
     * to a nother version again. This method returns all versions, including the soft deleted
     * versions.
     *
     * @param context
     * @param versionHistoryId
     * @return
     */
    public List<Version> findAllByVersionHistory(Context context, int versionHistoryId)
    {
        TableRowIterator tri = null;
        try
        {
            tri = DatabaseManager.query(context, "SELECT * FROM " + TABLE_NAME + " where " + HISTORY_ID + "="
                    + versionHistoryId + " ORDER BY " + VERSION_NUMBER + " DESC");

            List<Version> versions = new ArrayList<Version>();
            while (tri.hasNext())
            {
                TableRow tr = tri.next();

                VersionImpl fromCache = (VersionImpl) context.fromCache(VersionImpl.class, tr.getIntColumn(VERSION_ID));

                if (fromCache != null)
                {
                    versions.add(fromCache);
                }
                else
                {
                    versions.add(new VersionImpl(context, tr));
                }
            }
            return versions;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        finally
        {
            if (tri != null)
            {
                tri.close();
            }
        }
    }


    public VersionImpl create(Context context) {
        try {
            TableRow row = DatabaseManager.create(context, TABLE_NAME);
            VersionImpl v = new VersionImpl(context, row);

            //TODO Do I have to manage the event?
            //context.addEvent(new Event(Event.CREATE, Constants.VERSION, e.getID(), null));

            return v;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * Use {@link VersioningService#removeVersion(Context, int)}  instead.
     *
     * @param c DSpace's context object
     * @param versionID The ID of the version you want to remove
     */
    protected void delete(Context c, int versionID) {
        try {
            //TODO Do I have to manage the event?
            //context.addEvent(new Event(Event.DELETE, Constants.VERSION, getID(), getEmail()));

            // Remove ourself
            VersionImpl version = find(c, versionID);
            if(version!=null){
                //Remove ourself from our cache first !
                c.removeCached(version, version.getVersionId());

                DatabaseManager.delete(c, version.getMyRow());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public void update(VersionImpl version) {
        try {
            //TODO Do I have to manage the event?
            DatabaseManager.update(version.getMyContext(), version.getMyRow());


//        if (modified)
//        {
//            myContext.addEvent(new Event(Event.MODIFY, Constants.EPERSON, getID(), null));
//            modified = false;
//        }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
