/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.curate.Distributive;
import org.dspace.eperson.Group;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A curator task creating zip archives in BagIt format for items with more than one ORIGINAL bitstream.
 */
@Distributive
public class ArchiveOriginalBitstreams extends AbstractCurationTask {

    private final static String ARCHIVE_BUNDLE_NAME = "ARCHIVE";
    private final static String BAGIT_ARCHIVE_NAME = "container";
    private final static String BAGIT_ARCHIVE_FILE_NAME = BAGIT_ARCHIVE_NAME + ".zip";
    private final static String BAGIT_BASE_DIR = BAGIT_ARCHIVE_NAME + "/";
    private final static String BAGIT_PAYLOAD_DIR = "data/";
    private final static String BAGIT_MANIFEST_FILE_NAME = "manifest-md5.txt";
    private final static String BAGIT_DECLARATION_FILE_NAME = "bagit.txt";
    private final static String BAGIT_DECLARATION_CONTENT = "BagIt-Version: 0.97\nTag-File-Character-Encoding: UTF-8";
    
    protected List<String> errors = new ArrayList<>();
    
    private Group groupAnonymous;

    @Override
    public void init(Curator curator, String taskId) throws IOException {
        super.init(curator, taskId);
    }
    
    /**
     * Perform the curation task upon passed DSO.
     *
     * @param dso the DSpace object
     * @throws IOException if IO error
     */
    @Override
    public int perform(DSpaceObject dso) throws IOException
    {
        distribute(dso);
        if (errors.size() > 0)
        {
            StringBuilder errorBuilder = new StringBuilder();
            for (String error : errors)
            {
                report(error);
                errorBuilder.append(error);
                errorBuilder.append("\n");
            }
            setResult(errorBuilder.toString());
            return Curator.CURATE_ERROR;
        }
        return Curator.CURATE_SUCCESS;
    }
    
    /**
     * Performs the archive curation task on an item. <br>
     * If there are at most one bitstream and no old archive, no action is performed.<br>
     * Otherwise, if there is an old archive, it is checked whether all checksums match. If they do not,
     * the old archive is removed.
     * @param item the DSpace Item
     * @throws SQLException
     * @throws IOException
     */
    @Override
    protected void performItem(Item item) throws SQLException, IOException {
        // Do not operate on Items, that are still in the submission workflow
        if(item.isInProgressSubmission()) {
            report("Item " + item.getHandle() + " is in submission or workflow, skipping.");
            return;
        }

        List<Bitstream> originalBitstreams = getOriginalBitstreams(item);
        Bundle[] archiveBundles = item.getBundles(ARCHIVE_BUNDLE_NAME);

        if (groupAnonymous == null) {
            groupAnonymous = Group.find(Curator.curationContext(), Group.ANONYMOUS_ID);
        }

        try {

            // If there is maximum one original bitstream, and no old archive, don't do anything.
            if (originalBitstreams.size() < 2 && archiveBundles.length < 1) {
                report("Item " + item.getHandle() + " has maximum one bitstream, " +
                        "and there is no old archive, this item will not be processed.");
                return;
            }

            if (archiveBundles.length < 1) {
                // No archive bundle but several bitstreams, create new archive
                Bundle archiveBundle = item.createBundle(ARCHIVE_BUNDLE_NAME);
                String createMessage = createArchiveBitstream(archiveBundle, originalBitstreams);
                item.update();
                item.updateLastModified();

                String message = "The item with handle " + item.getHandle() + " has " + originalBitstreams.size()
                        + " bitstreams, new zip archive created. ";
                if (createMessage != null) {
                    message += createMessage;
                }
                report(message);
                setResult(message);

            }
            else {
                // Old archive bundle present, check if the checksums match
                Bundle archiveBundle = archiveBundles[0];

                // if there is a bundle with the name ARCHIVE and if it contains a file named like we name our archives
                // and if this file is either not a ZIP archive or a corrupted unreadable archive, then we may run into
                // an IOException. We could catch this exception and recreate the archive of course, but that would
                // cover underlying problems. Either our assetstore (that stores the archive) is unreliable or some
                // other function uses the same name for Bundles and files within this Bundle.
                if (!archiveChecksumsMatch(originalBitstreams, archiveBundle)) {
                    Bitstream archiveBitstream = archiveBundle.getBitstreamByName(BAGIT_ARCHIVE_FILE_NAME);

                    // remove the archive bitstream if it exists as the checksum doesn't match anymore
                    if(archiveBitstream != null) {
                        archiveBundle.removeBitstream(archiveBitstream);
                    }

                    // check if the bundle is empty and remove it
                    if (archiveBundle.getBitstreams().length == 0)
                    {
                        item.removeBundle(archiveBundle);
                    }

                    // Create a new archive only if there is more than one bitstream
                    if (originalBitstreams.size() > 1) {
                        String createMessage = createArchiveBitstream(archiveBundle, originalBitstreams);
                        item.update();
                        item.updateLastModified();
                        String message = "Checksums don't match; zip archive replaced for item with handle " + item.getHandle();
                        if (createMessage != null) {
                            message += "; " + createMessage;
                        }
                        report(message);
                        setResult(message);
                    }
                    else {
                        String message = "Checksums don't match but number of bitstreams below 2; removed old zip archive, did NOT created a new archive for the item with handle " + item.getHandle();
                        report(message);
                        setResult(message);
                    }
                } else {
                    report("All checksums match for item with handle " + item.getHandle());
                }

                // Check if the embargo end date matches the max embargo end date of the bitstreams
                Date maxEmbargoEndDate = getMaxEmbargoEndDate(originalBitstreams);
                Date archiveEmbargoEndDate = getMaxEmbargoEndDate(Arrays.asList(archiveBundle.getBitstreams()));

                if (maxEmbargoEndDate !=  null
                        && (archiveEmbargoEndDate == null
                        || !DateUtils.isSameDay(maxEmbargoEndDate, archiveEmbargoEndDate))) {
                    String message = "A bitstream has a later embargo end date as the archive, "
                            + "changing it in the archive to " + maxEmbargoEndDate;
                    report(message);
                    setResult(message);

                    Bitstream archiveBitstream = archiveBundle.getBitstreamByName(BAGIT_ARCHIVE_FILE_NAME);

                    List<ResourcePolicy> resourcePolicies = AuthorizeManager.getPoliciesActionFilter(
                            Curator.curationContext(),
                            archiveBitstream,
                            Constants.READ
                    );

                    for (ResourcePolicy resourcePolicy : resourcePolicies) {
                        if(resourcePolicy.getGroupID() == groupAnonymous.getID()) {
                            ResourcePolicy policy = AuthorizeManager.createOrModifyPolicy(resourcePolicy,
                                    Curator.curationContext(),
                                    null,
                                    groupAnonymous.getID(),
                                    null,
                                    maxEmbargoEndDate,
                                    Constants.READ,
                                    null,
                                    archiveBitstream);
                            policy.update();
                        }

                    }
                    item.update();
                }
                else {
                    report("Embargo end date matches for item with handle " + item.getHandle());
                }
            }

            if (archiveBundles.length > 1) {
                report("There is more than one " + ARCHIVE_BUNDLE_NAME + " bundle present in item " + item.getID()
                        + ", this should never be the case!");
            }

        } catch (AuthorizeException e) {
            errors.add("Error creating or modifying embargo end date: " + e);
        }
    }

    /**
     * Creates a bitstream with a zip archive of all original bitstreams.
     * @param archiveBundle the archive bundle
     * @param originalBitstreams a list of original bitstreams.
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    private String createArchiveBitstream(Bundle archiveBundle, List<Bitstream> originalBitstreams)
            throws SQLException, IOException, AuthorizeException {
        String returnMessage = null;

        Context context = Curator.curationContext();
        File archiveFile = createBagitZipArchive(context, originalBitstreams);
        Bitstream archiveBitstream = archiveBundle.createBitstream(new FileInputStream(archiveFile));
        archiveBitstream.setName(BAGIT_ARCHIVE_FILE_NAME);

        /*
         * Set the embargo end date for the archive to the last embargo end date of all the bitstreams.
         */
        Date maxEmbargoDate = getMaxEmbargoEndDate(originalBitstreams);

        if (maxEmbargoDate != null) {
            returnMessage = "Setting embargo for archive to " + maxEmbargoDate;
            report(returnMessage);
            ResourcePolicy policy = AuthorizeManager.createOrModifyPolicy(null,
                    context,
                    null,
                    groupAnonymous.getID(),
                    null,
                    maxEmbargoDate,
                    Constants.READ,
                    null,
                    archiveBitstream);
            policy.update();
        }
        return returnMessage;
    }

    /**
     * Gets the maximum embargo end date of all bitstreams in the list.
     * @param originalBitstreams
     * @return the max embargo end date or null if no embargo set.
     * @throws SQLException
     */
    private Date getMaxEmbargoEndDate(List<Bitstream> originalBitstreams)
            throws SQLException {
        Date maxEmbargoDate = null;
        for (Bitstream bitstream : originalBitstreams) {
            List<ResourcePolicy> resourcePolicies = AuthorizeManager.getPoliciesActionFilter(
                    Curator.curationContext(),
                    bitstream,
                    Constants.READ
            );

            for (ResourcePolicy resourcePolicy : resourcePolicies) {
                if (resourcePolicy.getGroupID() == groupAnonymous.getID() && resourcePolicy.getStartDate() != null) {
                    report(" - Bitstream embargo found: " + resourcePolicy.getStartDate());
                    if (maxEmbargoDate == null || maxEmbargoDate.before(resourcePolicy.getStartDate())) {
                        maxEmbargoDate = resourcePolicy.getStartDate();
                    }
                }
            }
        }
        return maxEmbargoDate;
    }

    /**
     * Retrieves all ORIGINAL bitstream objects in an item.
     * @param item the item object
     * @return a list with the bitstreams-
     */
    private List<Bitstream> getOriginalBitstreams(Item item) throws SQLException {
        List<Bitstream> bitstreams = new ArrayList<>();

        for (Bundle bundle : item.getBundles()) {
            if ("ORIGINAL".equals(bundle.getName())) {
                bitstreams.addAll(Arrays.asList(bundle.getBitstreams()));
            }
        }
        return bitstreams;
    }

    /**
     * Checks if the checksums of the ORIGINAL bitstream are the same as those for the files in the archive BagIt zip.
     *
     * @param originalBitstreams the ORIGINAL bitstreams
     * @param archiveBundle the archive bundle
     * @return true, if all ORIGINAL bitstreams are present in the archive and all the checksums match.
     *
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    private boolean archiveChecksumsMatch(List<Bitstream> originalBitstreams, Bundle archiveBundle)
            throws SQLException, IOException, AuthorizeException {
    
        // check if the archive bundle contain files
        if(archiveBundle.getBitstreams().length < 1) {
            return false;
        }
        
        // check if it contains a file with our archive file name.
        Bitstream archiveBitstream = archiveBundle.getBitstreamByName(BAGIT_ARCHIVE_FILE_NAME);
        if (archiveBitstream == null)
        {
            return false;
        }
        
        // try to open it as a ZIP archive. This throws an IOException if it is not a ZIP archive, corrupted or an IO
        // error raises.
        ZipInputStream zipInputStream = new ZipInputStream(archiveBitstream.retrieve());
        String manifestContent = getManifestContent(zipInputStream, BAGIT_BASE_DIR + BAGIT_MANIFEST_FILE_NAME);
        Map<String, String> checksumMap = getChecksumMap(manifestContent);

        if (originalBitstreams.size() != checksumMap.size()) {
            return false;
        }

        for (Bitstream bitstream : originalBitstreams) {
            if (!StringUtils.equals(checksumMap.get(
                    BAGIT_PAYLOAD_DIR + getMd5FileName(bitstream.getName())), bitstream.getChecksum())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the contents of the manifest file of a BagIt zip-file.
     *
     * @param zipInputStream the input stream of the zip file
     * @param manifestFileName the name of the manifest file
     * @return a string with the manifest content
     * @throws IOException if the zip stream cannot de read
     */
    private String getManifestContent(ZipInputStream zipInputStream, String manifestFileName)
            throws IOException {
        String content = null;
        ZipEntry zipEntry;

        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (StringUtils.equals(manifestFileName, zipEntry.getName())) {
                content = IOUtils.toString(zipInputStream, Charset.forName("UTF-8"));
            }
        }
        return content;
    }

    /**
     * Creates a map with file names as keys and checksums as values from the content of a BagIt manifest file.
     * @param manifestContent the content of the manifest file as string.
     * @return a map with the result.
     */
    private Map<String, String> getChecksumMap(String manifestContent) {
        Map<String, String> checksumMap = new HashMap<>();
        if (manifestContent != null) {
            String[] rows = manifestContent.split("\\n");
            for (String row : rows) {
                String[] valueKey = row.split(" ", 2);
                if (valueKey.length == 2) {
                    checksumMap.put(valueKey[1], valueKey[0]);
                }
            }
        }
        return checksumMap;
    }


    /**
     * Creates a zip archive according to BagIt specifications.
     *
     * @param context the curator context
     * @param bitstreamList a list of bitstreams for the archive
     * @return the generated zip file
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    private File createBagitZipArchive(Context context, List<Bitstream> bitstreamList)
            throws SQLException, IOException, AuthorizeException {

        if (bitstreamList == null || bitstreamList.size() == 0) {
            return null;
        }

        String tempFileName = bitstreamList.get(0).getChecksum();
        File tempZipFile = File.createTempFile(tempFileName, ".zip");

        Set<String> fileNamesInZip = new HashSet();

        try {

            FileOutputStream fileOutputStream = new FileOutputStream(tempZipFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream, Charset.forName("UTF-8"));

            StringBuffer checksums = new StringBuffer();

            for (Bitstream bitstream : bitstreamList) {
                String fileName = getMd5FileName(bitstream.getName());
                // You can't have several files with the same name in a zip
                if (fileNamesInZip.contains(fileName)) {
                    continue;
                }
                else {
                    fileNamesInZip.add(fileName);
                }

                addZipEntry(zipOutputStream, fileName, bitstream.retrieve());

                // For the BagIt checksum file
                checksums.append(bitstream.getChecksum());
                checksums.append(" ");
                checksums.append(BAGIT_PAYLOAD_DIR + fileName);
                checksums.append("\n");

            }

            //Write manifest file to zip
            ZipEntry zipEntry = new ZipEntry(BAGIT_BASE_DIR + BAGIT_MANIFEST_FILE_NAME);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(checksums.toString().getBytes());

            //Write begit declation file to zip
            zipEntry = new ZipEntry(BAGIT_BASE_DIR + BAGIT_DECLARATION_FILE_NAME);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(BAGIT_DECLARATION_CONTENT.getBytes());

            zipOutputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            errors.add("Error creating zip archive: " + e);
            throw e;
        }

        return tempZipFile;
    }

    /**
     * Adds a zip entry to a zipOutputStream using an inputStream.
     *
     * @param zipOutputStream
     * @param fileName
     * @param inputStream
     * @throws IOException
     */
    void addZipEntry(ZipOutputStream zipOutputStream, String fileName, InputStream inputStream)
            throws IOException {
        ZipEntry zipEntry = new ZipEntry(BAGIT_BASE_DIR + BAGIT_PAYLOAD_DIR + fileName);
        zipOutputStream.putNextEntry(zipEntry);

        int length;
        byte[] buffer = new byte[2048];
        while ((length = inputStream.read(buffer, 0, buffer.length)) > 0) {
            zipOutputStream.write(buffer, 0, length);
        }
        zipOutputStream.closeEntry();
        inputStream.close();
    }

    /**
     * Creates a md5 string from the filename and appends the extension (if any).
     *
     * @param fileName the name of the file
     * @return a string with the me5 filename
     */
    String getMd5FileName(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isNotEmpty(extension)) {
            extension = "." + extension;
        }
        return DigestUtils.md5Hex(fileName) + extension;
    }

}