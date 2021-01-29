package org.dspace.administer;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.apache.commons.cli.PosixParser;
import org.dspace.handle.HandleManager;
//#import org.dspace.content.factory.ContentServiceFactory;
//#import org.dspace.content.service.BitstreamFormatService;
//#import org.dspace.content.service.BitstreamService;
//#import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
//#import org.dspace.handle.factory.HandleServiceFactory;
//#import org.dspace.handle.service.HandleService;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * @author Pascal-Nicolas Becker
 */
public class IngestBitstreamTool {

    private static String bundleName = "ORIGINAL";
    private static String filename = null;
    private static String description = null;
    private static HelpFormatter formatter;
    private static boolean verbose = false;
    private Context context;
    private static Logger log = Logger.getLogger(IngestBitstreamTool.class);


    public IngestBitstreamTool() throws SQLException {
        context = new Context();
    }
    /**
     * Main launcher method for the bitstream ingest tool
     * @param argv
     * @throws Exception
     */
    public static void main(String[] argv)
            throws Exception {

        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        // required item (ID or handle) argument
        options.addOption("i", "item", true, "Target item handle or UUID");
        // required path to bitstream argument
        options.addOption("f", "file", true, "Full path to file (bitstream) to ingest to the target item");
        // optional target bundle name argument
        options.addOption("b", "bundle", true, "(Optional) bundle name to ingest this file. Default: ORIGINAL");
        // optional filename argument
        options.addOption("n", "name", true, "(Optional) filename for the new bitstream. Default: Filename of source file");
        // optional description argument
        options.addOption("d" , "description", true, "(Optional) description for the new bitstream");
        // optional verbose component
        options.addOption("v", "verbose", false, "Verbose output");
        // help
        options.addOption("h", "help", false, "Print this help message");

        CommandLine line = parser.parse(options, argv);
        formatter = new HelpFormatter();

        if (line.hasOption("h")) {
            usage(options);
        }

        if (!line.hasOption("i")) {
            System.out.println("Required item ID or handle is missing");
            usage(options);
        }

        if (!line.hasOption("f")) {
            System.out.println("Required path to file is missing");
            usage(options);
        }

        // Optional args
        if (line.hasOption("b")) {
            bundleName = line.getOptionValue("b");
        }
        if (line.hasOption("n")) {
            filename = line.getOptionValue("n");
        }
        if (line.hasOption("d")) {
            description = line.getOptionValue("d");
        }
        if (line.hasOption("v")) {
            verbose = true;
        }

        String identifier = line.getOptionValue("i");
        String epersonIdentifier = line.getOptionValue("e");
        String pathArg = line.getOptionValue("f");
        Path path = Paths.get(pathArg);
        if (!Files.isReadable(path) || !Files.isRegularFile(path)) {
            System.out.println("Input file must exist and be readable: " + pathArg);
            System.out.println("Absolute resolved path was " + path.toAbsolutePath().toString());
            log.error("Input file must exist and be readable: " + pathArg);
            usage(options);
            System.exit(1); // this should be called by usage(Options) already. Call it here, to be sure.
        }

        IngestBitstreamTool tool = new IngestBitstreamTool();

        // Perform the actual ingest
        tool.ingestBitstream(identifier, path, epersonIdentifier);
        System.exit(0);

    }

    /**
     * Perform the actual ingest to the item and set basic bitstream metadata
     * @param identifier
     * @param path
     * @param epersonIdentifier
     * @throws SQLException
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws AuthorizeException
     */
    public void ingestBitstream(String identifier, Path path, String epersonIdentifier) throws SQLException,
            IOException, IllegalArgumentException, AuthorizeException {

        verbose("Turning of authorisation system while we perform this ingest");
        if (context == null) {
            System.out.println("Context is null, aborting");
            System.exit(1);
        }
        
        try {
            // Turn off authorisation so we can create new bitstreams, bundles, update items
            context.turnOffAuthorisationSystem();

//#            ItemService itemService = ContentServiceFactory.getInstance().getItemService();
//#            HandleService handleService = HandleServiceFactory.getInstance().getHandleService();
//#            BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
//#            BundleService bundleService = ContentServiceFactory.getInstance().getBundleService();
//#            BitstreamFormatService bitstreamFormatService = ContentServiceFactory.getInstance().getBitstreamFormatService();

            Item item = null;

            verbose("Resolving " + identifier + " to item...");
            verbose("Context: " + context);

            try {
                Integer itemID = Integer.valueOf(identifier);
                DSpaceObject dso = Item.find(context, itemID);
                item = (Item)dso;
            } catch (IllegalArgumentException e) {
                verbose("Identifier is not UUID, trying handle instead.");
            }
            if (item == null) {
                // Try by handle
                DSpaceObject dso = HandleManager.resolveToObject(context, identifier);
                if (dso instanceof Item) {
                    item = (Item) dso;
                } else {
                    verbose("Identifier is not a handle.");
                }
            }
            
            if (item == null) {
                verbose("Couldn't resolve identifier (" + identifier + ") to a valid item.");
                throw new IllegalArgumentException("Couldn't detect identifier '" + identifier
                        + "'. Is it identifying an item?");
            }

            // Look for ORIGINAL bundle or create if not found
            Bundle[] bundles = item.getBundles(bundleName);
            Bundle bundle = null;
            if (bundles.length > 0) {
                verbose("Found bundle " + bundles[0].getName() + " in item, using that for new bitstream");
                bundle = bundles[0];
            } else {
                try {
                    verbose("Creating new " + bundleName + " bundle in item");
                    bundle = item.createBundle(bundleName);
                } catch (AuthorizeException e) {
                    System.out.println("Authorization error creating new " + bundleName + " bundle in item " + Integer.toString(item.getID()));
                    throw new AuthorizeException(e.getMessage());
                }
            }

            if (bundle == null) {
                verbose("Failed to find or create " + bundleName + " bundle for item " + Integer.toString(item.getID()));
                throw new IOException("Failed to find or create \" + bundleName + \" bundle for item");
            }

            // Get input stream from our path
            BufferedInputStream bif = null;
            try {
                bif = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ));
            } catch (IOException e) {
                System.out.println();
                verbose("IO error getting input stream from file: " + e.getMessage());
                throw new IOException(e);
            }

            // Ingest the actual bitstream and set basic metadata
            verbose("Creating bitstream in DSpace from file...");
            Bitstream bitstream = bundle.createBitstream(bif);
            if (filename == null) {
                filename = path.getFileName().toString();
            }
            verbose("Setting filename to " + filename);
            bitstream.setName(filename);
            bitstream.setSource(path.toAbsolutePath().toString());
            BitstreamFormat format = FormatIdentifier.guessFormat(context, bitstream);
            if (format == null) {
                System.err.println("We were unable to detect the bitstream's format. Please ensure that the file's " +
                        "suffix is registered in DSpace's bitstream format registry.");
                log.error("We were unable to detect the bitstream's format. Please ensure that the file's " +
                        "suffix is registered in DSpace's bitstream format registry.");
                System.exit(1);
            }
            verbose("Setting format to " + format.getMIMEType());
            bitstream.setFormat(format);
            if (description != null) {
                verbose("Setting description to " + description);
                bitstream.setDescription(description);
            }

            // Final update to new bitstream and target item
            bitstream.update();
            item.update();
            context.complete();
            String success = "New bitstream successfully ingested to item. Bitstream ID = "
                    + bitstream.getID() + ", Item ID = " + item.getID();
            log.info(success);
            System.out.println(success);
        } finally {
            // Restore auth state (not really needed since we're exiting now anyway, but just as good practice
            context.restoreAuthSystemState();
        }
    }

    /**
     * Print usage information and exit with exit code 1
     */
    private static void usage(Options options) {
        formatter.printHelp("dspace ingest", options);
        System.exit(1);
    }

    private static void verbose(String message) {
        if(verbose) {
            System.out.println(message);
            log.info(message);
        }
    }
}
