import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 * 
 * @author Shengyu Jin
 * 
 */
public final class RSSAggregator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSAggregator() {
    }
    /**
     * numberOfChild Method's return {@value} for Tag with no child
     */
    private static final int NONE_CHILD = 0;
    /**
     * Processes one XML RSS (version 2.0) feed from a given URL converting it
     * into the corresponding HTML output file.
     * 
     * @param url
     *            the URL of the RSS feed
     * @param file
     *            the name of the HTML output file
     * @param out
     *            the output stream to report progress or errors
     * @updates out.content
     * @requires out.is_open
     * @ensures <pre>
     * [reads RSS feed from url, saves HTML document with table of news items
     *   to file, appends to out.content any needed messages]
     * </pre>
    */
    private static void processFeed(String url, String file, SimpleWriter out) {
        XMLTree xml = new XMLTree1(url);
        // checking for valid RSS 2.0
        boolean isValid = true;
        if ((xml.label().equals("rss")) && (xml.hasAttribute("version"))) {
            if (xml.attributeValue("version").equals("2.0")) {
                SimpleWriter htmlOut = new SimpleWriter1L(file);
                outputHeader(xml.child(0), htmlOut);
                String tag = "item";
                for (int i = 0; i < xml.child(0).numberOfChildren(); i++) {
                    if (xml.child(0).child(i).isTag()) {
                        if (tag.equals(xml.child(0).child(i).label())) {
                            processItem(xml.child(0).child(i), htmlOut);
                        }
                    }
                }
                outputFooter(htmlOut);
                htmlOut.close();
                out.println(file + " is successfully processed!");
            } else {
                isValid = false;
            }
        } else {
            isValid = false;
        }
        if (!isValid) {
            out.println(xml.label() + " is not a valid RSS 2.0 feed!");
        }
    }
    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     * 
     * <html>
     * <head>
     * <title>the channel tag title as the page title</title>
     * </head>
     * <body>
     *  <h1>the page title inside a link to the <channel> link</h1>
     *  <p>the channel description</p>
     *  <table border="1">
     *   <tr>
     *    <th>Date</th>
     *    <th>Source</th>
     *    <th>News</th>
     *   </tr>
     * 
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";
        out.println("<html>");
        out.println("<head>");
        //title
        int indexTitle = getChildElement(channel, "title");
        //check whether <title> is blank
        if (channel.child(indexTitle).numberOfChildren() == NONE_CHILD) {
            out.println("<title>Empty Title</title>");
        } else {
            out.println("<title>" + channel.child(indexTitle).child(0).label()
                    + "</title>");
        }
        out.println("</head>");
        out.println("<body>");
        //link
        int indexLink = getChildElement(channel, "link");
        out.println(" <h1 style=\"text-align:center;\">");
        out.println("  <a href=\"" + channel.child(indexLink).child(0).label() + "\" "
                + "style=\"color: blue;\">" + channel.child(indexTitle).child(0).label()
                  + "</a>");
        out.println(" </h1>");
        out.println("<hr></hr>");
        //description
        int indexDes = getChildElement(channel, "description");
        if (channel.child(indexDes).numberOfChildren() == NONE_CHILD) {
            out.println(" <p>No description</p>");
        } else {
            out.println(
                    " <p>" + channel.child(indexDes).child(0).label() + "</p>");
        }
        //table header
        out.println(" <table border=\"1\">");
        out.println("  <tr>");
        out.println("   <th>Date</th>");
        out.println("   <th>Source</th>");
        out.println("   <th>News</th>");
        out.println("  </tr>");
    }

    /**
     * Outputs the tags in the generated HTML file. These are the
     * expected elements generated by this method:
     * 
     * <html>
     * <head>
     * <title>the <feeds> title as the page title</title>
     * </head>
     * <body>
     *  <ul style="list-style-type:disc">
     *    <li>each feed title inside a link to the generated HTML page</li> * number
     *  of feeds
     *  </ul>
     * </body>
     * </html>
     * @param feeds
     *            the feeds element XMLTree
     * @param fileOut
     *            the output stream for .html file
     * @param out
     *            the output stream for console
     * @updates out.content
     * @requires [the root of feeds is a <feeds> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML tags]
     */
    private static void indexHeader(XMLTree feeds, SimpleWriter fileOut,
            SimpleWriter out) {
        String indexTitle = feeds.attributeValue("title");
        fileOut.println("<html>");
        fileOut.println("<head>");
        fileOut.println("<title>" + indexTitle + "</title>");
        fileOut.println("</head>");
        fileOut.println("<body>");
        fileOut.println(" <h1 style=\"text-align:center;\">" + indexTitle + "</h1>");
        fileOut.println(" <ul style=\"list-style-type:disc\">");
        //processing each feed url
        for (int i = 0; i < feeds.numberOfChildren(); i++) {
            XMLTree subFeed = feeds.child(i);
            String fileName = subFeed.attributeValue("file");
            String feedName = subFeed.attributeValue("name");
            String url = subFeed.attributeValue("url");
            processFeed(url, fileName, out);
            fileOut.println("  <li>");
            fileOut.println("   <a href=\"" + fileName + "\" " + "style=\"color: blue;\">"
                    + feedName + "</a>");
            fileOut.println("  </li>");
        }
        fileOut.println(" </ul>");
        fileOut.println("</body>");
        fileOut.println("</html>");
    }

    /**
     * Outputs the "closing" tags in the generated HTML file.  These are the
     * expected elements generated by this method:
     * 
     *  </table>
     * </body>
     * </html>
     * 
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";
        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     * 
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";
        int index = -1;
        for (int i = 0; i < xml.numberOfChildren(); i++) {
            if (xml.child(i).isTag()) {
                if (tag.equals(xml.child(i).label())) {
                    index = i;
                }
            }
        }
        return index;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     * 
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires
     * [the label of the root of item is an <item> tag] and out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";
        out.println("  <tr>");
        if (getChildElement(item, "pubDate") != -1) {
            int indexPubDate = getChildElement(item, "pubDate");
            out.println("   <td>" + item.child(indexPubDate).child(0).label() + "</td>");
        } else {
            out.println("   <td>No date available</td>");
        }
        if (getChildElement(item, "source") != -1) {
            int indexSource = getChildElement(item, "source");
            out.println("   <td>");
            out.println("    <a href=\"" + item.child(indexSource).attributeValue("url")
                    + "\" " + "style=\"color: blue;\">"
                     + item.child(indexSource).child(0).label() + "</a>");
            out.println("   </td>");
        } else {
            out.println("   <td>No source available</td>");
        }
        if (getChildElement(item, "title") != -1) {
            int indexTitle = getChildElement(item, "title");
            if (item.child(indexTitle).numberOfChildren() != 0) {
                if (getChildElement(item, "link") != -1) {
                    int indexLink = getChildElement(item, "link");
                    out.println("   <td>");
                    out.println("    <a href=\""
                           + item.child(indexLink).child(0).label()
                            + "\" " + "style=\"color: blue;\">"
                             + item.child(indexTitle).child(0).label() + "</a>");
                    out.println("   </td>");
                } else {
                    out.println("   <td>" + item.child(indexTitle).child(0).label()
                            + "</td>");
                }
            }
        } else {  //either <title> or <description> must be present
            int indexDes = getChildElement(item, "description");
            if (item.child(indexDes).numberOfChildren() != 0) {
                if (getChildElement(item, "link") != -1) {
                    int indexLink = getChildElement(item, "link");
                    out.println("   <td>");
                    out.println("    <a href=\""
                           + item.child(indexLink).child(0).label()
                            + "\" " + "style=\"color: blue;\">"
                             + item.child(indexDes).child(0).label() + "</a>");
                    out.println("   </td>");
                } else {
                    out.println("   <td>" + item.child(indexDes).child(0).label()
                            + "</td>");
                }
            } else {
                if (getChildElement(item, "link") != -1) {
                    int indexLink = getChildElement(item, "link");
                    out.println("   <td>");
                    out.println("    <a href=\""
                           + item.child(indexLink).child(0).label()
                            + "\" " + "style=\"color: blue;\">"
                             + "No title available" + "</a>");
                    out.println("   </td>");
                } else {
                    out.println("   <td>No title available</td>");
                }
            }
        }
    }

    /**
     * Main method.
     * 
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        out.print("Please enter the name of an XML file containing a list of URLs "
                + "for RSS v2.0 feeds: ");
        String inFile = in.nextLine();
        if (!inFile.contains(".")) {
            inFile = inFile + ".xml";
        }
        //Enable to assume it's a valid feeds file
        XMLTree rssReed = new XMLTree1(inFile);
        out.print("Enter the name of an output file: ");
        String outFile = in.nextLine();
        if (!outFile.contains(".")) {
            outFile = outFile + ".html";
        }
        SimpleWriter fileOut = new SimpleWriter1L(outFile);
        indexHeader(rssReed, fileOut, out);
        in.close();
        fileOut.close();
        out.close();
    }

}